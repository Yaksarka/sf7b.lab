package lab.sf7b

import android.graphics.Paint
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import lab.sf7b.core.*

@Composable fun TreePanel(model:EditorModel,onEdit:(String)->Unit){val d=model.document?:return;val map=d.nodes.associateBy{it.id};var selected by remember{mutableStateOf(model.activeNode.takeIf{it in map}?:d.root)};val density=LocalDensity.current.density;var zoom by remember{mutableFloatStateOf(density*.85f)};var pan by remember{mutableStateOf(Offset(16f,16f))};var connect by remember{mutableStateOf(false)};var rewire by remember{mutableStateOf(false)}
    fun label(n:Node):String=when(n.kind){"source"->d.layer(n.layer).name;"layer"->d.layer(n.layer).name+model.tr(" · выход"," · output");"blend"->n.blend;"mask"->model.tr("Маска","Mask");else->Catalog.definition(n.effect).en}
    val levels=hashMapOf<String,Int>();fun depth(id:String):Int=levels.getOrPut(id){1+(map.getValue(id).inputs.maxOfOrNull{depth(it)}?:-1)}
    d.nodes.forEach{depth(it.id)};val rows=hashMapOf<Int,Int>();val positions=d.nodes.associate{n->val level=levels.getValue(n.id);val row=rows[level]?:0;rows[level]=row+1;n.id to Offset(level*210f,row*92f)}
    val surface=MaterialTheme.colorScheme.surfaceContainer;val accent=MaterialTheme.colorScheme.primary;val textColor=MaterialTheme.colorScheme.onSurface
    Column{PanelTitle(model.tr("Дерево обработки","Processing tree"),model.tr("Выберите узел. Двумя пальцами — масштаб и перемещение.","Select a node. Pinch to zoom and pan."))
        Canvas(Modifier.fillMaxWidth().height(260.dp).background(MaterialTheme.colorScheme.background).pointerInput(Unit){detectTransformGestures{_,p,z,_->pan+=p;zoom=(zoom*z).coerceIn(density*.15f,density*3f)}}.pointerInput(zoom,pan,d){detectTapGestures{p->val q=(p-pan)/zoom;positions.entries.firstOrNull{(_,v)->q.x in v.x..v.x+180&&q.y in v.y..v.y+64}?.let{selected=it.key;model.activeNode=it.key;map[it.key]?.layer?.takeIf{it.isNotEmpty()}?.let{model.activeLayer=it}}}}){
            fun pos(p:Offset)=p*zoom+pan
            for(n in d.nodes){val end=positions.getValue(n.id);for(input in n.inputs){val start=positions.getValue(input);drawLine(accent.copy(alpha=.5f),pos(start+Offset(180f,32f)),pos(end+Offset(0f,32f)),2f)}}
            val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=textColor.toArgb();textSize=14*zoom}
            for(n in d.nodes){val p=pos(positions.getValue(n.id));drawRoundRect(if(n.id==selected)accent.copy(alpha=.3f)else surface,p,Size(180*zoom,64*zoom),CornerRadius(10*zoom));drawContext.canvas.nativeCanvas.drawText(label(n).take(20),p.x+8*zoom,p.y+25*zoom,paint);paint.textSize=10*zoom;drawContext.canvas.nativeCanvas.drawText(if(n.kind=="effect")if(n.effect.enabled)"ON"else "OFF"else if(n.automatic)model.tr("Слои","Stack")else model.tr("Вход","Input"),p.x+8*zoom,p.y+46*zoom,paint);paint.textSize=14*zoom}
        }
        val node=map[selected]
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())){
            TextButton(enabled=node?.kind in listOf("effect","mask"),onClick={node?.let{onEdit(it.id)}}){Text(model.tr("Настроить","Edit"))}
            TextButton(enabled=node?.kind=="effect",onClick={if(node!=null)model.commit(d.copy(nodes=d.nodes.map{if(it.id==node.id)it.copy(effect=it.effect.copy(enabled=!it.effect.enabled))else it}))}){Text(model.tr("Вкл / выкл","On / off"))}
            TextButton(enabled=node!=null&&!node.automatic&&node.kind!="source",onClick={if(node!=null){model.commit(d.removeNode(node.id));selected=d.root}}){Text(model.tr("Удалить","Delete"))}
            TextButton(onClick={connect=true}){Text(model.tr("Смешать ветви","Blend branches"))}
            TextButton(enabled=node!=null&&!node.automatic,onClick={rewire=true}){Text(model.tr("Входы узла","Node inputs"))}
            TextButton(enabled=node!=null,onClick={if(node!=null){val n=Node(kind="mask",inputs=listOf(node.id),layer=model.activeLayer,maskData=Mask(selection="luma"));model.commit(d.copy(nodes=d.nodes+n));selected=n.id;onEdit(n.id)}}){Text(model.tr("Маска из ветви","Mask from branch"))}
        }
        LazyColumn(Modifier.heightIn(max=220.dp)){items(d.nodes.filter{!it.automatic},key={it.id}){n->
            val thumbnail by produceState<android.graphics.Bitmap?>(null,n,d){try{value=model.thumbnail(null,n.layer,n.id)}catch(e:kotlinx.coroutines.CancellationException){throw e}catch(_:Exception){}}
            ListItem(headlineContent={Text(label(n),fontSize=14.sp)},supportingContent={Text(n.inputs.mapNotNull{map[it]}.joinToString(" + "){label(it)},fontSize=12.sp)},leadingContent={Row(verticalAlignment=Alignment.CenterVertically){RadioButton(selected==n.id,{selected=n.id;model.activeNode=n.id;model.activeLayer=n.layer});thumbnail?.let{Image(it.asImageBitmap(),null,Modifier.size(48.dp))}}},modifier=Modifier.clickable{selected=n.id;model.activeNode=n.id;model.activeLayer=n.layer})}}
    }
    if(rewire&&map[selected]!=null)NodeInputsDialog(model,d,map.getValue(selected),{rewire=false})
    if(connect){val options=d.nodes.filter{!it.automatic||it.kind=="layer"};var a by remember{mutableStateOf(options.firstOrNull()?.id?:"")};var b by remember{mutableStateOf(d.layers.find{it.id==model.activeLayer}?.head?:options.lastOrNull()?.id?:"")};var mode by remember{mutableStateOf("Screen")};var editInput by remember{mutableIntStateOf(-1)}
        AlertDialog(onDismissRequest={connect=false},title={Text(model.tr("Входы смешивания","Blend inputs"))},text={Column{TextButton(onClick={editInput=0}){Text("A: ${map[a]?.let{label(it)}?:"—"}")};TextButton(onClick={editInput=1}){Text("B: ${map[b]?.let{label(it)}?:"—"}")};ChoiceRow(model.tr("Режим","Mode"),mode,BLENDS){mode=it};if(editInput>=0)Column(Modifier.heightIn(max=180.dp).verticalScroll(rememberScrollState())){options.forEach{n->TextButton(onClick={if(editInput==0)a=n.id else b=n.id;editInput=-1}){Text(label(n))}}}}},confirmButton={TextButton(enabled=a.isNotEmpty()&&b.isNotEmpty()&&model.activeLayer.isNotEmpty(),onClick={try{model.commit(d.branch(model.activeLayer,a,b,mode));connect=false}catch(_:Exception){model.message=model.tr("Такое соединение создаёт цикл","This connection would create a cycle")}}){Text(model.tr("Создать","Create"))}},dismissButton={TextButton(onClick={connect=false}){Text(model.tr("Отмена","Cancel"))}})
    }
}

