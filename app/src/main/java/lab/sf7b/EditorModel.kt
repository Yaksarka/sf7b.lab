package lab.sf7b

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.SystemClock
import android.provider.DocumentsContract
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import lab.sf7b.core.*
import java.io.*
import kotlin.math.*

@OptIn(kotlinx.coroutines.FlowPreview::class)
class EditorModel(app:Application):AndroidViewModel(app) {
    private val context=app
    private val library by lazy{lab.sf7b.core.ProjectLibrary(app.filesDir)}
    private var activeDirectory=File(app.filesDir,"workspace")
    val projectDirectory:File get()=activeDirectory
    private val project:ProjectFiles get()=ProjectFiles(activeDirectory)
    private val imageCache=File(app.cacheDir,"sources")
    private val preferences=app.getSharedPreferences("editor",0)
    private data class SaveRequest(val id:String,val document:Document?=null,val thumbnail:ByteArray?=null,val barrier:CompletableDeferred<Unit>?=null)
    private data class RenderRequest(val projectId:String,val directory:File,val document:Document,val revision:Long)
    private val saveQueue=Channel<SaveRequest>(Channel.UNLIMITED)
    private val renderMutex=Mutex()
    private val thumbnailMutex=Mutex()
    private val presetMutex=Mutex()
    private var originalKey=""
    private val input=MutableStateFlow<RenderRequest?>(null)
    private var renderRevision=0L
    private var reportedRecoveryCount=0
    var activeProjectId by mutableStateOf("");private set
    var projectEntries by mutableStateOf<List<lab.sf7b.core.ProjectEntry>>(emptyList());private set
    private val history=ArrayDeque<Document>()
    private val future=ArrayDeque<Document>()
    private var longJob:Job?=null
    var document by mutableStateOf<Document?>(null); private set
    var bitmap by mutableStateOf<Bitmap?>(null); private set
    var original by mutableStateOf<Bitmap?>(null); private set
    var renderedDocument by mutableStateOf<Document?>(null); private set
    var home by mutableStateOf(true)
    var loading by mutableStateOf(true); private set
    var rendering by mutableStateOf(false); private set
    var working by mutableStateOf(false); private set
    var progress by mutableFloatStateOf(0f); private set
    var message by mutableStateOf<String?>(null)
    var activeLayer by mutableStateOf("")
    var activeNode by mutableStateOf("")
    var undoCount by mutableIntStateOf(0); private set
    var redoCount by mutableIntStateOf(0); private set
    var language by mutableStateOf(preferences.getString("language","ru")!!);private set
    var theme by mutableStateOf(preferences.getString("theme","system")!!);private set
    var quality by mutableIntStateOf(preferences.getInt("quality",1024));private set
    var presets by mutableStateOf<List<Preset>>(emptyList());private set
    var operation by mutableStateOf("");private set
    var lastTiming by mutableStateOf("");private set
    @Volatile var lastExportBackend="";private set
    var hardwareInfo by mutableStateOf("");private set
    fun tr(ru:String,en:String)=if(language=="ru")ru else en
    init {
        viewModelScope.launch(Dispatchers.IO){hardwareInfo=deviceInfo(context);File(context.filesDir,"device-info.txt").writeText(hardwareInfo)}
        viewModelScope.launch(Dispatchers.IO){for(request in saveQueue){try{
            request.document?.let{if(request.thumbnail!=null)library.saveThumbnail(request.id,request.thumbnail,it)else library.save(request.id,it)}
            request.barrier?.complete(Unit)
            if(request.document!=null){val entries=library.list();withContext(Dispatchers.Main){projectEntries=entries}}
        }catch(e:Exception){request.barrier?.completeExceptionally(e);withContext(Dispatchers.Main){message=tr("Не удалось сохранить проект: ","Could not save project: ")+e.message}}}}
        viewModelScope.launch {
            try{projectEntries=withContext(Dispatchers.IO){library.list()};val id=withContext(Dispatchers.IO){library.activeId()};if(!id.isNullOrEmpty()){val saved=withContext(Dispatchers.IO){library.open(id)};activate(saved.entry.id,saved.directory,saved.document,false)};presets=withContext(Dispatchers.IO){loadPresets()};reportRecoveryNotices()}catch(e:Exception){message=tr("Ошибка восстановления: ","Recovery failed: ")+e.message}finally{loading=false}
        }
        viewModelScope.launch {
            input.filterNotNull().debounce(80).collectLatest{request->
                val d=request.document
                rendering=true
                try{val result=withContext(Dispatchers.Default){renderMutex.withLock{
                    val result=renderPreview(d,request.directory)
                    val first=d.layers.firstOrNull()
                    if(first!=null){val sourceId="source-${first.id}";val baseline=d.copy(root=sourceId,layers=listOf(first.copy(head=sourceId,mask=null)),nodes=listOf(Node(id=sourceId,kind="source",layer=first.id)));val key=Codec.encode(baseline)
                        if(original==null||originalKey!=key){val source=renderPreview(baseline,request.directory);withContext(Dispatchers.Main){if(request.projectId==activeProjectId&&request.revision==renderRevision){original=source;originalKey=key}}}
                    }
                    result
                }};if(request.projectId==activeProjectId&&request.revision==renderRevision){bitmap=result;renderedDocument=d
                    if(d==document){val thumb=withContext(Dispatchers.Default){val small=Bitmap.createScaledBitmap(result,min(256,result.width),max(1,(result.height*min(1f,256f/result.width)).roundToInt()),true);try{ByteArrayOutputStream().also{small.compress(Bitmap.CompressFormat.PNG,100,it)}.toByteArray()}finally{if(small!==result)small.recycle()}};if(d==document&&request.projectId==activeProjectId)saveQueue.trySend(SaveRequest(request.projectId,d,thumb))}
                }
                }catch(_:CancellationException){}catch(e:OutOfMemoryError){message=tr("Недостаточно памяти. Уменьшите качество предпросмотра в настройках.","Not enough memory. Lower preview quality in Settings.")}catch(e:Exception){message=tr("Ошибка обработки: ","Render error: ")+e.message}finally{rendering=false}
            }
        }
    }
    fun settings(lang:String=language,mode:String=theme,preview:Int=quality){language=lang;theme=mode;quality=preview;preferences.edit().putString("language",lang).putString("theme",mode).putInt("quality",preview).apply();document?.let{refreshPreview(it)}}
    private suspend fun reportRecoveryNotices(){
        val available=withContext(Dispatchers.IO){library.recoveryNotices}
        val pending=available.drop(reportedRecoveryCount);reportedRecoveryCount=available.size
        if(pending.isNotEmpty())withContext(Dispatchers.Main){message=pending.joinToString("\n\n"){recoveryNoticeText(it,language)}}
    }
    private fun requestPreview(d:Document){if(activeProjectId.isNotEmpty())input.value=RenderRequest(activeProjectId,activeDirectory,d,++renderRevision)}
    private fun persist(d:Document){saveQueue.trySend(SaveRequest(activeProjectId,d))}
    suspend fun flushSaves(){val barrier=CompletableDeferred<Unit>();saveQueue.send(SaveRequest(activeProjectId,document,barrier=barrier));barrier.await()}
    fun checkpoint(){document?.let{persist(it)}}
    fun commit(d:Document){try{d.validate();if(d==document)return;document?.let{history.addLast(it)};while(history.size>48)history.removeFirst();future.clear();document=d;undoCount=history.size;redoCount=0;if(d.layers.none{it.id==activeLayer})activeLayer=d.layers.lastOrNull()?.id?:"";requestPreview(d);persist(d)}catch(e:Exception){message=e.message}}
    fun undo(){if(history.isNotEmpty()){document?.let{future.addLast(it)};val d=history.removeLast();document=d;undoCount=history.size;redoCount=future.size;activeLayer=d.layers.lastOrNull()?.id?:"";requestPreview(d);persist(d)}}
    fun redo(){if(future.isNotEmpty()){document?.let{history.addLast(it)};val d=future.removeLast();document=d;undoCount=history.size;redoCount=future.size;activeLayer=d.layers.lastOrNull()?.id?:"";requestPreview(d);persist(d)}}
    fun preview(d:Document){requestPreview(d)}
    fun cancelPreview(){document?.let{requestPreview(it)}}
    private fun refreshPreview(d:Document){requestPreview(d)}
    private fun activate(id:String,directory:File,d:Document,edit:Boolean=true){activeProjectId=id;activeDirectory=directory;document=d;history.clear();future.clear();undoCount=0;redoCount=0;activeLayer=d.layers.lastOrNull()?.id?:"";activeNode="";bitmap=null;original=null;originalKey="";renderedDocument=null;home=!edit;requestPreview(d)}
    fun newCanvas(w:Int,h:Int,color:Int){work(tr("Создание проекта","Create project")){_->flushSaves();val entry=library.create(Document.canvas(w,h,color));val opened=library.open(entry.id);val entries=library.list();withContext(Dispatchers.Main){activate(entry.id,opened.directory,opened.document);projectEntries=entries}}}
    fun openLibraryProject(id:String){work(tr("Открытие проекта","Open project")){_->flushSaves();val opened=library.open(id);library.setActive(id);withContext(Dispatchers.Main){activate(id,opened.directory,opened.document)}}}
    fun renameProject(id:String,name:String){work(tr("Переименование","Rename")){_->flushSaves();library.rename(id,name);val opened=if(id==activeProjectId)library.open(id)else null;val entries=library.list();withContext(Dispatchers.Main){opened?.let{document=it.document;requestPreview(it.document)};projectEntries=entries}}}
    fun duplicateProject(id:String){work(tr("Создание копии","Duplicate project")){_->flushSaves();library.duplicate(id);val entries=library.list();withContext(Dispatchers.Main){projectEntries=entries}}}
    fun deleteProject(id:String){work(tr("Удаление проекта","Delete project")){_->flushSaves();library.delete(id);val entries=library.list();withContext(Dispatchers.Main){if(id==activeProjectId){activeProjectId="";document=null;bitmap=null;original=null;input.value=null;renderRevision++;history.clear();future.clear();undoCount=0;redoCount=0;home=true};projectEntries=entries}}}
    fun selectLayer(id:String){activeLayer=id;activeNode=document?.layers?.find{it.id==id}?.head?:""}
    fun updateLayer(l:Layer){document?.let{commit(it.withLayer(l))}}
    fun addLayer(l:Layer){document?.let{commit(it.addLayer(l));activeLayer=l.id}}
    fun addEffect(id:String){val d=document?:return;if(activeLayer.isEmpty())return;commit(d.addEffect(activeLayer,Catalog.newEffect(id)));activeNode=document!!.layer(activeLayer).head}
    fun cancelWork(){longJob?.cancel()}
    private fun work(label:String,block:suspend (()->Unit)->Unit){
        if(working)return
        working=true;progress=0f;operation=label
        longJob=viewModelScope.launch{val start=SystemClock.elapsedRealtime()
            try{withContext(Dispatchers.IO){val coroutine=currentCoroutineContext();block{coroutine.ensureActive()}};reportRecoveryNotices();lastTiming="$label: ${(SystemClock.elapsedRealtime()-start)/1000f} s"
            }catch(_:CancellationException){message=tr("Отменено","Cancelled")}catch(e:OutOfMemoryError){message=tr("Недостаточно памяти. Выберите меньший размер явно.","Not enough memory. Choose a smaller size explicitly.")}catch(e:Exception){message=tr("Ошибка: ","Error: ")+e.message}finally{working=false;operation=""}
        }
    }
    fun importImage(uri:Uri,asLayer:Boolean=false){work(tr("Импорт","Import")){check->
        flushSaves();val adding=asLayer&&document!=null;val ownerId=activeProjectId
        val stage=if(adding)projectDirectory else File(context.cacheDir,"image-import-${uid()}")
        val path="assets/${uid()}.image";val file=File(stage,path);file.parentFile!!.mkdirs()
        var retained=false
        try{context.contentResolver.openInputStream(uri)!!.use{copyChecked(it,file.outputStream(),check)};val repo=ImageRepository(stage,imageCache,check);val info=repo.inspect(file);repo.ensure(path,256)
            val l=Layer(name=tr("Изображение","Image"),kind="image",asset=path)
            if(adding){withContext(Dispatchers.Main){require(activeProjectId==ownerId);commit(document!!.addLayer(l));retained=true;activeLayer=l.id;original=null;home=false}}
            else{val entry=library.create(Document(width=info.width,height=info.height).addLayer(l),stage);val opened=library.open(entry.id);library.setActive(entry.id);val entries=library.list();withContext(Dispatchers.Main){activate(entry.id,opened.directory,opened.document);projectEntries=entries}}
        }catch(e:Throwable){if(!retained)file.delete();throw e}finally{if(!adding)stage.deleteRecursively()}
    }}
    fun replaceImage(uri:Uri,layerId:String,ownerId:String=activeProjectId){work(tr("Смена изображения","Change image")){check->
        require(activeProjectId==ownerId){tr("Проект изменился","The active project changed")};flushSaves()
        val directory=projectDirectory;val path="assets/${uid()}.image";val file=File(directory,path).also{it.parentFile!!.mkdirs()};var retained=false
        try{context.contentResolver.openInputStream(uri)!!.use{copyChecked(it,file.outputStream(),check)};ImageRepository(directory,imageCache,check).ensure(path,256)
            withContext(Dispatchers.Main){require(activeProjectId==ownerId);val d=document!!;val layer=d.layer(layerId);require(layer.kind=="image");commit(d.withLayer(layer.copy(asset=path)));retained=true;original=null;home=false}
        }catch(e:Throwable){if(!retained)file.delete();throw e}
    }}
    fun importFont(uri:Uri,layerId:String){val ownerId=activeProjectId;val directory=projectDirectory;work(tr("Импорт шрифта","Import font")){check->
        val path="assets/${uid()}.ttf";val file=File(directory,path);var retained=false
        try{context.contentResolver.openInputStream(uri)!!.use{copyChecked(it,file.outputStream(),check)};require(file.length() in 1..32_000_000){"Font exceeds 32 MB"};Typeface.createFromFile(file)
            withContext(Dispatchers.Main){require(activeProjectId==ownerId);val l=document!!.layer(layerId);updateLayer(l.copy(text=l.text.copy(font=path)));retained=true}
        }catch(e:Throwable){if(!retained)file.delete();throw e}
    }}
    fun openProject(uri:Uri){work(tr("Открытие проекта","Open project")){check->
        flushSaves();val opened=context.contentResolver.openInputStream(uri)!!.use{library.importProject(it,check)};library.setActive(opened.entry.id);val entries=library.list()
        withContext(Dispatchers.Main){activate(opened.entry.id,opened.directory,opened.document);projectEntries=entries}
    }}
    fun saveProject(uri:Uri,projectId:String=activeProjectId){if(projectId.isEmpty())return;work(tr("Сохранение проекта","Save project")){check->
        flushSaves()
        val temp=File(context.cacheDir,"package-${uid()}.sf7blab")
        try{temp.outputStream().use{library.exportProject(projectId,it,check=check)};deliver(temp,uri,check);withContext(Dispatchers.Main){message=tr("Проект сохранён","Project saved")}}finally{temp.delete()}
    }}
    fun export(uri:Uri,width:Int,height:Int,background:Int){val d=document?:return;val directory=projectDirectory;work(tr("Экспорт PNG","Export PNG")){check->
        lastExportBackend=""
        require(width in 1..30000&&height in 1..30000&&width.toLong()*height<=240_000_000){"Invalid export size"}
        val work=File(context.cacheDir,"render-${uid()}");val temp=File(context.cacheDir,"output-${uid()}.png")
        val estimate=d.width.toLong()*d.height*4*(d.nodes.size+12L)
        require(context.cacheDir.usableSpace>estimate+64L*1024*1024){tr("Недостаточно места для полноразмерного рендера. Освободите место.","Not enough storage for full-resolution rendering. Free some space.")}
        try{renderMutex.withLock{SurfacePool(work,memoryBytes=64*1024*1024,persistOnClose=false).use{pool->val source=AndroidSources(directory,pool,ImageRepository(directory,imageCache,check),check);val gpu=if(GpuRenderer.canAccelerate(d))GpuRenderer.tryRender(pool,source,d,d.width,d.height,check)else null;lastExportBackend=if(gpu!=null)"GPU"else"CPU";val surface=gpu?.also{progress=.8f}?:Renderer(pool,source,check){progress=it*.8f}.render(d,d.width,d.height);temp.outputStream().use{PngWriter.write(it,width,height,{y->outputRow(d,surface,y,width,height,background)},check){p->progress=.8f+p*.18f}}}}
            deliver(temp,uri,check);withContext(Dispatchers.Main){message=tr("PNG сохранён: ","PNG saved: ")+"$width × $height"}
        }finally{work.deleteRecursively();temp.delete()}
    }}
    private suspend fun renderPreview(d:Document,directory:File=projectDirectory):Bitmap {
        val start=SystemClock.elapsedRealtime();val scale=min(1f,quality.toFloat()/max(d.width,d.height));val w=max(1,(d.width*scale).roundToInt());val h=max(1,(d.height*scale).roundToInt());val ctx=currentCoroutineContext();val check={ctx.ensureActive()}
        val work=File(context.cacheDir,"preview-${uid()}");val cache=NodeRasterCache(File(context.cacheDir,"node-rasters"),directory.canonicalPath)
        try{return SurfacePool(work,memoryBytes=48*1024*1024,persistOnClose=false).use{pool->val source=AndroidSources(directory,pool,ImageRepository(directory,imageCache,check),check);val gpu=if(GpuRenderer.canAccelerate(d))GpuRenderer.tryRender(pool,source,d,w,h,check)else null;val surface=gpu?:Renderer(pool,source,check,cache=cache).render(d,w,h);val outScale=min(1f,quality.toFloat()/max(d.outputWidth,d.outputHeight));val ow=max(1,(d.outputWidth*outScale).roundToInt());val oh=max(1,(d.outputHeight*outScale).roundToInt());val bmp=Bitmap.createBitmap(ow,oh,Bitmap.Config.ARGB_8888)
                try{for(y in 0 until oh){check();val row=outputRow(d,surface,y,ow,oh);for(x in row.indices)row[x]=Pixels.straight(row[x]);bmp.setPixels(row,0,ow,0,y,ow,1)};lastTiming=tr("Предпросмотр: ","Preview: ")+"${(SystemClock.elapsedRealtime()-start)/1000f} s";bmp}catch(e:Throwable){bmp.recycle();throw e}
            }}finally{work.deleteRecursively();cache.trim()}
    }
    suspend fun thumbnail(effect:Effect?,layerId:String=activeLayer,nodeId:String=""):Bitmap? {
        val d=document?:return null;val directory=projectDirectory;val layer=d.layers.firstOrNull{it.id==layerId}?:return null
        val smallDoc=if(nodeId.isNotEmpty())d.copy(root=nodeId)else if(effect==null)d.copy(root="output-${layer.id}")else d.addEffect(layer.id,effect)
        return withContext(Dispatchers.Default){thumbnailMutex.withLock{while(rendering)delay(50);val ctx=currentCoroutineContext();val check={ctx.ensureActive()};val work=File(context.cacheDir,"thumb-${uid()}")
            try{SurfacePool(work,memoryBytes=2*1024*1024,persistOnClose=false).use{pool->val scale=64f/max(d.width,d.height);val w=max(1,(d.width*scale).roundToInt());val h=max(1,(d.height*scale).roundToInt());val surface=Renderer(pool,AndroidSources(directory,pool,ImageRepository(directory,imageCache,check),check),check).render(smallDoc,w,h);val pixels=IntArray(w*h);for(y in 0 until h){check();val row=surface.readRow(y);for(x in row.indices)pixels[y*w+x]=Pixels.straight(row[x])};Bitmap.createBitmap(pixels,w,h,Bitmap.Config.ARGB_8888)}}finally{work.deleteRecursively()}
        }}
    }
    private fun deliver(file:File,uri:Uri,check:()->Unit){try{context.contentResolver.openOutputStream(uri,"wt")!!.use{copyChecked(file.inputStream(),it,check)}}catch(e:Throwable){runCatching{DocumentsContract.deleteDocument(context.contentResolver,uri)};throw e}}
    private fun copyChecked(input:InputStream,output:OutputStream,check:()->Unit){input.use{src->output.use{out->val buf=ByteArray(65536);while(true){check();val n=src.read(buf);if(n<0)break;out.write(buf,0,n)};out.flush()}}}
    private fun loadPresets():List<Preset>{val f=File(context.filesDir,"presets.json");return if(f.exists())Codec.json.decodeFromString<List<Preset>>(f.readText()).onEach{it.validate()}else emptyList()}
    fun savePreset(p:Preset){try{p.validate();replacePresets(presets+p)}catch(e:Exception){message=e.message}}
    fun replacePresets(ps:List<Preset>){presets=ps;viewModelScope.launch(Dispatchers.IO){try{presetMutex.withLock{val f=File(context.filesDir,"presets.json");val t=File(context.filesDir,"presets.tmp");t.writeText(Codec.json.encodeToString(ps));atomicReplace(t,f)}}catch(e:Exception){withContext(Dispatchers.Main){message=e.message}}}}
    fun presetFromLayer(name:String):Preset?{val d=document?:return null;var head=d.layer(activeLayer).head;val list=mutableListOf<Effect>();val nodes=d.nodes.associateBy{it.id};while(true){val n=nodes[head]?:break;require(n.kind!="blend"&&n.maskInput.isEmpty()&&n.inputs.size<=1){tr("Для ветвей и отдельных входов сохраните проект .sf7blab. Набор сохраняет линейную цепочку.","Save a .sf7blab project for branches and external inputs. Presets store a linear chain.")};if(n.kind!="effect")break;list.add(0,n.effect);head=n.inputs[0]};return list.takeIf{it.isNotEmpty()}?.let{Preset(name=name,effects=it)}}
    fun applyPreset(p:Preset){val d=document?:return;var next=d;for(e in p.effects)next=next.addEffect(activeLayer,e);commit(next)}
    fun importPreset(uri:Uri){work(tr("Импорт набора","Import preset")){check->val text=context.contentResolver.openInputStream(uri)!!.bufferedReader().use{reader->val result=StringBuilder();val chars=CharArray(4096);while(true){check();val n=reader.read(chars);if(n<0)break;require(result.length+n<=4*1024*1024){"Preset too large"};result.append(chars,0,n)};result.toString()};val p=Codec.json.decodeFromString<Preset>(text).also{it.validate()};withContext(Dispatchers.Main){savePreset(p)}}}
    fun exportPreset(p:Preset,uri:Uri){work(tr("Экспорт набора","Export preset")){_->context.contentResolver.openOutputStream(uri,"wt")!!.bufferedWriter().use{it.write(Codec.json.encodeToString(p))};withContext(Dispatchers.Main){message=tr("Набор сохранён","Preset saved")}}}
}
