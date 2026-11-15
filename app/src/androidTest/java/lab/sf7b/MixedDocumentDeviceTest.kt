package lab.sf7b

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.SystemClock
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.runBlocking
import lab.sf7b.core.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Assume.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest

class MixedDocumentDeviceTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private val model get()=ViewModelProvider(rule.activity)[EditorModel::class.java]
    private val ownIds=linkedSetOf<String>()
    private val privateRoots=mutableListOf<File>()
    private var retained=false
    private var existingIds=emptySet<String>()
    private var previousProject=""
    private var previousLanguage="ru"
    private var previousTheme="system"
    private var previousQuality=1024
    private var previousHome=true
    private var previousMessage:String?=null
    private var previousPreferences=emptyMap<String,Any?>()
    private var existingManifests=emptyMap<String,Pair<String?,String?>>()

    @Before fun retainUserWorkBeforeCreatingAnyFixture() {
        rule.waitUntil(60000){!model.loading&&!model.working}
        rule.runOnIdle{
            existingIds=model.projectEntries.map{it.id}.toSet();previousProject=model.activeProjectId
            previousLanguage=model.language;previousTheme=model.theme;previousQuality=model.quality;previousHome=model.home;previousMessage=model.message
            previousPreferences=rule.activity.getSharedPreferences("editor",0).all.toMap()
        }
        existingManifests=existingIds.associateWith{id->
            val directory=File(rule.activity.filesDir,"projects/$id")
            hashFile(File(directory,"document.json")) to hashFile(File(directory,"last-good.json"))
        }
        assumeTrue("Cannot safely switch away from an unrestored existing library",previousProject.isNotEmpty()||existingIds.isEmpty())
        if(previousProject.isNotEmpty()){
            val store=ProjectFiles(model.projectDirectory);store.load()
            assumeFalse("Existing recovery work is left untouched; only isolated fixtures may be corrupted",store.recoveredFromBackup)
        }
        retained=true
    }

    @After fun removeOnlyRecordedFixturesAndRestoreTheOriginalSelection() {
        if(!retained)return
        try {
            rule.runOnIdle{model.cancelWork()};rule.waitUntil(60000){!model.working}
            runBlocking{model.flushSaves()}
            if(previousProject.isNotEmpty()&&model.projectEntries.any{it.id==previousProject}) {
                rule.runOnIdle{model.openLibraryProject(previousProject)}
                rule.waitUntil(60000){!model.working&&model.activeProjectId==previousProject}
            }
            for(id in ownIds.filter{it !in existingIds})if(model.projectEntries.any{it.id==id}) {
                rule.runOnIdle{model.deleteProject(id)}
                rule.waitUntil(60000){!model.working&&model.projectEntries.none{it.id==id}}
            }
            for((id,bytes)in existingManifests) {
                val directory=File(rule.activity.filesDir,"projects/$id")
                assertEquals("Existing document changed: $id",bytes.first,hashFile(File(directory,"document.json")))
                assertEquals("Existing recovery copy changed: $id",bytes.second,hashFile(File(directory,"last-good.json")))
            }
        } finally {
            rule.runOnIdle{model.settings(previousLanguage,previousTheme,previousQuality);model.home=previousHome;model.message=previousMessage}
            val preferences=rule.activity.getSharedPreferences("editor",0).edit()
            for(key in listOf("language","theme","quality"))when(val value=previousPreferences[key]){
                null->preferences.remove(key)
                is String->preferences.putString(key,value)
                is Int->preferences.putInt(key,value)
            }
            preferences.commit()
            for(directory in privateRoots){require(directory.canonicalFile.parentFile==rule.activity.cacheDir.canonicalFile&&directory.name.startsWith("mixed-native-"));directory.deleteRecursively()}
        }
    }

    private fun privateRoot():File=File(rule.activity.cacheDir,"mixed-native-${uid()}").also{require(it.mkdir());privateRoots+=it}
    private fun hashFile(file:File):String? {
        if(!file.isFile)return null
        val hash=MessageDigest.getInstance("SHA-256")
        file.inputStream().use{input->val buffer=ByteArray(65536);while(true){val count=input.read(buffer);if(count<0)break;hash.update(buffer,0,count)}}
        return hash.digest().joinToString(""){"%02x".format(it.toInt()and 255)}
    }
    private fun fixtureResources(directory:File) {
        File(directory,"assets").mkdirs()
        val image=Bitmap.createBitmap(64,48,Bitmap.Config.ARGB_8888)
        try {
            val pixels=IntArray(64*48){i->val x=i%64;val y=i/64;val alpha=if(x<8)0 else if(x<30)128 else 255
                (alpha shl 24)or((x*255/63)shl 16)or((y*255/47)shl 8)or(if((x/8+y/8)%2==0)224 else 32)}
            image.setPixels(pixels,0,64,0,0,64,48)
            File(directory,"assets/image.png").outputStream().use{assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,it))}
        }finally{image.recycle()}
        val font=File("/system/fonts").listFiles().orEmpty().firstOrNull{it.extension=="ttf"}?:error("No system TTF available for isolated verification")
        font.copyTo(File(directory,"assets/font.ttf"))
    }
    private fun mixedDocument():Document {
        var doc=Document(name="Mixed native verification",width=192,height=144)
        for(i in 0 until 15) {
            val kind=listOf("image","generator","text","drawing","solid")[i%5]
            val layer=Layer(name="$kind ${i/5+1}",kind=kind,asset=if(kind=="image")"assets/image.png"else"",color=0x30206090,
                generator=Effect("linear_gradient",mapOf("angle" to(i*11f)),palette=listOf(0x804040e0.toInt(),0x80e08020.toInt())),
                text=TextData(text="Привет ${i/5+1}\nsf7b",font=if(kind=="text")"assets/font.ttf"else"",size=.09f,color=0xc0f0e0d0.toInt(),outline=.003f,shadow=.002f),
                strokes=if(kind=="drawing")listOf(Stroke(listOf(Point(.12f,.2f),Point(.8f,.72f)),color=0xa0e03080.toInt(),size=.09f,softness=.4f),Stroke(listOf(Point(.45f,.4f),Point(.55f,.5f)),size=.04f,erase=true))else emptyList(),
                transform=Transform(x=(i%3-1)*.04f,y=(i%2)*.025f,scale=.85f,angle=(i%3-1)*7f),opacity=.55f,blend=BLENDS[i%BLENDS.size],
                mask=Mask(base=.82f,strokes=if(i%2==0)listOf(Stroke(listOf(Point(.2f,.3f),Point(.7f,.7f)),size=.07f,erase=true))else emptyList()))
            doc=doc.addLayer(layer)
            when(i){
                0->doc=doc.addEffect(layer.id,Effect("adjust",mapOf("brightness" to .06f,"contrast" to .1f),seed=501,mask=Mask(base=.7f)))
                5->doc=doc.addEffect(layer.id,Catalog.newEffect("random_pixelate").copy(params=mapOf("depth" to 3f,"regularity" to 55f),seed=502,mask=Mask(base=.6f)))
                10->doc=doc.addEffect(layer.id,Effect("rgb_split",mapOf("amount" to .015f,"angle" to 20f),seed=503))
            }
        }
        val first=doc.layers.first();val originalHead=first.head
        doc=doc.addEffect(first.id,Effect("invert",seed=504),"source-${first.id}")
        doc=doc.branch(first.id,originalHead,doc.layer(first.id).head,"Screen")
        return doc.copy(frame=Frame(.1f,.05f,.8f,.9f,turn=1)).also{it.validate()}
    }
    private data class Rendered(val pixels:IntArray,val png:ByteArray,val milliseconds:Long)
    private fun render(directory:File,doc:Document,work:File):Rendered {
        val started=SystemClock.elapsedRealtime()
        return SurfacePool(work,memoryBytes=32*1024*1024,persistOnClose=false).use{pool->
            val source=AndroidSources(directory,pool,ImageRepository(directory,File(work,"decode")))
            val surface=Renderer(pool,source).render(doc,doc.width,doc.height)
            val pixels=IntArray(doc.outputWidth*doc.outputHeight)
            for(y in 0 until doc.outputHeight)outputRow(doc,surface,y,doc.outputWidth,doc.outputHeight).copyInto(pixels,y*doc.outputWidth)
            val png=ByteArrayOutputStream();PngWriter.write(png,doc.outputWidth,doc.outputHeight,{y->pixels.copyOfRange(y*doc.outputWidth,(y+1)*doc.outputWidth)})
            Rendered(pixels,png.toByteArray(),SystemClock.elapsedRealtime()-started)
        }
    }

    @Test fun fifteenMixedLayersKeepTheirEditableResourcesMasksSeedsAndRenderedPngAcrossPortableCopies() {
        val root=privateRoot();val input=File(root,"input");fixtureResources(input)
        val doc=mixedDocument();assertEquals(15,doc.layers.size)
        for(kind in listOf("image","generator","text","drawing","solid"))assertEquals(3,doc.layers.count{it.kind==kind})
        val library=lab.sf7b.core.ProjectLibrary(File(root,"library"))
        val original=library.create(doc,input);val opened=library.open(original.id)
        val imageHash=hashFile(File(input,"assets/image.png"));val fontHash=hashFile(File(input,"assets/font.ttf"))
        assertTrue(input.deleteRecursively());assertFalse(input.exists())
        val before=render(opened.directory,opened.document,File(root,"render-original"))
        val copy=library.duplicate(original.id)
        val copyDoc=library.open(copy.id).document
        val edited=copyDoc.copy(nodes=copyDoc.nodes.map{if(it.kind=="effect"&&it.effect.id=="random_pixelate")it.copy(effect=it.effect.copy(seed=99991))else it})
        library.save(copy.id,edited)
        assertEquals(502L,library.open(original.id).document.nodes.first{it.kind=="effect"&&it.effect.id=="random_pixelate"}.effect.seed)
        assertEquals(99991L,library.open(copy.id).document.nodes.first{it.kind=="effect"&&it.effect.id=="random_pixelate"}.effect.seed)
        val packed=ByteArrayOutputStream();library.exportProject(original.id,packed)
        val secondPhone=lab.sf7b.core.ProjectLibrary(File(root,"other-installation"))
        val imported=secondPhone.importProject(packed.toByteArray().inputStream())
        library.delete(original.id)
        assertEquals(imageHash,hashFile(File(imported.directory,"assets/image.png")))
        assertEquals(fontHash,hashFile(File(imported.directory,"assets/font.ttf")))
        val afterProcessRestart=lab.sf7b.core.ProjectLibrary(File(root,"other-installation")).open(imported.entry.id)
        assertEquals(doc,afterProcessRestart.document)
        val after=render(afterProcessRestart.directory,afterProcessRestart.document,File(root,"render-imported"))
        assertArrayEquals(before.pixels,after.pixels);assertArrayEquals(before.png,after.png)
        assertTrue(before.pixels.any{Pixels.a(it)in 1..254});assertTrue(before.pixels.distinct().size>20)
        val bitmap=BitmapFactory.decodeByteArray(after.png,0,after.png.size)?:error("PNG decode failed")
        try{assertEquals(doc.outputWidth,bitmap.width);assertEquals(doc.outputHeight,bitmap.height);assertTrue(bitmap.hasAlpha())}finally{bitmap.recycle()}
        val reports=File(rule.activity.filesDir,"test-results").also{it.mkdirs()}
        File(reports,"mixed-document-15-layers.txt").writeText("canvas=192x144\noutput=${doc.outputWidth}x${doc.outputHeight}\nlayers=15 (3 image, 3 generator, 3 text, 3 drawing, 3 solid)\nnodes=${doc.nodes.size}\nsource_png=64x48 RGBA\nfont=embedded system TTF\nrender=CPU AndroidSources/Renderer\noriginal_ms=${before.milliseconds}\nportable_reopen_ms=${after.milliseconds}\ncomparison=exact premultiplied pixels and PNG bytes\ncopy_edit_isolated=true\nsource_files_deleted_before_render=true\nNot a 15-layer 2000px performance benchmark.\n")
    }

    @Test fun aDamagedOwnedProjectShowsItsLocalizedRecoveryWarningWithoutChangingOtherDocuments() {
        for(language in listOf("ru","en")) {
            rule.runOnIdle{model.settings(lang=language,preview=256)}
            val target=createOwnedCanvas();val targetDirectory=model.projectDirectory
            val safe=model.document!!.copy(name="Recovery ${uid().take(8)}")
            rule.runOnIdle{model.commit(safe)};runBlocking{model.flushSaves()}
            createOwnedCanvas() // The corrupted fixture is no longer active and has no pending saves.
            val files=ProjectFiles(targetDirectory);files.save(safe.copy(name="Interrupted latest change"))
            val current=File(targetDirectory,"document.json")
            if(language=="ru")current.writeText("{truncated")else assertTrue(current.delete())
            rule.runOnIdle{model.openLibraryProject(target)}
            rule.waitUntil(60000){!model.working&&model.activeProjectId==target}
            rule.runOnIdle{assertEquals(safe,model.document)}
            val expected=recoveryNoticeText(RecoveryNotice(RecoveryKind.DOCUMENT_BACKUP,target,safe.name),language)
            rule.waitUntil(5000){rule.onAllNodesWithText(expected).fetchSemanticsNodes().isNotEmpty()}
            rule.onNodeWithText(expected).assertExists()
            // Repeated flushes repair current, retain the recovery bytes and do not re-notify.
            runBlocking{model.flushSaves()}
            assertEquals(safe,Codec.decode(File(targetDirectory,"document.json").readText()))
            assertEquals(safe,Codec.decode(File(targetDirectory,"last-good.json").readText()))
            rule.runOnIdle{model.message=null;model.openLibraryProject(target)}
            rule.waitUntil(60000){!model.working&&model.activeProjectId==target}
            rule.runOnIdle{assertNull("An unchanged refresh must not repeat the recovery warning",model.message)}
        }
    }
    private fun createOwnedCanvas():String {
        val previous=model.activeProjectId
        rule.runOnIdle{model.newCanvas(32,24,0x40205080)}
        rule.waitUntil(60000){!model.working&&model.activeProjectId.isNotEmpty()&&model.activeProjectId!=previous}
        val id=model.activeProjectId;require(id !in existingIds);ownIds+=id
        return id
    }
}
