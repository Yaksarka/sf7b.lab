package lab.sf7b

import android.graphics.Typeface
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.core.view.WindowCompat
import lab.sf7b.core.*


data class UiActions(val openImage:(Boolean)->Unit,val openProject:()->Unit,val saveProject:()->Unit,val export:(Int,Int,Int)->Unit,val importFont:(String)->Unit,val importPreset:()->Unit,val exportPreset:(Preset)->Unit,val exportLibraryProject:(String)->Unit,val replaceImage:(String)->Unit)
private val dark=darkColorScheme(primary=Color(0xff64d7e9),onPrimary=Color(0xff00343d),primaryContainer=Color(0xff174652),onPrimaryContainer=Color(0xffe0f8fc),background=Color(0xff101518),surface=Color(0xff192126),surfaceContainer=Color(0xff273138),onSurface=Color(0xffeff4f5),onBackground=Color(0xffeff4f5),onSurfaceVariant=Color(0xffbac8ce),outline=Color(0xff83949b),outlineVariant=Color(0xff3d4c54),error=Color(0xffffb4ab))
private val light=lightColorScheme(primary=Color(0xff006d83),onPrimary=Color.White,primaryContainer=Color(0xffc9eff6),onPrimaryContainer=Color(0xff143b45),background=Color(0xfff7f9fa),surface=Color.White,surfaceContainer=Color(0xffedf1f3),onSurface=Color(0xff18252b),onBackground=Color(0xff18252b),onSurfaceVariant=Color(0xff52636c),outline=Color(0xff798b94),outlineVariant=Color(0xffd0dce1),error=Color(0xffba1a1a))

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun EditorApp(model:EditorModel){
    val isDark=when(model.theme){"dark"->true;"light"->false;else->isSystemInDarkTheme()}
    val view=LocalView.current
    SideEffect{(view.context as? android.app.Activity)?.let{activity->WindowCompat.getInsetsController(activity.window,view).apply{isAppearanceLightStatusBars=!isDark;isAppearanceLightNavigationBars=!isDark}}}
    MaterialTheme(colorScheme=if(isDark)dark else light){
        var panel by rememberSaveable{mutableStateOf("")};var newCanvas by remember{mutableStateOf(false)}
        var expanded by rememberSaveable{mutableStateOf(false)};var collapsed by rememberSaveable{mutableStateOf(false)}
        var commands by remember{mutableStateOf<EffectCommands?>(null)}
        var projectExportId by rememberSaveable{mutableStateOf<String?>(null)}
        var asLayer by rememberSaveable{mutableStateOf(false)};var fontLayer by rememberSaveable{mutableStateOf("")}
        var replacementLayer by rememberSaveable{mutableStateOf("")};var replacementOwner by rememberSaveable{mutableStateOf("")}
        var exportSize by remember{mutableStateOf(Triple(1,1,0))};var presetExport by remember{mutableStateOf<Preset?>(null)}
        var paintTarget by rememberSaveable{mutableStateOf("")};var brushSize by rememberSaveable{mutableFloatStateOf(.025f)};var brushSoft by rememberSaveable{mutableFloatStateOf(.3f)};var brushOpacity by rememberSaveable{mutableFloatStateOf(1f)};var brushColor by rememberSaveable{mutableIntStateOf(-1)};var eraser by rememberSaveable{mutableStateOf(false)};var panMode by rememberSaveable{mutableStateOf(false)}
        val imagePicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null){if(replacementLayer.isNotEmpty())model.replaceImage(uri,replacementLayer,replacementOwner)else model.importImage(uri,asLayer)};replacementLayer="";replacementOwner=""}
        val projectPicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)model.openProject(uri)}
        val fontPicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)model.importFont(uri,fontLayer)}
        val projectSave=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")){uri->if(uri!=null)model.saveProject(uri,projectExportId?:model.activeProjectId);projectExportId=null}
        val pngSave=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")){uri->if(uri!=null)model.export(uri,exportSize.first,exportSize.second,exportSize.third)}
        val presetSave=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")){uri->if(uri!=null)presetExport?.let{model.exportPreset(it,uri)}}
        val presetOpen=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)model.importPreset(uri)}
        val actions=UiActions(
            openImage={layer->asLayer=layer;replacementLayer="";imagePicker.launch(arrayOf("image/jpeg","image/png","image/webp"))},
            openProject={projectPicker.launch(arrayOf("application/octet-stream","application/zip","*/*"))},
            saveProject={projectExportId=model.activeProjectId;projectSave.launch("${model.document?.name?:"sf7b"}.sf7blab")},
            export={w,h,bg->exportSize=Triple(w,h,bg);panel="";pngSave.launch("sf7b-${w}x$h.png")},
            importFont={id->fontLayer=id;fontPicker.launch(arrayOf("font/ttf","application/x-font-ttf","application/octet-stream","*/*"))},
            importPreset={presetOpen.launch(arrayOf("application/json","*/*"))},
            exportPreset={p->presetExport=p;presetSave.launch("sf7b-preset.json")},
            exportLibraryProject={id->projectExportId=id;projectSave.launch("${model.projectEntries.find{it.id==id}?.name?:"sf7b"}.sf7blab")},
            replaceImage={id->replacementLayer=id;replacementOwner=model.activeProjectId;imagePicker.launch(arrayOf("image/jpeg","image/png","image/webp"))}
        )
        val snackbar=remember{SnackbarHostState()}
        val closePanel:()->Unit={model.cancelPreview();panel=""}
        fun draw(){val d=model.document?:return;val layer=d.layers.find{it.id==model.activeLayer&&it.kind=="drawing"}?:d.layers.lastOrNull{it.kind=="drawing"}?:Layer(name=model.tr("Рисунок","Drawing"),kind="drawing").also{model.addLayer(it)};model.activeLayer=layer.id;paintTarget="draw:${layer.id}";eraser=false;panel=""}
        fun editNode(id:String){model.cancelPreview();val node=model.document?.nodes?.find{it.id==id}?:return;panel=when(node.kind){"source"->"layer:${node.layer}";"mask"->"graph-mask:$id";"effect"->"node:$id";else->"tree"}}
        @Composable fun PanelBody(){key(panel,model.activeProjectId){when {
            panel=="settings"->SettingsPanel(model,actions,closePanel)
            panel=="effects"||panel.startsWith("effects@")->EffectsBrowser(model,panel.substringAfter('@',"0").toIntOrNull()?:0){panel="effect:$it"}
            panel.startsWith("effect:")->EffectPanel(model,panel.removePrefix("effect:"),null,closePanel,{id->panel="mask:$id"},{commands=it})
            panel.startsWith("node:")->EffectPanel(model,null,panel.removePrefix("node:"),closePanel,{id->panel="mask:$id"},{commands=it})
            panel=="color"->ColorPanel(model,{panel="effect:$it"},{panel="crop"})
            panel=="layers"->LayersPanel(model,actions,{panel="layer:$it"},{panel="tree"})
            panel.startsWith("layer:")->LayerPanel(model,panel.removePrefix("layer:"),closePanel,{panel="mask-layer:$it"})
            panel=="tree"->TreePanel(model,{editNode(it)})
            panel=="text"->TextPanel(model,actions,closePanel)
            panel=="crop"->CropPanel(model,closePanel)
            panel=="export"->ExportPanel(model,actions)
            panel=="presets"->PresetsPanel(model,actions)
            panel=="random"->RandomModePanel(model,closePanel)
            panel.startsWith("mask-layer:")->MaskPanel(model,panel.removePrefix("mask-layer:"),false,closePanel){id->paintTarget="layer:$id";eraser=true;panel=""}
            panel.startsWith("mask:")->MaskPanel(model,panel.removePrefix("mask:"),true,closePanel){id->paintTarget="node:$id";eraser=true;panel=""}
            panel.startsWith("graph-mask:")->MaskPanel(model,panel.removePrefix("graph-mask:"),true,closePanel){id->paintTarget="graph:$id";eraser=true;panel=""}
        }}}
        LaunchedEffect(model.message){model.message?.let{snackbar.showSnackbar(it);model.message=null}}
        LaunchedEffect(model.activeProjectId){panel="";paintTarget="";expanded=false;collapsed=false}
        LaunchedEffect(panel){if(!panel.startsWith("effect:")&&!panel.startsWith("node:"))commands=null;collapsed=false}
        BackHandler(!model.home||panel.isNotEmpty()||paintTarget.isNotEmpty()){when{panel.isNotEmpty()->closePanel();paintTarget.isNotEmpty()->paintTarget="";else->model.home=true}}
        Scaffold(snackbarHost={SnackbarHost(snackbar)},containerColor=MaterialTheme.colorScheme.background){padding->
            Column(Modifier.fillMaxSize().padding(padding).imePadding()){
                if(model.loading)Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){CircularProgressIndicator()}
                else if(model.home)ProjectGallery(model,actions,{newCanvas=true},{panel="settings"})
                else model.document?.let{d->
                    var menu by remember{mutableStateOf(false)}
                    val drafting=panel=="text"||panel=="crop"||panel=="random"||listOf("effect:","node:","layer:","mask:","mask-layer:","graph-mask:").any{panel.startsWith(it)}
                    Row(Modifier.fillMaxWidth().heightIn(min=56.dp),verticalAlignment=Alignment.CenterVertically){
                        if(drafting){
                            IconButton(onClick=closePanel){Icon(Icons.Default.Close,model.tr("Отменить изменения","Cancel changes"))}
                            Text(commands?.title?:d.name,Modifier.weight(1f),fontSize=14.sp,maxLines=1,overflow=TextOverflow.Ellipsis)
                            commands?.let{cmd->
                                IconButton(onClick={cmd.apply();panel="presets"}){Icon(painterResource(R.drawable.ic_bookmark),model.tr("Сохранить набор","Save preset"))}
                                IconButton(onClick={cmd.apply();panel="export"}){Icon(painterResource(R.drawable.ic_save),model.tr("Применить и экспортировать","Apply and export"))}
                                IconButton(enabled=cmd.variant!=null,onClick={cmd.variant?.invoke()}){Icon(painterResource(R.drawable.ic_shuffle),model.tr("Другой вариант","New variant"))}
                                IconButton(onClick=cmd.apply){Icon(Icons.Default.Check,model.tr("Применить эффект","Apply effect"))}
                            }
                        }else{
                            TextButton(onClick={model.home=true}){Text("sf7b.lab",fontSize=13.sp)}
                            Spacer(Modifier.weight(1f))
                            IconButton(enabled=model.undoCount>0&&!model.working,onClick=model::undo){Icon(painterResource(R.drawable.ic_undo),model.tr("Отменить последнее действие","Undo"))}
                            IconButton(enabled=model.redoCount>0&&!model.working,onClick=model::redo){Icon(painterResource(R.drawable.ic_undo),model.tr("Повторить действие","Redo"),modifier=Modifier.graphicsLayer(scaleX=-1f))}
                            IconButton(onClick={panel="presets"}){Icon(painterResource(R.drawable.ic_bookmark),model.tr("Наборы настроек","Presets"))}
                            IconButton(onClick={panel="export"}){Icon(painterResource(R.drawable.ic_save),model.tr("Экспорт PNG","Export PNG"))}
                            Box{IconButton(onClick={menu=true}){Icon(Icons.Default.MoreVert,model.tr("Меню проекта","Project menu"))};DropdownMenu(menu,{menu=false}){
                                listOf(model.tr("Сохранить проект","Save project") to {actions.saveProject()},model.tr("Открыть проект","Open project") to {actions.openProject()},model.tr("Полотно / обрезка","Canvas / crop") to {panel="crop"},model.tr("Наборы настроек","Presets") to {panel="presets"},model.tr("Настройки","Settings") to {panel="settings"}).forEach{(label,click)->DropdownMenuItem(text={Text(label)},onClick={menu=false;click()})}
                            }}
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically){Text(d.name,Modifier.weight(1f),fontSize=11.sp,maxLines=1,overflow=TextOverflow.Ellipsis,color=MaterialTheme.colorScheme.onSurfaceVariant);Text("${d.outputWidth} × ${d.outputHeight}",fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);if(model.rendering){Spacer(Modifier.width(8.dp));CircularProgressIndicator(Modifier.size(13.dp),strokeWidth=2.dp)}}
                    BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()){
                        val dockHeight=if(collapsed)48.dp else minOf(maxHeight*(if(expanded).74f else if(panel.isEmpty()).43f else .58f),if(expanded)520.dp else if(panel.isEmpty())280.dp else 400.dp)
                        val dragThreshold=with(LocalDensity.current){44.dp.toPx()}
                        Column(Modifier.fillMaxSize()){
                            Box(Modifier.fillMaxWidth().weight(1f)){
                                Preview(model,paintTarget,panMode,Stroke(emptyList(),brushColor,brushSize,brushSoft,brushOpacity,eraser),Modifier.fillMaxSize())
                                if(paintTarget.isEmpty()){
                                    val node=d.nodes.find{it.id==model.activeNode}?:d.nodes.find{it.id==d.layers.find{l->l.id==model.activeLayer}?.head}
                                    val label=commands?.title?:node?.takeIf{it.kind=="effect"}?.let{Catalog.definition(it.effect).en}?:d.layers.find{it.id==model.activeLayer}?.name?:"sf7b.lab"
                                    Surface(onClick={closePanel();panel="tree"},modifier=Modifier.align(Alignment.BottomStart).padding(10.dp).widthIn(max=240.dp),shape=RoundedCornerShape(24.dp),color=MaterialTheme.colorScheme.primary.copy(alpha=.88f),contentColor=MaterialTheme.colorScheme.onPrimary){Row(Modifier.padding(horizontal=12.dp).heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){Text(label,Modifier.weight(1f,fill=false),fontSize=11.sp,maxLines=1,overflow=TextOverflow.Ellipsis);Icon(painterResource(R.drawable.ic_tree),model.tr("Открыть дерево","Open tree"),Modifier.size(22.dp))}}
                                }
                            }
                            if(paintTarget.isNotEmpty()){
                                Surface(color=MaterialTheme.colorScheme.surface){Column(Modifier.heightIn(max=270.dp).verticalScroll(rememberScrollState())){
                                    Row(Modifier.fillMaxWidth().padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically){Text(if(paintTarget.startsWith("draw:"))model.tr("Рисование","Drawing")else model.tr("Кисть маски","Mask brush"),Modifier.weight(1f));TextButton(onClick={paintTarget=""}){Text(model.tr("Готово","Done"))}}
                                    Row(Modifier.fillMaxWidth().padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically){FilterChip(eraser,{eraser=!eraser},label={Text(model.tr("Ластик","Eraser"))});Spacer(Modifier.width(8.dp));FilterChip(panMode,{panMode=!panMode},label={Text(model.tr("Перемещение","Pan / zoom"))})}
                                    if(paintTarget.startsWith("draw:"))ColorControl(model.tr("Цвет","Color"),brushColor,{brushColor=it},model)
                                    NumberControl(model.tr("Размер кисти","Brush size"),brushSize,.001f,.3f,{brushSize=it},model=model)
                                    NumberControl(model.tr("Мягкость","Softness"),brushSoft,0f,1f,{brushSoft=it},model=model)
                                    NumberControl(model.tr("Непрозрачность","Opacity"),brushOpacity,0f,1f,{brushOpacity=it},model=model)
                                }}
                            }else Surface(Modifier.fillMaxWidth().height(dockHeight),shape=RoundedCornerShape(topStart=20.dp,topEnd=20.dp),color=MaterialTheme.colorScheme.surface){
                                Column{
                                    Row(Modifier.fillMaxWidth().height(48.dp).pointerInput(panel,expanded,collapsed){var dy=0f;detectVerticalDragGestures(onDragStart={dy=0f},onDragEnd={if(dy < -dragThreshold){collapsed=false;expanded=true}else if(dy>dragThreshold){if(expanded)expanded=false else collapsed=true}},onVerticalDrag={change,delta->change.consume();dy+=delta})},verticalAlignment=Alignment.CenterVertically){
                                        IconButton(onClick={if(panel.isNotEmpty())closePanel()else collapsed=!collapsed}){Icon(painterResource(if(panel.isNotEmpty())R.drawable.ic_back else R.drawable.ic_chevron_down),if(panel.isNotEmpty())model.tr("К категориям","Back to categories")else model.tr("Свернуть панель","Collapse panel"))}
                                        Box(Modifier.weight(1f),contentAlignment=Alignment.Center){Box(Modifier.width(60.dp).height(5.dp).background(MaterialTheme.colorScheme.outlineVariant,RoundedCornerShape(4.dp)))}
                                        IconButton(onClick={collapsed=false;expanded=!expanded}){Icon(if(expanded)painterResource(R.drawable.ic_chevron_down) else painterResource(R.drawable.ic_chevron_up),model.tr("Размер панели","Panel size"))}
                                    }
                                    if(!collapsed){
                                        Box(Modifier.weight(1f).fillMaxWidth()){if(panel.isEmpty())StudioHome(model,actions){panel=it}else PanelBody()}
                                        if(panel.isEmpty())StudioTools(model,{panel=it},{draw()})
                                        else if(panel.startsWith("effect:")||panel.startsWith("node:")||panel=="tree")NodeRibbon(model){editNode(it)}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if(model.home&&panel.isNotEmpty())ModalBottomSheet(onDismissRequest=closePanel,sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=false),containerColor=MaterialTheme.colorScheme.surface){Box(Modifier.fillMaxWidth().heightIn(max=680.dp).imePadding()){PanelBody()}}
        if(newCanvas)CreateCanvasDialog(model,{newCanvas=false}){w,h,c->model.newCanvas(w,h,c);newCanvas=false}
        if(model.working)AlertDialog(onDismissRequest={},title={Text(model.operation)},text={Column{if(model.progress>0f)LinearProgressIndicator(progress={model.progress.coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth())else LinearProgressIndicator(modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(12.dp));Text(model.tr("Обработка выполняется на устройстве","Processing on this device"))}},confirmButton={TextButton(onClick=model::cancelWork){Text(model.tr("Отменить","Cancel"))}})
    }
}
