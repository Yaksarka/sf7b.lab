package lab.sf7b

import android.graphics.Bitmap
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import lab.sf7b.core.*
import kotlinx.coroutines.CancellationException
import kotlin.math.*

@Composable fun EffectsBrowser(model:EditorModel,initialCategory:Int=0,onChoose:(String)->Unit){var query by remember{mutableStateOf("")};var category by remember(initialCategory){mutableIntStateOf(initialCategory)}
    val keyboard=LocalSoftwareKeyboardController.current;val focus=LocalFocusManager.current
    if(model.document?.layers?.none{it.id==model.activeLayer}!=false){Column{PanelTitle(model.tr("Эффекты","Effects"));Text(model.tr("Сначала добавьте или выберите слой во вкладке «Слои».","First add or select a layer in Layers."),Modifier.padding(16.dp))};return}
    Column(Modifier.fillMaxSize()) {PanelTitle(model.tr("Эффекты","Effects"))
        OutlinedTextField(query,{query=it},modifier=Modifier.fillMaxWidth().padding(horizontal=16.dp),label={Text(model.tr("Поиск на русском и английском","Search Russian and English"))},singleLine=true,keyboardOptions=KeyboardOptions(imeAction=ImeAction.Search),keyboardActions=KeyboardActions(onSearch={keyboard?.hide();focus.clearFocus()}))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(8.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){val categories=if(model.language=="ru")Catalog.categoriesRu else Catalog.categoriesEn;categories.forEachIndexed{i,label->FilterChip(category==i,{category=i},label={Text(label)})}}
        val list=Catalog.effects.filter{(category==0||it.category==category||(category==-1&&it.category in 2..3))&&(query.isBlank()||it.ru.contains(query,true)||it.en.contains(query,true)||it.id.contains(query,true))}
        if(list.isEmpty())Text(model.tr("Ничего не найдено","No effects found"),Modifier.padding(24.dp))
        LazyVerticalGrid(columns=GridCells.Adaptive(112.dp),modifier=Modifier.fillMaxWidth().weight(1f),contentPadding=PaddingValues(12.dp),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            items(list,key={it.id}){definition->
                val thumbnail by produceState<Bitmap?>(null,definition.id,model.document,model.activeLayer){try{value=model.thumbnail(Catalog.newEffect(definition.id))}catch(e:CancellationException){throw e}catch(_:Exception){}}
                Surface(shape=RoundedCornerShape(10.dp),color=MaterialTheme.colorScheme.surfaceContainer,modifier=Modifier.testTag("effect:${definition.id}").clickable{keyboard?.hide();focus.clearFocus();onChoose(definition.id)}){Column{
                    Box(Modifier.fillMaxWidth().aspectRatio(1f),contentAlignment=Alignment.Center){if(thumbnail!=null)Image(thumbnail!!.asImageBitmap(),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)else Text("·",fontSize=28.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
                    Text(definition.en,Modifier.padding(8.dp),fontSize=13.sp,minLines=2,maxLines=2)
                }}
            }
        }
    }
}
@Composable fun EffectPanel(model:EditorModel,newId:String?,nodeId:String?,onClose:()->Unit,onMask:(String)->Unit,onCommands:((EffectCommands?)->Unit)?=null){
    val d=model.document?:return;val node=d.nodes.find{it.id==nodeId};val layer=d.layers.find{it.id==(node?.layer?:model.activeLayer)}?:return
    var effect by remember(nodeId,newId){mutableStateOf(node?.effect?:Catalog.newEffect(newId!!))};val draftId=remember{uid()};val def=Catalog.definition(effect)
    var mapInput by remember(nodeId){mutableStateOf(node?.inputs?.getOrNull(1)?:"")};var maskInput by remember(nodeId){mutableStateOf(node?.maskInput?:"")}
    val supportsReference=effect.id=="displacement"||(effect.id=="low_freq_banding"&&effect.algorithmVersion==2)
    fun draft():Document {val inputs=listOf(node?.inputs?.first()?:layer.head)+listOfNotNull(mapInput.takeIf{it.isNotEmpty()&&supportsReference});return if(node!=null)d.copy(nodes=d.nodes.map{if(it.id==node.id)it.copy(effect=effect,inputs=inputs,maskInput=maskInput)else it})else d.copy(nodes=d.nodes+Node(id=draftId,inputs=inputs,layer=layer.id,effect=effect,maskInput=maskInput),layers=d.layers.map{if(it.id==layer.id)it.copy(head=draftId)else it}).stack()}
    fun change(e:Effect){effect=e;model.preview(draft())}
    LaunchedEffect(Unit){model.preview(draft())}
    LaunchedEffect(effect,mapInput,maskInput){onCommands?.invoke(EffectCommands(def.en,{model.commit(draft());onClose()},if(def.random)({change(effect.copy(seed=java.security.SecureRandom().nextLong()))})else null))}
    DisposableEffect(Unit){onDispose{onCommands?.invoke(null)}}
    Column(Modifier.verticalScroll(rememberScrollState())){
        PanelTitle(def.en)
        ToggleRow(model.tr("Включён","Enabled"),effect.enabled,{change(effect.copy(enabled=it))})
        val map=d.nodes.associateBy{it.id}
        fun depends(id:String,target:String):Boolean {if(id==target)return true;val n=map[id]?:return false;return(n.inputs+listOfNotNull(n.maskInput.takeIf{it.isNotEmpty()})).any{depends(it,target)}}
        val options=d.nodes.filter{(!it.automatic||it.kind=="layer")&&if(node==null)!depends(it.id,"output-${layer.id}")else !depends(it.id,node.id)}
        fun label(n:Node)=when(n.kind){"source","layer"->d.layer(n.layer).name;"mask"->model.tr("Маска","Mask");"blend"->n.blend;else->Catalog.byId(n.effect.id).en}+" · "+n.id.takeLast(4)
        if(supportsReference){val labels=listOf(if(effect.id=="displacement")model.tr("Процедурная карта","Procedural map")else model.tr("Текущее изображение","Current image"))+options.map{label(it)};ChoiceRow(if(effect.id=="displacement")model.tr("Карта смещения","Displacement map")else model.tr("Второй источник","Reference source"),options.find{it.id==mapInput}?.let{label(it)}?:labels[0],labels){chosen->mapInput=options.find{label(it)==chosen}?.id?:"";model.preview(draft())}}
        if(options.any{it.kind=="mask"}){val masks=options.filter{it.kind=="mask"};val labels=listOf(model.tr("Без узла маски","No mask node"))+masks.map{label(it)};ChoiceRow(model.tr("Вход маски","Mask input"),masks.find{it.id==maskInput}?.let{label(it)}?:labels[0],labels){chosen->maskInput=masks.find{label(it)==chosen}?.id?:"";model.preview(draft())}}
        TextButton(onClick={change(Effect(effect.id,seed=effect.seed,mask=effect.mask,algorithmVersion=effect.algorithmVersion))},modifier=Modifier.padding(horizontal=8.dp)){Text(model.tr("Сбросить эффект","Reset effect"))}
        def.params.forEach{p->
            val label=if(model.language=="ru")p.ru else p.en
            if(p.choices.isNotEmpty())ChoiceRow(label,p.choices[effect.p(p.id).roundToInt().coerceIn(0,p.choices.lastIndex)],p.choices){choice->change(effect.copy(params=effect.params+(p.id to p.choices.indexOf(choice).toFloat())))}
            else NumberControl(label,effect.p(p.id),p.min,p.max,{value->change(effect.copy(params=effect.params+(p.id to value)))},p.integral,p.unit,p.default,model)
        }
        NumberControl(model.tr("Смешивание с исходником","Mix with input"),effect.mix,0f,1f,{change(effect.copy(mix=it))},default=1f,model=model)
        if(def.random){var seedText by remember(effect.seed){mutableStateOf(effect.seed.toString())};Row(Modifier.fillMaxWidth().padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically){OutlinedTextField(seedText,{seedText=it;it.toLongOrNull()?.let{v->change(effect.copy(seed=v))}},Modifier.weight(1f),label={Text("Seed")},singleLine=true,isError=seedText.toLongOrNull()==null);TextButton(onClick={change(effect.copy(seed=java.security.SecureRandom().nextLong()))}){Text(model.tr("Другой вариант","New variant"))}}}
        if(effect.id=="color_swap"){
            ColorControl(model.tr("Исходный цвет","Source color"),effect.palette[0],{change(effect.copy(palette=listOf(it,effect.palette[1])))},model)
            ColorControl(model.tr("Цвет замены","Replacement color"),effect.palette[1],{change(effect.copy(palette=listOf(effect.palette[0],it)))},model)
        }else if(effect.id in listOf("gradient_map","palette","linear_gradient","radial_gradient","perlin","voronoi","stripes","checker")||(effect.algorithmVersion==2&&effect.id in listOf("patchwork_break","random_pixelate")))PalettePanel(effect.palette,{change(effect.copy(palette=it))},model)
        if(effect.id=="curves")CurvesPanel(effect.curves,{change(effect.copy(curves=it))},model)
        TextButton(onClick={val next=draft();model.commit(next);onMask(node?.id?:draftId)},modifier=Modifier.fillMaxWidth()){Text(model.tr("Применить и открыть маску","Apply and open mask"))}
        ApplyRow(model,onClose,{model.commit(draft());onClose()})
    }
}
@Composable fun PalettePanel(colors:List<Int>,onChange:(List<Int>)->Unit,model:EditorModel){
    Text(model.tr("Редактируемая палитра","Editable palette"),Modifier.padding(16.dp),fontSize=16.sp)
    colors.forEachIndexed{index,c->Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.weight(1f)){ColorControl("${index+1}",c,{v->onChange(colors.mapIndexed{i,old->if(i==index)v else old})},model)};TextButton(enabled=colors.size>2,onClick={onChange(colors.filterIndexed{i,_->i!=index})}){Text("−")}}}
    TextButton(enabled=colors.size<32,onClick={onChange(colors+0xffffffff.toInt())},modifier=Modifier.fillMaxWidth()){Text(model.tr("Добавить цвет","Add color"))}
}
@Composable fun CurvesPanel(curves:List<List<Point>>,onChange:(List<List<Point>>)->Unit,model:EditorModel){var channel by remember{mutableIntStateOf(0)};var selected by remember{mutableIntStateOf(0)};val c=curves[channel];val accent=MaterialTheme.colorScheme.primary
    val latestCurves by rememberUpdatedState(curves);val latestOnChange by rememberUpdatedState(onChange)
    LaunchedEffect(channel,c.size){selected=selected.coerceIn(0,c.lastIndex)}
    Row(Modifier.padding(horizontal=16.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("RGB","R","G","B").forEachIndexed{i,label->FilterChip(channel==i,{channel=i;selected=0},label={Text(label)})}}
    fun update(points:List<Point>){onChange(curves.mapIndexed{i,old->if(i==channel)points else old})}
    Canvas(Modifier.fillMaxWidth().height(180.dp).padding(16.dp).background(MaterialTheme.colorScheme.surfaceContainer).pointerInput(channel){detectDragGestures(onDragStart={p->val points=latestCurves[channel];selected=points.indices.minBy{index->hypot(p.x/size.width-points[index].x,1-p.y/size.height-points[index].y)}}){event,_->event.consume();val points=latestCurves[channel];val gap=if(selected>0&&selected<points.lastIndex)min(.001f,(points[selected+1].x-points[selected-1].x)/4)else .001f;val low=if(selected==0)0f else points[selected-1].x+gap;val high=if(selected==points.lastIndex)1f else points[selected+1].x-gap;val x=if(selected==0)0f else if(selected==points.lastIndex)1f else (event.position.x/size.width).coerceIn(low,high);val y=(1-event.position.y/size.height).coerceIn(0f,1f);val next=points.mapIndexed{i,p->if(i==selected)Point(x,y)else p};latestOnChange(latestCurves.mapIndexed{i,old->if(i==channel)next else old})}}){
        for(i in 0..4){drawLine(Color.Gray.copy(alpha=.3f),Offset(size.width*i/4,0f),Offset(size.width*i/4,size.height));drawLine(Color.Gray.copy(alpha=.3f),Offset(0f,size.height*i/4),Offset(size.width,size.height*i/4))}
        c.zipWithNext().forEach{(a,b)->drawLine(accent,Offset(a.x*size.width,(1-a.y)*size.height),Offset(b.x*size.width,(1-b.y)*size.height),3f)};c.forEachIndexed{i,p->drawCircle(if(i==selected)Color.White else accent,6f,Offset(p.x*size.width,(1-p.y)*size.height))}
    }
    ChoiceRow(model.tr("Точка","Point"),(selected+1).toString(),c.indices.map{(it+1).toString()}){selected=it.toInt()-1}
    if(selected>0&&selected<c.lastIndex){val gap=min(.001f,(c[selected+1].x-c[selected-1].x)/4);NumberControl("X",c[selected].x,c[selected-1].x+gap,c[selected+1].x-gap,{v->update(c.mapIndexed{i,p->if(i==selected)p.copy(x=v)else p})},model=model)}
    NumberControl("Y",c[selected.coerceAtMost(c.lastIndex)].y,0f,1f,{v->update(c.mapIndexed{i,p->if(i==selected)p.copy(y=v)else p})},model=model)
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center){TextButton(enabled=c.size<32,onClick={val pair=c.zipWithNext().maxBy{it.second.x-it.first.x};val p=Point((pair.first.x+pair.second.x)/2,(pair.first.y+pair.second.y)/2);update((c+p).sortedBy{it.x});selected=(c+p).sortedBy{it.x}.indexOf(p)}){Text(model.tr("Добавить точку","Add point"))};TextButton(enabled=selected>0&&selected<c.lastIndex,onClick={update(c.filterIndexed{i,_->i!=selected});selected=0}){Text(model.tr("Удалить точку","Delete point"))}}
}
@Composable fun ColorPanel(model:EditorModel,onEffect:(String)->Unit,onCrop:()->Unit){Column{PanelTitle(model.tr("Цвет и геометрия","Color and geometry"));listOf("adjust","curves").forEach{id->val def=Catalog.byId(id);TextButton(onClick={onEffect(id)},modifier=Modifier.fillMaxWidth()){Text(def.en)}};TextButton(onClick=onCrop,modifier=Modifier.fillMaxWidth()){Text(model.tr("Обрезка, поворот, отражение","Crop, rotate, flip"))}}}
@Composable fun PresetsPanel(model:EditorModel,actions:UiActions){var save by remember{mutableStateOf(false)};var rename by remember{mutableIntStateOf(-1)}
    Column{PanelTitle(model.tr("Наборы настроек","Presets"),model.tr("Цепочка эффектов активного слоя с параметрами и seed","Active layer's effect chain with parameters and seeds"))
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){TextButton(onClick={save=true}){Text(model.tr("Сохранить цепочку","Save chain"))};TextButton(onClick=actions.importPreset){Text(model.tr("Импорт","Import"))}}
        LazyColumn(Modifier.heightIn(max=460.dp)){items(Catalog.builtinPresets()){p->ListItem(headlineContent={Text(p.name,fontSize=14.sp)},supportingContent={Text(p.effects.joinToString(" → "){Catalog.byId(it.id).en},fontSize=12.sp)},trailingContent={TextButton(onClick={model.applyPreset(p)}){Text(model.tr("Применить","Apply"))}})}
            items(model.presets.size){index->val p=model.presets[index];Column{ListItem(headlineContent={Text(p.name)},trailingContent={TextButton(onClick={model.applyPreset(p)}){Text(model.tr("Применить","Apply"))}});Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End){TextButton(onClick={rename=index}){Text(model.tr("Имя","Rename"))};TextButton(onClick={actions.exportPreset(p)}){Text(model.tr("Экспорт","Export"))};TextButton(onClick={model.replacePresets(model.presets.filterIndexed{i,_->i!=index})}){Text(model.tr("Удалить","Delete"))}}}}
        }
    }
    if(save)TextPrompt(model.tr("Имя набора","Preset name"),"My sf7b",model,{save=false}){name->try{val p=model.presetFromLayer(name);if(p==null)model.message=model.tr("Добавьте хотя бы один эффект в активный слой","Add at least one effect to the active layer")else model.savePreset(p)}catch(e:IllegalArgumentException){model.message=e.message}}
    if(rename in model.presets.indices)TextPrompt(model.tr("Имя набора","Preset name"),model.presets[rename].name,model,{rename=-1}){name->model.replacePresets(model.presets.mapIndexed{i,p->if(i==rename)p.copy(name=name)else p})}
}
