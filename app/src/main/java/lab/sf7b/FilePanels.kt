package lab.sf7b

import android.os.Build
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.text.input.KeyboardType
import lab.sf7b.core.*
import kotlin.math.*

@Composable fun CreateCanvasDialog(model:EditorModel,onDismiss:()->Unit,onCreate:(Int,Int,Int)->Unit){
    var width by remember{mutableStateOf("3000")};var height by remember{mutableStateOf("3000")};var color by remember{mutableIntStateOf(0)}
    val w=width.toIntOrNull()?:0;val h=height.toIntOrNull()?:0;val valid=w in 1..30000&&h in 1..30000&&w.toLong()*h<=240_000_000
    AlertDialog(onDismissRequest=onDismiss,title={Text(model.tr("Новое полотно","New canvas"))},text={Column(Modifier.verticalScroll(rememberScrollState())){
        Row{listOf("1:1" to (3000 to 3000),"4:5" to (2400 to 3000),"9:16" to (1080 to 1920)).forEach{(name,size)->TextButton(onClick={width=size.first.toString();height=size.second.toString()}){Text(name)}}}
        OutlinedTextField(width,{width=it},label={Text(model.tr("Ширина, px","Width, px"))},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number));Spacer(Modifier.height(8.dp));OutlinedTextField(height,{height=it},label={Text(model.tr("Высота, px","Height, px"))},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))
        ColorControl(model.tr("Фон · 00 = прозрачный","Background · 00 = transparent"),color,{color=it},model)
        if(!valid)Text(model.tr("От 1 до 30 000 px, не более 240 Мп","1–30,000 px, up to 240 MP"),color=MaterialTheme.colorScheme.error)
    }},confirmButton={TextButton(enabled=valid,onClick={onCreate(w,h,color)}){Text(model.tr("Создать","Create"))}},dismissButton={TextButton(onClick=onDismiss){Text(model.tr("Отмена","Cancel"))}})
}
fun cropFrameForAspect(frame:Frame,width:Int,height:Int,outputAspect:Float):Frame {
    require(width>0&&height>0&&outputAspect.isFinite()&&outputAspect>0f)
    val ratio=if(frame.turn%2==0)outputAspect else 1f/outputAspect
    val sourceAspect=width.toFloat()/height
    val fw=if(sourceAspect>ratio)ratio/sourceAspect else 1f
    val fh=if(sourceAspect>ratio)1f else sourceAspect/ratio
    return frame.copy(left=(1f-fw)/2f,top=(1f-fh)/2f,width=fw,height=fh)
}
@Composable fun CropPanel(model:EditorModel,onClose:()->Unit){val d=model.document?:return;var f by remember(d.frame){mutableStateOf(d.frame)};var aspect by remember{mutableStateOf<Float?>(null)}
    LaunchedEffect(f){model.preview(d.copy(frame=f))}
    Column(Modifier.verticalScroll(rememberScrollState())){PanelTitle(model.tr("Полотно и обрезка","Canvas and crop"),model.tr("Координаты относительно исходного полотна","Coordinates relative to the base canvas"))
        Row(Modifier.horizontalScroll(rememberScrollState())){listOf("Free" to 0f,"1:1" to 1f,"4:5" to .8f,"9:16" to .5625f,"16:9" to(16f/9f)).forEach{(label,r)->TextButton(onClick={aspect=r.takeIf{it>0};if(r>0)f=cropFrameForAspect(f,d.width,d.height,r)}){Text(if(label=="Free")model.tr("Свободно","Free")else label)}};TextButton(onClick={f=Frame();aspect=null}){Text(model.tr("Сброс","Reset"))}}
        NumberControl(model.tr("Слева","Left"),f.left,0f,max(0f,1-f.width),{f=f.copy(left=it)},model=model)
        NumberControl(model.tr("Сверху","Top"),f.top,0f,max(0f,1-f.height),{f=f.copy(top=it)},model=model)
        NumberControl(model.tr("Ширина","Width"),f.width,.001f,1f,{v->
            val a=aspect;val rawRatio=a?.let{if(f.turn%2==0)it else 1f/it}
            val nextWidth=if(rawRatio==null)v else min(v,rawRatio*d.height/d.width)
            val nextHeight=if(rawRatio==null)f.height else nextWidth*d.width/(rawRatio*d.height)
            f=f.copy(width=nextWidth,height=nextHeight,left=min(f.left,1-nextWidth),top=min(f.top,1-nextHeight))
        },model=model)
        NumberControl(model.tr("Высота","Height"),f.height,.001f,1f,{v->
            val a=aspect;val rawRatio=a?.let{if(f.turn%2==0)it else 1f/it}
            val nextHeight=if(rawRatio==null)v else min(v,d.width/(rawRatio*d.height))
            val nextWidth=if(rawRatio==null)f.width else nextHeight*rawRatio*d.height/d.width
            f=f.copy(width=nextWidth,height=nextHeight,left=min(f.left,1-nextWidth),top=min(f.top,1-nextHeight))
        },model=model)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center){TextButton(onClick={val rotated=f.copy(turn=(f.turn+3)%4);f=aspect?.let{cropFrameForAspect(rotated,d.width,d.height,it)}?:rotated}){Text("↶ 90°")};TextButton(onClick={val rotated=f.copy(turn=(f.turn+1)%4);f=aspect?.let{cropFrameForAspect(rotated,d.width,d.height,it)}?:rotated}){Text("90° ↷")}}
        ToggleRow(model.tr("Отразить по горизонтали","Flip horizontally"),f.flipX,{f=f.copy(flipX=it)});ToggleRow(model.tr("Отразить по вертикали","Flip vertically"),f.flipY,{f=f.copy(flipY=it)})
        val preview=d.copy(frame=f);Text("${preview.outputWidth} × ${preview.outputHeight}",Modifier.padding(16.dp));ApplyRow(model,onClose,{model.commit(preview);onClose()})
    }
}
@Composable fun ExportPanel(model:EditorModel,actions:UiActions){val d=model.document?:return;var w by remember(model.activeProjectId,d.outputWidth,d.outputHeight){mutableStateOf(d.outputWidth.toString())};var h by remember(model.activeProjectId,d.outputWidth,d.outputHeight){mutableStateOf(d.outputHeight.toString())};var lock by remember{mutableStateOf(true)};var transparent by remember{mutableStateOf(true)};var bg by remember{mutableIntStateOf(-1)}
    val width=w.toIntOrNull()?:0;val height=h.toIntOrNull()?:0;val valid=width in 1..30000&&height in 1..30000&&width.toLong()*height<=240_000_000
    Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom=16.dp)){PanelTitle(model.tr("Экспорт PNG","Export PNG"),"RGBA · 8 bit · sRGB")
        TextButton(onClick={w=d.outputWidth.toString();h=d.outputHeight.toString()},modifier=Modifier.padding(horizontal=8.dp)){Text(model.tr("Исходный размер","Original size"))}
        OutlinedTextField(w,{v->w=v;if(lock)v.toIntOrNull()?.takeIf{it>0}?.let{h=max(1,(it.toFloat()*d.outputHeight/d.outputWidth).roundToInt()).toString()}},Modifier.fillMaxWidth().padding(horizontal=16.dp),label={Text(model.tr("Ширина, px","Width, px"))},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),isError=width !in 1..30000)
        Spacer(Modifier.height(8.dp));OutlinedTextField(h,{v->h=v;if(lock)v.toIntOrNull()?.takeIf{it>0}?.let{w=max(1,(it.toFloat()*d.outputWidth/d.outputHeight).roundToInt()).toString()}},Modifier.fillMaxWidth().padding(horizontal=16.dp),label={Text(model.tr("Высота, px","Height, px"))},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),isError=height !in 1..30000)
        ToggleRow(model.tr("Сохранять пропорции","Keep aspect ratio"),lock,{lock=it});ToggleRow(model.tr("Прозрачный фон","Transparent background"),transparent,{transparent=it});if(!transparent)ColorControl(model.tr("Цвет фона","Background color"),bg,{bg=it or 0xff000000.toInt()},model)
        Text(model.tr("Место сохранения выберите в следующем системном окне.","Choose a destination in the next system dialog."),Modifier.padding(16.dp),fontSize=13.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Button(enabled=valid,onClick={actions.export(width,height,if(transparent)0 else bg)},modifier=Modifier.fillMaxWidth().padding(horizontal=16.dp)){Text(model.tr("Выбрать место и сохранить","Choose destination and save"))}
    }
}
@Composable fun SettingsPanel(model:EditorModel,actions:UiActions,onClose:()->Unit){Column(Modifier.verticalScroll(rememberScrollState())){PanelTitle(model.tr("Настройки","Settings"))
    ChoiceRow(model.tr("Язык","Language"),if(model.language=="ru")"Русский"else "English",listOf("Русский","English")){model.settings(lang=if(it=="Русский")"ru"else "en")}
    val themes=if(model.language=="ru")listOf("Системная","Тёмная","Светлая")else listOf("System","Dark","Light");val ids=listOf("system","dark","light")
    ChoiceRow(model.tr("Тема","Theme"),themes[ids.indexOf(model.theme).coerceAtLeast(0)],themes){model.settings(mode=ids[themes.indexOf(it)])}
    ChoiceRow(model.tr("Сторона предпросмотра","Preview edge"),model.quality.toString(),listOf("512","1024","2048")){model.settings(preview=it.toInt())}
    if(model.document!=null){TextButton(onClick=actions.saveProject,modifier=Modifier.fillMaxWidth()){Text(model.tr("Сохранить текущий проект","Save current project"))}}
    HorizontalDivider();Text("sf7b.lab ${BuildConfig.VERSION_NAME}",Modifier.padding(16.dp),fontSize=15.sp);Text(model.tr("Обработка и хранение проектов выполняются на устройстве. Для переноса экспортируйте файл .sf7blab.","Processing and project storage stay on this device. Export a .sf7blab file for transfer."),Modifier.padding(horizontal=16.dp),fontSize=13.sp)
    Text(model.lastTiming,Modifier.padding(16.dp),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
    TextButton(onClick=onClose,modifier=Modifier.fillMaxWidth()){Text(model.tr("Готово","Done"))}
}}