@Composable private fun NodeInputsDialog(model:EditorModel,d:Document,node:Node,onClose:()->Unit){
    val map=d.nodes.associateBy{it.id};var a by remember{mutableStateOf(node.inputs.getOrNull(0)?:"")};var b by remember{mutableStateOf(node.inputs.getOrNull(1)?:"")};var mask by remember{mutableStateOf(node.maskInput)};var mode by remember{mutableStateOf(node.blend)};var choose by remember{mutableIntStateOf(-1)}
    fun label(id:String):String {val n=map[id]?:return model.tr("Нет","None");return(when(n.kind){"source","layer"->d.layer(n.layer).name;"mask"->model.tr("Маска","Mask");"blend"->n.blend;else->Catalog.byId(n.effect.id).en})+" · "+id.takeLast(4)}
    AlertDialog(onDismissRequest=onClose,title={Text(model.tr("Входы узла","Node inputs"))},text={Column(Modifier.heightIn(max=420.dp).verticalScroll(rememberScrollState())){
        if(node.kind!="source")TextButton(onClick={choose=0}){Text("A: ${label(a)}")}
        if(node.kind=="blend"||node.effect.id=="displacement")TextButton(onClick={choose=1}){Text("B: ${label(b)}")}
        TextButton(onClick={choose=2}){Text(model.tr("Маска: ","Mask: ")+label(mask))}
        if(node.kind=="blend")ChoiceRow(model.tr("Режим","Mode"),mode,BLENDS){mode=it}
        if(choose>=0){if(choose!=0)TextButton(onClick={if(choose==1)b=""else mask="";choose=-1}){Text(model.tr("Нет","None"))};d.nodes.filter{(!it.automatic||it.kind=="layer")&&it.id!=node.id&&(choose!=2||it.kind=="mask")}.forEach{n->TextButton(onClick={when(choose){0->a=n.id;1->b=n.id;2->mask=n.id};choose=-1}){Text(label(n.id))}}}
    }},confirmButton={TextButton(onClick={try{val inputs=if(node.kind=="source")emptyList()else listOf(a)+listOfNotNull(b.takeIf{it.isNotEmpty()&&(node.kind=="blend"||node.effect.id=="displacement")});val next=d.copy(nodes=d.nodes.map{if(it.id==node.id)it.copy(inputs=inputs,maskInput=mask,blend=mode)else it});next.validate();model.commit(next);onClose()}catch(_:Exception){model.message=model.tr("Связь создаёт цикл или недопустимый вход","Connection creates a cycle or invalid input")}}){Text(model.tr("Применить","Apply"))}},dismissButton={TextButton(onClick=onClose){Text(model.tr("Отмена","Cancel"))}})
}
