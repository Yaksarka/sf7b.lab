package lab.sf7b

import android.graphics.Bitmap
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import lab.sf7b.core.*
import kotlinx.coroutines.CancellationException
import java.util.Locale

data class EffectCommands(val title:String,val apply:()->Unit,val variant:(()->Unit)?)

@Composable fun StudioHome(model:EditorModel,actions:UiActions,onPanel:(String)->Unit) {
    val d=model.document?:return
    val source=d.layers.find{it.id==model.activeLayer&&it.kind=="image"}?:d.layers.firstOrNull{it.kind=="image"}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            StudioCard(model.tr("ИСХОДНИК","SOURCE IMAGE"),model.tr("Сменить изображение","Change the image"),0,Modifier.weight(1f)){if(source!=null)actions.replaceImage(source.id)else actions.openImage(true)}
            StudioCard("RANDOM MODE",model.tr("Случайная цепочка","Generate a chain"),1,Modifier.weight(1f)){onPanel("random")}
        }
        StudioCard(model.tr("Эффекты","Effects"),model.tr("Каталог и поиск","Browse and search"),2){onPanel("effects")}
        val categories=listOf(-1,1,4,5,6,7,9,8)
        val subtitles=if(model.language=="ru")listOf("Искажения, зеркала, узоры","Разрывы, полосы, цветовые сдвиги","Кривые, палитры, коррекция","Пикселизация и дизеринг","Текстура и зерно","Градиенты и процедурные источники","Свечение и освещение","Объём над изображением")else listOf("Distortions, mirrors, patterns","Breaks, bands, channel shifts","Curves, palettes, correction","Pixelation and dithering","Texture and grain","Gradients and procedural sources","Glow and lighting","Volume from an image")
        categories.forEachIndexed{i,category->
            val label=if(category==-1)model.tr("ГЕОМЕТРИЯ","GEOMETRY")else (if(model.language=="ru")Catalog.categoriesRu else Catalog.categoriesEn)[category].uppercase(Locale.ROOT)
            StudioCard(label,subtitles[i],i+3){onPanel("effects@$category")}
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable private fun StudioCard(title:String,subtitle:String,kind:Int,modifier:Modifier=Modifier,onClick:()->Unit) {
    val colors=MaterialTheme.colorScheme
    Surface(onClick=onClick,modifier=modifier.fillMaxWidth().heightIn(min=74.dp),shape=RoundedCornerShape(18.dp),color=colors.primaryContainer,contentColor=colors.onPrimaryContainer) {
        Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            Canvas(Modifier.size(42.dp)) {
                val ink=colors.primary;val faint=ink.copy(alpha=.20f)
                when(kind%4) {
                    0->{drawRoundRect(faint,cornerRadius=androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()));drawCircle(ink,size.minDimension*.13f,androidx.compose.ui.geometry.Offset(size.width*.7f,size.height*.26f));val path=Path().apply{moveTo(0f,size.height*.85f);lineTo(size.width*.36f,size.height*.35f);lineTo(size.width*.56f,size.height*.64f);lineTo(size.width*.78f,size.height*.43f);lineTo(size.width,size.height*.85f);close()};drawPath(path,ink)}
                    1->{for(i in 0..3)drawCircle(if(i%2==0)ink else faint,size.minDimension*(.10f+i*.08f),style=Stroke(2.dp.toPx()))}
                    2->{for(y in 0..3)for(x in 0..3)drawRect(if((x+y)%3==0)ink else faint,androidx.compose.ui.geometry.Offset(x*size.width/4,y*size.height/4),androidx.compose.ui.geometry.Size(size.width/4-2.dp.toPx(),size.height/4-2.dp.toPx()))}
                    else->{for(i in 0..5)drawLine(if(i%2==0)ink else faint,androidx.compose.ui.geometry.Offset(i*size.width/6,0f),androidx.compose.ui.geometry.Offset(size.width-i*size.width/6,size.height),3.dp.toPx())}
                }
            }
            Column(Modifier.weight(1f)) {Text(title,fontSize=14.sp,fontWeight=FontWeight.SemiBold,maxLines=2,overflow=TextOverflow.Ellipsis);Text(subtitle,fontSize=11.sp,maxLines=2,overflow=TextOverflow.Ellipsis)}
        }
    }
}

