package lab.sf7b

import android.graphics.Bitmap
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import lab.sf7b.core.*
import kotlinx.coroutines.CancellationException

@Composable fun LayersPanel(model:EditorModel,actions:UiActions,onEdit:(String)->Unit,onTree:()->Unit){val d=model.document?:return;var generator by remember{mutableStateOf(false)}
    Column{Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Box(Modifier.weight(1f)){PanelTitle(model.tr("Слои","Layers"))};TextButton(onClick=onTree){Text(model.tr("Дерево","Tree"))}}
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal=8.dp)){
            TextButton(onClick={actions.openImage(true)}){Text(model.tr("+ Изображение","+ Image"))};TextButton(onClick={model.addLayer(Layer(name=model.tr("Фон","Background"),color=0xffffffff.toInt()))}){Text(model.tr("+ Фон","+ Background"))};Box{TextButton(onClick={generator=true}){Text(model.tr("+ Генератор","+ Generator"))};DropdownMenu(generator,{generator=false}){Catalog.effects.filter{it.category==7}.forEach{def->DropdownMenuItem(text={Text(def.en)},onClick={model.addLayer(Layer(name=def.en,kind="generator",generator=Effect(def.id)));generator=false})}}}
            TextButton(onClick={model.addLayer(Layer(name=model.tr("Текст","Text"),kind="text"))}){Text(model.tr("+ Текст","+ Text"))};TextButton(onClick={model.addLayer(Layer(name=model.tr("Рисунок","Drawing"),kind="drawing"))}){Text(model.tr("+ Рисунок","+ Drawing"))}
        }
        LazyColumn(Modifier.heightIn(min=180.dp,max=440.dp)){items(d.layers.reversed(),key={it.id}){l->
            val thumbnail by produceState<Bitmap?>(null,l,model.document){try{value=model.thumbnail(null,l.id)}catch(e:CancellationException){throw e}catch(_:Exception){}}
            Surface(color=if(model.activeLayer==l.id)MaterialTheme.colorScheme.primary.copy(alpha=.15f)else MaterialTheme.colorScheme.surface){Row(Modifier.fillMaxWidth().heightIn(min=72.dp).clickable{model.selectLayer(l.id)},verticalAlignment=Alignment.CenterVertically){
                Box(Modifier.padding(8.dp).size(48.dp).background(MaterialTheme.colorScheme.surfaceContainer),contentAlignment=Alignment.Center){if(thumbnail!=null)Image(thumbnail!!.asImageBitmap(),null,Modifier.fillMaxSize())else Text(l.kind.take(1).uppercase())}
                Column(Modifier.weight(1f)){Text(l.name,fontSize=14.sp);Text("${(l.opacity*100).toInt()}% · ${l.blend}",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
                Checkbox(l.visible,{model.updateLayer(l.copy(visible=it))});TextButton(onClick={model.selectLayer(l.id);onEdit(l.id)}){Text(model.tr("Правка","Edit"))}
            }}
        }}
    }
}
@Composable fun LayerPanel(model:EditorModel,id:String,onClose:()->Unit,onMask:(String)->Unit){val d=model.document?:return;val original=d.layers.find{it.id==id}?:return;var l by remember(id){mutableStateOf(original)}
    fun change(next:Layer){l=next;model.preview(d.withLayer(l))}
    Column(Modifier.verticalScroll(rememberScrollState())){PanelTitle(model.tr("Свойства слоя","Layer properties"))
        OutlinedTextField(l.name,{change(l.copy(name=it))},Modifier.fillMaxWidth().padding(horizontal=16.dp),label={Text(model.tr("Имя","Name"))},singleLine=true)
        ToggleRow(model.tr("Видимость","Visibility"),l.visible,{change(l.copy(visible=it))})
        NumberControl(model.tr("Непрозрачность","Opacity"),l.opacity,0f,1f,{change(l.copy(opacity=it))},default=1f,model=model)
        ChoiceRow(model.tr("Смешивание","Blend mode"),l.blend,BLENDS){change(l.copy(blend=it))}
        if(l.kind=="solid")ColorControl(model.tr("Цвет фона","Background color"),l.color,{change(l.copy(color=it))},model)
        if(l.kind=="generator"){PalettePanel(l.generator.palette,{change(l.copy(generator=l.generator.copy(palette=it)))},model);val def=Catalog.byId(l.generator.id);def.params.forEach{p->NumberControl(if(model.language=="ru")p.ru else p.en,l.generator.p(p.id),p.min,p.max,{v->change(l.copy(generator=l.generator.copy(params=l.generator.params+(p.id to v))))},p.integral,p.unit,p.default,model)};if(def.random)TextButton(onClick={change(l.copy(generator=l.generator.copy(seed=java.security.SecureRandom().nextLong())))},modifier=Modifier.fillMaxWidth()){Text(model.tr("Другой случайный вариант","New random variant"))}}
        if(l.kind=="generator"&&Catalog.byId(l.generator.id).random){var seed by remember(l.generator.seed){mutableStateOf(l.generator.seed.toString())};OutlinedTextField(seed,{seed=it;it.toLongOrNull()?.let{v->change(l.copy(generator=l.generator.copy(seed=v)))}},Modifier.fillMaxWidth().padding(horizontal=16.dp),label={Text("Seed")},singleLine=true,isError=seed.toLongOrNull()==null)}
        NumberControl(model.tr("Положение X","Position X"),l.transform.x,-2f,2f,{change(l.copy(transform=l.transform.copy(x=it)))},default=0f,model=model)
        NumberControl(model.tr("Положение Y","Position Y"),l.transform.y,-2f,2f,{change(l.copy(transform=l.transform.copy(y=it)))},default=0f,model=model)
        NumberControl(model.tr("Масштаб слоя","Layer scale"),l.transform.scale,.01f,10f,{change(l.copy(transform=l.transform.copy(scale=it)))},default=1f,model=model)
        NumberControl(model.tr("Поворот","Rotation"),l.transform.angle,-180f,180f,{change(l.copy(transform=l.transform.copy(angle=it)))},unit="°",default=0f,model=model)
        ToggleRow(model.tr("Отразить X","Flip X"),l.transform.flipX,{change(l.copy(transform=l.transform.copy(flipX=it)))});ToggleRow(model.tr("Отразить Y","Flip Y"),l.transform.flipY,{change(l.copy(transform=l.transform.copy(flipY=it)))})
        TextButton(onClick={model.commit(d.withLayer(l));onMask(id)},modifier=Modifier.fillMaxWidth()){Text(model.tr("Применить и открыть маску слоя","Apply and open layer mask"))}
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())){TextButton(onClick={val i=d.layers.indexOf(original);if(i<d.layers.lastIndex){val ls=d.layers.toMutableList();ls.removeAt(i);ls.add(i+1,l);model.commit(d.copy(layers=ls).stack());onClose()}}){Text(model.tr("Выше","Move up"))};TextButton(onClick={val i=d.layers.indexOf(original);if(i>0){val ls=d.layers.toMutableList();ls.removeAt(i);ls.add(i-1,l);model.commit(d.copy(layers=ls).stack());onClose()}}){Text(model.tr("Ниже","Move down"))};TextButton(onClick={model.commit(d.withLayer(l).duplicateLayer(id));onClose()}){Text(model.tr("Копия","Duplicate"))};TextButton(onClick={try{model.commit(d.removeLayer(id));onClose()}catch(_:Exception){model.message=model.tr("Слой используется другой ветвью. Сначала измените её входы.","Another branch uses this layer. Reconnect its inputs first.")}}){Text(model.tr("Удалить","Delete"),color=MaterialTheme.colorScheme.error)}}
        ApplyRow(model,onClose,{model.commit(d.withLayer(l));onClose()})
    }
}
@Composable fun MaskPanel(model:EditorModel,id:String,isEffect:Boolean,onClose:()->Unit,onPaint:(String)->Unit){val d=model.document?:return;val layer=if(!isEffect)d.layers.find{it.id==id}else null;val node=if(isEffect)d.nodes.find{it.id==id}else null
    var mask by remember(id){mutableStateOf(layer?.mask?:if(node?.kind=="mask")node.maskData else node?.effect?.mask?:Mask())}
    fun doc(m:Mask?):Document=if(layer!=null)d.withLayer(layer.copy(mask=m))else if(node?.kind=="mask")if(m==null)d.removeNode(id)else d.copy(nodes=d.nodes.map{if(it.id==id)it.copy(maskData=m)else it})else d.copy(nodes=d.nodes.map{if(it.id==id)it.copy(effect=it.effect.copy(mask=m))else it})
    fun change(m:Mask){mask=m;model.preview(doc(mask))}
    Column(Modifier.verticalScroll(rememberScrollState())){PanelTitle(model.tr("Маска","Mask"),model.tr("Белое показывает, чёрное скрывает","White reveals, black hides"))
        val selectionIds=listOf("none","luma","color");val selectionLabels=if(model.language=="ru")listOf("Без выделения","По яркости","По цвету")else listOf("No selection","Luminance range","Color range")
        ChoiceRow(model.tr("Выделение","Selection"),selectionLabels[selectionIds.indexOf(mask.selection)],selectionLabels){change(mask.copy(selection=selectionIds[selectionLabels.indexOf(it)]))}
        if(mask.selection=="none")NumberControl(model.tr("Базовое покрытие","Base coverage"),mask.base,0f,1f,{change(mask.copy(base=it))},model=model)
        if(mask.selection=="luma"){NumberControl(model.tr("Нижняя яркость","Low luminance"),mask.low,0f,1f,{change(mask.copy(low=it))},model=model);NumberControl(model.tr("Верхняя яркость","High luminance"),mask.high,0f,1f,{change(mask.copy(high=it))},model=model)}
        if(mask.selection=="color"){ColorControl(model.tr("Выбранный цвет","Selected color"),mask.color,{change(mask.copy(color=it))},model);NumberControl(model.tr("Допуск","Tolerance"),mask.tolerance,.001f,1f,{change(mask.copy(tolerance=it))},model=model)}
        ToggleRow(model.tr("Инвертировать","Invert"),mask.inverted,{change(mask.copy(inverted=it))})
        TextButton(onClick={change(Mask())},modifier=Modifier.fillMaxWidth()){Text(model.tr("Сбросить маску","Reset mask"))}
        Button(onClick={model.commit(doc(mask));onPaint(id)},modifier=Modifier.fillMaxWidth().padding(horizontal=16.dp)){Text(model.tr("Применить и рисовать кистью","Apply and paint mask"))}
        TextButton(onClick={model.commit(doc(null));onClose()},modifier=Modifier.fillMaxWidth()){Text(model.tr("Удалить маску","Delete mask"))}
        ApplyRow(model,onClose,{model.commit(doc(mask));onClose()})
    }
}
@Composable fun TextPanel(model:EditorModel,actions:UiActions,onClose:()->Unit){val d=model.document?:return;val existing=d.layers.find{it.id==model.activeLayer&&it.kind=="text"};var layer by remember(model.activeLayer){mutableStateOf(existing?:Layer(name=model.tr("Текст","Text"),kind="text"))}
    val text=layer.text
    fun doc():Document=if(existing!=null)d.withLayer(layer)else d.addLayer(layer)
    fun change(next:TextData){layer=layer.copy(text=next);model.preview(doc())}
    Column(Modifier.verticalScroll(rememberScrollState())){PanelTitle(model.tr("Текстовый слой","Text layer"))
        OutlinedTextField(text.text,{change(text.copy(text=it))},Modifier.fillMaxWidth().padding(horizontal=16.dp),label={Text(model.tr("Текст","Text"))},minLines=2,maxLines=5)
        NumberControl(model.tr("Размер / сторона полотна","Size / canvas edge"),text.size,.005f,.8f,{change(text.copy(size=it))},model=model)
        ColorControl(model.tr("Цвет и прозрачность","Color and alpha"),text.color,{change(text.copy(color=it))},model)
        val alignIds=listOf("left","center","right");val alignLabels=if(model.language=="ru")listOf("Слева","По центру","Справа")else listOf("Left","Center","Right")
        ChoiceRow(model.tr("Выравнивание","Alignment"),alignLabels[alignIds.indexOf(text.align)],alignLabels){change(text.copy(align=alignIds[alignLabels.indexOf(it)]))}
        NumberControl(model.tr("Межстрочный интервал","Line spacing"),text.lineSpacing,.5f,3f,{change(text.copy(lineSpacing=it))},model=model)
        NumberControl(model.tr("Межбуквенный интервал","Letter spacing"),text.letterSpacing,-.1f,.5f,{change(text.copy(letterSpacing=it))},model=model)
        NumberControl(model.tr("Обводка / сторона полотна","Outline / canvas edge"),text.outline,0f,.05f,{change(text.copy(outline=it))},model=model)
        ColorControl(model.tr("Цвет обводки","Outline color"),text.outlineColor,{change(text.copy(outlineColor=it))},model)
        NumberControl(model.tr("Тень / сторона полотна","Shadow / canvas edge"),text.shadow,0f,.1f,{change(text.copy(shadow=it))},model=model)
        TextButton(onClick={val next=doc();model.commit(next);model.activeLayer=layer.id;actions.importFont(layer.id);onClose()},modifier=Modifier.fillMaxWidth()){Text(if(text.font.isEmpty())model.tr("Применить и импортировать TTF","Apply and import TTF")else model.tr("Заменить сохранённый шрифт TTF","Replace retained TTF"))}
        Text(model.tr("Положение, поворот и масштаб — в свойствах слоя.","Position, rotation and scale are in layer properties."),Modifier.padding(16.dp),fontSize=13.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
        ApplyRow(model,onClose,{model.commit(doc());model.activeLayer=layer.id;onClose()})
    }
}
