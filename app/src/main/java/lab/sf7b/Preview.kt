package lab.sf7b

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*
import lab.sf7b.core.*
import kotlin.math.*

@Composable fun Preview(model:EditorModel,target:String,panMode:Boolean,brush:Stroke,modifier:Modifier){
    val bitmap=model.bitmap;val d=model.document?:return
    var zoom by remember{mutableFloatStateOf(1f)};var pan by remember{mutableStateOf(Offset.Zero)};var bounds by remember{mutableStateOf(IntSize.Zero)};var comparing by remember{mutableStateOf(false)}
    var current by remember{mutableStateOf<List<Point>>(emptyList())}
    LaunchedEffect(model.activeProjectId){zoom=1f;pan=Offset.Zero;current=emptyList();comparing=false}
    val latestBrush by rememberUpdatedState(brush);val latestDocument by rememberUpdatedState(d)
    val darkBackdrop=MaterialTheme.colorScheme.background.luminance()<.5f
    val checkerA=if(darkBackdrop)Color(0xff2d3438)else Color(0xffe5e8ea)
    val checkerB=if(darkBackdrop)Color(0xff3b4348)else Color(0xfff6f7f8)
    fun imagePoint(offset:Offset):Point?{
        val b=bitmap?:return null;val scale=min(bounds.width.toFloat()/b.width,bounds.height.toFloat()/b.height)*zoom
        val left=(bounds.width-b.width*scale)/2+pan.x;val top=(bounds.height-b.height*scale)/2+pan.y
        var u=(offset.x-left)/(b.width*scale);var v=(offset.y-top)/(b.height*scale)
        if(u !in 0f..1f||v !in 0f..1f)return null
        val f=latestDocument.frame;if(f.flipX)u=1-u;if(f.flipY)v=1-v
        val p=when(f.turn){1->Point(v,1-u);2->Point(1-u,1-v);3->Point(1-v,u);else->Point(u,v)}
        var result=Point(f.left+p.x*f.width,f.top+p.y*f.height)
        if(target.startsWith("draw:")){
            val l=latestDocument.layers.find{it.id==target.substringAfter(':')}?:return null;val t=l.transform;val aspect=d.width.toFloat()/d.height;val dx=(result.x-.5f-t.x)*aspect/t.scale;val dy=(result.y-.5f-t.y)/t.scale;val a=-t.angle*PI/180
            result=Point(((dx*cos(a)-dy*sin(a))/aspect*(if(t.flipX)-1 else 1)+.5).toFloat(),((dx*sin(a)+dy*cos(a))*(if(t.flipY)-1 else 1)+.5).toFloat())
        }
        return result
    }
    fun finish(){if(current.isEmpty())return;val doc=latestDocument;val stroke=latestBrush.copy(points=current);val id=target.substringAfter(':')
        when{target.startsWith("draw:")->{val l=doc.layers.find{it.id==id};if(l!=null)model.commit(doc.withLayer(l.copy(strokes=l.strokes+stroke)))}
            target.startsWith("layer:")->{val l=doc.layers.find{it.id==id};if(l!=null){val m=l.mask?:Mask();model.commit(doc.withLayer(l.copy(mask=m.copy(strokes=m.strokes+stroke))))}}
            target.startsWith("node:")->{val n=doc.nodes.find{it.id==id};if(n!=null){val m=n.effect.mask?:Mask();model.commit(doc.copy(nodes=doc.nodes.map{if(it.id==id)it.copy(effect=it.effect.copy(mask=m.copy(strokes=m.strokes+stroke)))else it}))}}
            target.startsWith("graph:")->{val n=doc.nodes.find{it.id==id};if(n!=null){val m=n.maskData;model.commit(doc.copy(nodes=doc.nodes.map{if(it.id==id)it.copy(maskData=m.copy(strokes=m.strokes+stroke))else it}))}}
        };current=emptyList()
    }
    val gesture=if(target.isEmpty()||panMode)Modifier.pointerInput(bitmap){detectTransformGestures{_,delta,change,_->zoom=(zoom*change).coerceIn(.25f,12f);pan+=delta}}.pointerInput(bitmap){detectTapGestures(onDoubleTap={zoom=1f;pan=Offset.Zero})}
        else Modifier.pointerInput(target,bitmap,bounds,zoom,pan){detectDragGestures(onDragStart={current=listOfNotNull(imagePoint(it))},onDragCancel={current=emptyList()},onDragEnd={finish()}){change,_->change.consume();imagePoint(change.position)?.let{p->if(current.isEmpty()||hypot(current.last().x-p.x,current.last().y-p.y)>.0005f)current=current+p}}}.pointerInput(target,bitmap,bounds,zoom,pan){detectTapGestures{offset->imagePoint(offset)?.let{current=listOf(it);finish()}}}
    Box(modifier.clipToBounds().onSizeChanged{bounds=it}.then(gesture)){
        Canvas(Modifier.fillMaxSize()){val cell=8.dp.toPx();for(y in 0..ceil(size.height/cell).toInt())for(x in 0..ceil(size.width/cell).toInt())drawRect(if((x+y)%2==0)checkerA else checkerB,Offset(x*cell,y*cell),androidx.compose.ui.geometry.Size(cell,cell))}
        val shown=if(comparing)model.original?:bitmap else bitmap
        if(shown!=null)Image(shown.asImageBitmap(),model.tr("Изображение проекта","Project image"),Modifier.fillMaxSize().graphicsLayer(scaleX=zoom,scaleY=zoom,translationX=pan.x,translationY=pan.y),contentScale=ContentScale.Fit)
        if(current.isNotEmpty()&&bitmap!=null)Canvas(Modifier.fillMaxSize()){
            val scale=min(size.width/bitmap.width,size.height/bitmap.height)*zoom;val left=(size.width-bitmap.width*scale)/2+pan.x;val top=(size.height-bitmap.height*scale)/2+pan.y
            fun point(p:Point):Offset{val transformed=if(target.startsWith("draw:")){
                val layer=d.layers.firstOrNull{it.id==target.substringAfter(':')};val t=layer?.transform?:Transform();val aspect=d.width.toFloat()/d.height;val a=t.angle*PI/180
                val dx=(p.x-.5f)*aspect*(if(t.flipX)-1 else 1);val dy=(p.y-.5f)*(if(t.flipY)-1 else 1)
                Point((.5+t.x+(dx*cos(a)-dy*sin(a))*t.scale/aspect).toFloat(),(.5+t.y+(dx*sin(a)+dy*cos(a))*t.scale).toFloat())
            }else p;val f=d.frame;val u=(transformed.x-f.left)/f.width;val v=(transformed.y-f.top)/f.height;var q=when(f.turn){1->Point(1-v,u);2->Point(1-u,1-v);3->Point(v,1-u);else->Point(u,v)};q=Point(if(f.flipX)1-q.x else q.x,if(f.flipY)1-q.y else q.y);return Offset(left+q.x*bitmap.width*scale,top+q.y*bitmap.height*scale)}
            val brushLayerScale=if(target.startsWith("draw:"))d.layers.firstOrNull{it.id==target.substringAfter(':')}?.transform?.scale?:1f else 1f
            val color=if(target.startsWith("draw:")&&!brush.erase)Color(brush.color)else Color(0xff0a84ff).copy(alpha=.6f);val width=max(1f,brush.size*max(d.width,d.height)*scale*bitmap.width/d.outputWidth*brushLayerScale)
            if(current.size==1)drawCircle(color,width/2,point(current[0]))else{val path=Path();path.moveTo(point(current[0]).x,point(current[0]).y);current.drop(1).forEach{p->val q=point(p);path.lineTo(q.x,q.y)};drawPath(path,color,style=DrawStroke(width,cap=StrokeCap.Round,join=StrokeJoin.Round))}
        }
        Row(Modifier.align(Alignment.TopEnd).padding(8.dp),verticalAlignment=Alignment.CenterVertically){
            Surface(color=MaterialTheme.colorScheme.surface.copy(alpha=.9f),shape=androidx.compose.foundation.shape.RoundedCornerShape(8.dp)){Text("${(zoom*100).roundToInt()}%",Modifier.padding(8.dp),fontSize=12.sp)}
            Spacer(Modifier.width(8.dp));Surface(color=MaterialTheme.colorScheme.surface.copy(alpha=.9f),shape=androidx.compose.foundation.shape.RoundedCornerShape(8.dp)){
                Text(model.tr("Оригинал","Original"),Modifier.heightIn(min=48.dp).padding(horizontal=12.dp).wrapContentHeight().semantics{contentDescription=model.tr("Удерживайте для сравнения с оригиналом","Hold to compare with original")}.pointerInput(Unit){detectTapGestures(onPress={comparing=true;try{tryAwaitRelease()}finally{comparing=false}})},fontSize=13.sp)
            }
        }
        if(bitmap==null)CircularProgressIndicator(Modifier.align(Alignment.Center))
    }
}