@Composable fun StudioTools(model:EditorModel,onPanel:(String)->Unit,onDraw:()->Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal=6.dp),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
        val entries=listOf(Triple("layers",model.tr("Слои","Layers"),painterResource(R.drawable.ic_layers)),Triple("tree",model.tr("Дерево","Tree"),painterResource(R.drawable.ic_tree)),Triple("text",model.tr("Текст","Text"),painterResource(R.drawable.ic_text)),Triple("drawing",model.tr("Рисование","Draw"),painterResource(R.drawable.ic_brush)),Triple("crop",model.tr("Обрезка","Crop"),painterResource(R.drawable.ic_crop)))
        for((id,label,icon)in entries)Surface(onClick={if(id=="drawing")onDraw()else onPanel(id)},modifier=Modifier.widthIn(min=62.dp).height(64.dp),shape=RoundedCornerShape(14.dp),color=MaterialTheme.colorScheme.primary,contentColor=MaterialTheme.colorScheme.onPrimary) {
            Column(Modifier.padding(horizontal=5.dp,vertical=6.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Icon(icon,null,Modifier.size(25.dp));Spacer(Modifier.height(3.dp));Text(label,fontSize=10.sp,maxLines=1)}
        }
    }
}

@Composable fun NodeRibbon(model:EditorModel,onEdit:(String)->Unit) {
    val d=model.document?:return
    LazyRow(Modifier.fillMaxWidth().height(86.dp).background(MaterialTheme.colorScheme.background),contentPadding=PaddingValues(horizontal=12.dp,vertical=5.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
        items(d.nodes.filter{!it.automatic},key={it.id}){node->
            val title=when(node.kind){"source"->d.layer(node.layer).name;"effect"->Catalog.definition(node.effect).en;"mask"->model.tr("Маска","Mask");else->node.blend}
            val thumbnail by produceState<Bitmap?>(null,node,d){try{value=model.thumbnail(null,node.layer,node.id)}catch(e:CancellationException){throw e}catch(_:Exception){}}
            Surface(onClick={model.selectLayer(node.layer);model.activeNode=node.id;onEdit(node.id)},modifier=Modifier.width(100.dp).fillMaxHeight(),shape=RoundedCornerShape(6.dp),border=if(model.activeNode==node.id)BorderStroke(2.dp,MaterialTheme.colorScheme.primary)else null,color=MaterialTheme.colorScheme.surfaceContainer) {
                Column{Text(title,Modifier.padding(horizontal=5.dp,vertical=2.dp),fontSize=10.sp,maxLines=1,overflow=TextOverflow.Ellipsis);Box(Modifier.fillMaxWidth().weight(1f),contentAlignment=Alignment.Center){thumbnail?.let{Image(it.asImageBitmap(),null,Modifier.fillMaxSize())}}}
            }
        }
    }
}

@Composable fun RandomModePanel(model:EditorModel,onClose:()->Unit) {
    val base=remember(model.activeProjectId){model.document}?:return
    val layer=remember{model.activeLayer};if(base.layers.none{it.id==layer})return
    var seed by remember{mutableLongStateOf(java.security.SecureRandom().nextLong())};var count by remember{mutableIntStateOf(3)}
    val candidates=listOf("rgb_split","band_shift","scanlines","invert","posterize","gradient_map","pixelate","bayer","mono_noise","color_noise","grain","mirror","waves","bloom")
    val draft=remember(seed,count){val random=kotlin.random.Random(seed);candidates.shuffled(random).take(count).foldIndexed(base){i,d,id->d.addEffect(layer,Catalog.newEffect(id).copy(seed=seed+i*173L))}}
    LaunchedEffect(draft){model.preview(draft)}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        PanelTitle("Random Mode")
        Text(draft.nodes.filter{it.kind=="effect"&&base.nodes.none{n->n.id==it.id}}.joinToString(" → "){Catalog.definition(it.effect).en},Modifier.padding(horizontal=16.dp),fontSize=13.sp)
        NumberControl(model.tr("Число эффектов","Effect count"),count.toFloat(),1f,5f,{count=it.toInt()},integer=true,model=model)
        TextButton(onClick={seed=java.security.SecureRandom().nextLong()},modifier=Modifier.fillMaxWidth()){Text(model.tr("Другой вариант","New variant"))}
        ApplyRow(model,onClose,{model.commit(draft);onClose()})
    }
}
