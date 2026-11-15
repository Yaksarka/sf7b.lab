package lab.sf7b.core

import kotlin.math.*

interface SourceProvider {
    /** Stable identity of immutable resource bytes (or system font). Unknown providers disable reuse. */
    fun resourceVersion(asset:String):String?=null
    fun image(asset:String):Surface
    /** Interactive sources can decode at working resolution; exports still request document size. */
    fun image(asset:String,width:Int,height:Int):Surface=image(asset)
    /** Text is rasterized from retained vector/font data at the requested working resolution. */
    fun text(layer:Layer,width:Int,height:Int):Surface
}
class Renderer(private val pool:SurfacePool,private val sources:SourceProvider,private val check:()->Unit={},private val cache:NodeRasterCache?=null,private val progress:(Float)->Unit={}) {
    private lateinit var document:Document
    private var w=0;private var h=0
    private val aspect get()=w.toFloat()/h
    private val longSide get()=max(w,h).toFloat()
    private val memo=hashMapOf<String,Surface>()
    private var completed=0
    fun render(doc:Document,width:Int,height:Int):Surface {
        doc.validate();document=doc;w=width;h=height;memo.clear();completed=0
        if(doc.root.isEmpty())return pool.create(w,h)
        val nodes=doc.nodes.associateBy{it.id}
        val keys=cache?.keys(doc,w,h,sources,check).orEmpty()
        fun evaluate(id:String):Surface=memo.getOrPut(id) {
            check();val n=nodes.getValue(id)
            val key=if(cacheable(n))keys[id]else null
            if(key!=null)cache?.open(pool,key,w,h)?.let{check();completed++;progress(completed.toFloat()/doc.nodes.size);return@getOrPut it}
            val inputs=n.inputs.map{evaluate(it)}
            val raw=when(n.kind){"source"->source(doc.layer(n.layer));"blend"->blend(inputs,n);"mask"->maskSurface(inputs[0],n.maskData);"layer"->layerOutput(inputs[0],doc.layer(n.layer).mask);else->if(n.effect.enabled&&n.effect.mix>0)effect(inputs[0],n.effect,inputs.getOrNull(1))else inputs[0]}
            val output=if(n.maskInput.isEmpty())raw else applyNodeMask(raw,if(n.kind=="effect")inputs[0]else null,evaluate(n.maskInput))
            if(key!=null)cache?.store(key,output,check)
            completed++;progress(completed.toFloat()/doc.nodes.size);output
        }
        return evaluate(doc.root).also{progress(1f)}
    }
    /** Cache computed planes, not cheap source/stack aliases already owned by their input. */
    private fun cacheable(node:Node):Boolean {
        if(node.maskInput.isNotEmpty())return true
        return when(node.kind){
            "source"->{val layer=document.layer(node.layer);when(layer.kind){
                "image"->layer.transform!=Transform()
                "text"->layer.text.text.isNotEmpty()
                "drawing"->layer.strokes.isNotEmpty()
                "generator"->true
                else->false
            }}
            "effect"->node.effect.enabled&&node.effect.mix>0&&!identityEffect(node.effect)
            "blend"->node.opacity>0&&(node.inputs.size==2||node.opacity!=1f)
            "layer"->document.layer(node.layer).mask!=null
            "mask"->true
            else->false
        }
    }
    private fun identityEffect(e:Effect):Boolean=when(e.id){
        "adjust"->e.params.all{(id,v)->v==Catalog.byId("adjust").params.first{it.id==id}.default}
        "curves"->e.curves.all{it==listOf(Point(0f,0f),Point(1f,1f))}
        "bloom","multi_distort"->e.algorithmVersion==2&&e.p("intensity")==0f
        else->false
    }
    private fun source(layer:Layer):Surface {
        val sourceScale=layer.transform.scale.coerceAtLeast(1f)
        val image=when(layer.kind){"image"->sources.image(layer.asset,max(1,(w*sourceScale).roundToInt()),max(1,(h*sourceScale).roundToInt()));"text"->sources.text(layer,w,h);else->null}
        if(image!=null&&image.width==w&&image.height==h&&layer.transform==Transform())return image
        val output=pool.create(w,h)
        val t=layer.transform;val angle=-t.angle*PI/180;val cos=cos(angle).toFloat();val sin=sin(angle).toFloat()
        val imageRatio=if(image!=null)image.width.toFloat()/image.height else aspect
        val fitW=if(imageRatio>aspect)1f else imageRatio/aspect
        val fitH=if(imageRatio>aspect)aspect/imageRatio else 1f
        val row=IntArray(w)
        for(y in 0 until h){check();for(x in 0 until w){
            val nx=(x+.5f)/w;val ny=(y+.5f)/h
            val dx=(nx-.5f-t.x)*aspect/t.scale;val dy=(ny-.5f-t.y)/t.scale
            val lx=(dx*cos-dy*sin)/aspect*(if(t.flipX)-1 else 1)+.5f;val ly=(dx*sin+dy*cos)*(if(t.flipY)-1 else 1)+.5f
            val c=when(layer.kind){
                "solid"->if(lx in 0f..1f&&ly in 0f..1f)Pixels.premul(layer.color)else 0
                "generator"->if(lx in 0f..1f&&ly in 0f..1f)generate(layer.generator,lx,ly)else 0
                "drawing"->{var p=0;for(s in layer.strokes){val v=strokeCoverage(s,lx,ly,aspect,.5f/h/t.scale);if(v>0)p=if(s.erase)Pixels.opacity(p,1-v)else Pixels.over(p,Pixels.premul(s.color),opacity=v)};p}
                else->{val xx=(lx-.5f)/fitW+.5f;val yy=(ly-.5f)/fitH+.5f;image!!.sample(xx*image.width-.5f,yy*image.height-.5f,false)}
            }
            row[x]=c
        };output.writeRow(y,row)}
        output.seal();return output
    }
    private fun blend(inputs:List<Surface>,n:Node):Surface {
        if(inputs.size==1&&n.opacity==1f)return inputs[0]
        val out=pool.create(w,h)
        for(y in 0 until h){check();val top=inputs.last().readRow(y);val bottom=if(inputs.size==2)inputs.first().readRow(y)else IntArray(w);for(x in 0 until w)bottom[x]=Pixels.over(bottom[x],top[x],n.blend,n.opacity);out.writeRow(y,bottom)}
        out.seal();return out
    }
    private fun maskSurface(input:Surface,m:Mask):Surface {val out=pool.create(w,h);for(y in 0 until h){check();val row=input.readRow(y);for(x in 0 until w){val v=maskValue(m,(x+.5f)/w,(y+.5f)/h,row[x],aspect,.5f/h);row[x]=Pixels.rgba(v,v,v,1f)};out.writeRow(y,row)};out.seal();return out}
    private fun layerOutput(input:Surface,mask:Mask?):Surface {if(mask==null)return input;val out=pool.create(w,h);for(y in 0 until h){check();val row=input.readRow(y);for(x in 0 until w)row[x]=Pixels.opacity(row[x],maskValue(mask,(x+.5f)/w,(y+.5f)/h,row[x],aspect,.5f/h));out.writeRow(y,row)};out.seal();return out}
    private fun applyNodeMask(raw:Surface,original:Surface?,mask:Surface):Surface {val out=pool.create(w,h);for(y in 0 until h){check();val row=raw.readRow(y);val matte=mask.readRow(y);val before=original?.readRow(y);for(x in 0 until w){val m=Pixels.luma(matte[x])*Pixels.a(matte[x])/255f;row[x]=if(before!=null)Pixels.mix(before[x],row[x],m)else Pixels.opacity(row[x],m)};out.writeRow(y,row)};out.seal();return out}
    private fun effect(input:Surface,e:Effect,map:Surface?=null):Surface {
        if(e.id=="adjust"&&e.params.all{(id,v)->v==Catalog.byId("adjust").params.first{it.id==id}.default})return input
        if(e.id=="curves"&&e.curves.all{it==listOf(Point(0f,0f),Point(1f,1f))})return input
        val reference=ReferenceEffects.render(pool,input,e,w,h,document.width,document.height,check,reference=map)?:ReferenceColorEffects.render(pool,input,e,check)?:PointwiseEffects.render(pool,input,e,check)
        val raw=reference?:if(e.algorithmVersion==2&&e.id=="bloom")bloomV2(input,e)else if(e.algorithmVersion==2&&e.id=="multi_distort")multiDistortV2(input,e)else when(e.id){"bloom"->bloom(input,e);"pixel_sort"->pixelSort(input,e);"floyd_steinberg"->floyd(input,e);else->{
            val out=pool.create(w,h);val row=IntArray(w)
            for(y in 0 until h){check();for(x in 0 until w)row[x]=pixel(input,e,x,y,map);out.writeRow(y,row)};out.seal();out
        }}
        if(e.mix==1f&&e.mask==null)return raw
        val out=pool.create(w,h)
        for(y in 0 until h){check();val a=input.readRow(y);val b=raw.readRow(y);for(x in 0 until w)a[x]=Pixels.mix(a[x],b[x],e.mix*maskValue(e.mask,(x+.5f)/w,(y+.5f)/h,a[x],aspect,.5f/h));out.writeRow(y,a)}
        out.seal();return out
    }
    private fun generate(e:Effect,nx:Float,ny:Float):Int {
        val size=max(.002f,e.p("size"));val unitX=nx*w/longSide;val unitY=ny*h/longSide
        val value=when(e.id){
            "linear_gradient"->(nx-.5f)*e.cosine+(ny-.5f)*e.sine+.5f
            "radial_gradient"->hypot((nx-e.p("cx"))*aspect,ny-e.p("cy"))/e.p("radius")
            "perlin"->{var sum=0f;var weight=1f;var total=0f;var freq=1f;repeat(e.p("octaves").roundToInt()){sum+=perlin(e.seed+it,unitX/size*freq,unitY/size*freq)*weight;total+=weight;weight*=.5f;freq*=2};sum/total}
            "voronoi"->{val ux=unitX/size;val uy=unitY/size;val ix=floor(ux).toInt();val iy=floor(uy).toInt();var best=Float.POSITIVE_INFINITY;var color=0f;for(dy in -1..1)for(dx in -1..1){val cx=ix+dx;val cy=iy+dy;val distance=hypot(ux-cx-hash(e.seed,cx,cy),uy-cy-hash(e.seed,cx,cy,1));if(distance<best){best=distance;color=hash(e.seed,cx,cy,2)}};color}
            "stripes"->if(floor((unitX*e.cosine+unitY*e.sine)/size).toInt()%2==0)0f else 1f
            "checker"->if((floor(unitX/size).toInt()+floor(unitY/size).toInt())%2==0)0f else 1f
            else->0f
        }
        return Pixels.palette(value,e.palette)
    }
    private fun pixel(input:Surface,e:Effect,x:Int,y:Int,map:Surface?):Int {
        val c=input.get(x,y);val nx=(x+.5f)/w;val ny=(y+.5f)/h;val p=e.p("amount");val size=max(.002f,e.p("size"))*longSide
        val angle=e.radians;val cs=e.cosine;val sn=e.sine
        fun sample(xx:Float,yy:Float,clamp:Boolean=true)=input.sample(xx,yy,clamp)
        fun recolor(r:Float,g:Float,b:Float)=Pixels.rgba(r,g,b,Pixels.a(c)/255f)
        when(e.id){
            "patchwork_break"->{val cx=floor(x/size).toInt();val cy=floor(y/size).toInt();if(hash(e.seed,cx,cy,3)>e.p("density"))return c;val dx=(hash(e.seed,cx,cy)-.5f)*p*w;val dy=(hash(e.seed,cx,cy,1)-.5f)*p*h;return sample(x+dx,y+dy)}
            "low_freq_banding"->{val band=floor(y/size).toInt();val shift=(hash(e.seed,band,0)-.5f)*e.p("shift")*w;val q=sample(x+shift,y.toFloat());val gain=1+(hash(e.seed,band,1)-.5f)*p;return Pixels.rgba(Pixels.red(q)*gain,Pixels.green(q)*gain,Pixels.blue(q)*gain,Pixels.a(q)/255f)}
            "multi_distort"->{var dx=0f;var dy=0f;repeat(e.p("layers").roundToInt()){k->val phase=hash(e.seed,k,0)*2*PI;val f=(k+1f);dx+=(sin(ny*h/size*f*2*PI+phase)*p*w*.08/f).toFloat();dy+=(cos(nx*w/size*f*2*PI+phase)*p*h*.08/f).toFloat()};return sample(x+dx,y+dy)}
            "random_pixelate"->{val fullX=nx*document.width;val fullY=ny*document.height;val top=max(2f,e.p("size")*max(document.width,document.height));var block=top;var ox=floor(fullX/top)*top;var oy=floor(fullY/top)*top;repeat(4){depth->val split=hash(e.seed,floor(ox/top*16).toInt(),floor(oy/top*16).toInt(),depth)<e.p("variation");if(split&&block>2){block*=.5f;ox+=floor((fullX-ox)/block)*block;oy+=floor((fullY-oy)/block)*block}};return sample((ox+block*.5f)/document.width*w-.5f,(oy+block*.5f)/document.height*h-.5f)}
            "band_shift"->{val band=floor(y/size).toInt();return sample(x+(hash(e.seed,0,band)-.5f)*p*w,y.toFloat())}
            "mirror"->{val axis=e.p("axis").roundToInt();return sample(if(axis!=1)abs(nx-.5f)*2*(w-1)else x.toFloat(),if(axis!=0)abs(ny-.5f)*2*(h-1)else y.toFloat())}
            "radial_mirror","kaleidoscope"->{val cx=e.p("cx")*w;val cy=e.p("cy")*h;val dx=x-cx;val dy=y-cy;val r=hypot(dx,dy);val sectors=if(e.id=="radial_mirror")2f else e.p("sectors");val sector=2*PI/sectors;val theta=atan2(dy,dx)-angle;val folded=abs(((theta%sector+sector)%sector)-sector/2)+angle;return sample(cx+(r*cos(folded)).toFloat(),cy+(r*sin(folded)).toFloat())}
            "waves"->{val shift=sin((x*sn+y*cs)/size*2*PI).toFloat()*p*longSide*.2f;return sample(x+shift*cs,y-shift*sn)}
            "swirl"->{val dx=(nx-e.p("cx"))*aspect;val dy=ny-e.p("cy");val r=hypot(dx,dy);val turn=atan2(dy,dx)+p*8*max(0f,1-r/e.p("radius")).pow(2);return sample((e.p("cx")+cos(turn)*r/aspect)*w-.5f,(e.p("cy")+sin(turn)*r)*h-.5f)}
            "displacement"->{val xx=x/size;val yy=y/size;val matte=map?.get(x,y);val dx=((if(matte==null)perlin(e.seed,xx,yy)else Pixels.red(matte))-.5f)*p*w;val dy=((if(matte==null)perlin(e.seed+173,xx,yy)else Pixels.green(matte))-.5f)*p*h;return sample(x+dx,y+dy)}
            "pixelate"->return sample(floor(x/size)*size+size*.5f,floor(y/size)*size+size*.5f)
            "bayer"->{val bayer=intArrayOf(0,8,2,10,12,4,14,6,3,11,1,9,15,7,13,5);val xx=floor(x/size).toInt();val yy=floor(y/size).toInt();val d=(bayer[(yy and 3)*4+(xx and 3)]+.5f)/16-.5f;val n=e.p("levels")-1;val q:(Float)->Float={v->round((v+d/n)*n)/n};return recolor(q(Pixels.red(c)),q(Pixels.green(c)),q(Pixels.blue(c)))}
            "mono_noise","color_noise","grain"->{val xx=floor(x/size).toInt();val yy=floor(y/size).toInt();var r=Pixels.red(c);var g=Pixels.green(c);var b=Pixels.blue(c);val nr=(hash(e.seed,xx,yy)-.5f)*p;val ng=if(e.id=="color_noise")(hash(e.seed,xx,yy,1)-.5f)*p else nr;val nb=if(e.id=="color_noise")(hash(e.seed,xx,yy,2)-.5f)*p else nr;val gain=if(e.id=="grain")sqrt(max(0f,Pixels.luma(c)*(1-Pixels.luma(c))))*2 else 1f;r+=nr*gain;g+=ng*gain;b+=nb*gain;return recolor(r,g,b)}
            "linear_gradient","radial_gradient","perlin","voronoi","stripes","checker"->return generate(e,nx,ny)
            "sphere"->{val dx=(nx-e.p("cx"))*aspect/e.p("radius");val dy=(ny-e.p("cy"))/e.p("radius");val r2=dx*dx+dy*dy;if(r2>1)return 0;val z=sqrt(1-r2);val u=atan2(dx,z)/(2*PI)+.5+angle/(2*PI);val v=asin(dy)/PI+.5;return sample((u*w).toFloat(),(v*h).toFloat(),false)}
            "height_light","emboss"->{val step=max(1f,longSide*.002f);val gx=Pixels.luma(sample(x+step,y.toFloat()))-Pixels.luma(sample(x-step,y.toFloat()));val gy=Pixels.luma(sample(x.toFloat(),y+step))-Pixels.luma(sample(x.toFloat(),y-step));val lighting=(.5f+(gx*cs+gy*sn)*p*8).coerceIn(0f,1f);return if(e.id=="emboss")recolor(lighting,lighting,lighting)else recolor(Pixels.red(c)*lighting*1.6f,Pixels.green(c)*lighting*1.6f,Pixels.blue(c)*lighting*1.6f)}
        }
        return c
    }
    private fun pixelSort(input:Surface,e:Effect):Surface {
        val out=pool.create(w,h)
        for(y in 0 until h){check();val row=input.readRow(y);var start=-1
            fun eligible(c:Int)=Pixels.a(c)>0&&Pixels.luma(c)>=min(e.p("low"),e.p("high"))&&Pixels.luma(c)<=max(e.p("low"),e.p("high"))
            for(x in 0..w){if(x<w&&eligible(row[x])){if(start<0)start=x}else if(start>=0){val sorted=row.sliceArray(start until x).toTypedArray().sortedBy{Pixels.luma(it)};for(i in sorted.indices)row[start+i]=sorted[i];start=-1}}
            out.writeRow(y,row)
        };out.seal();return out
    }
    private fun floyd(input:Surface,e:Effect):Surface {
        val out=pool.create(w,h);var error=Array(3){FloatArray(w+2)};val levels=e.p("levels")-1
        for(y in 0 until h){check();val row=input.readRow(y);val next=Array(3){FloatArray(w+2)}
            for(x in 0 until w){val c=row[x];if(Pixels.a(c)==0)continue;val values=floatArrayOf(Pixels.red(c),Pixels.green(c),Pixels.blue(c));for(k in 0..2){val v=(values[k]+error[k][x+1]).coerceIn(0f,1f);val quant=round(v*levels)/levels;val diff=v-quant;values[k]=quant;error[k][x+2]+=diff*7/16;next[k][x]+=diff*3/16;next[k][x+1]+=diff*5/16;next[k][x+2]+=diff/16};row[x]=Pixels.rgba(values[0],values[1],values[2],Pixels.a(c)/255f)}
            out.writeRow(y,row);error=next
        };out.seal();return out
    }
    private fun box(input:Surface,radius:Int,horizontal:Boolean):Surface {
        val out=pool.create(w,h);val r=radius.coerceAtLeast(1);val denom=2*r+1
        if(horizontal){for(y in 0 until h){check();val src=input.readRow(y);val row=IntArray(w);var aa=0L;var rr=0L;var gg=0L;var bb=0L
            fun add(c:Int,sign:Int){aa+=Pixels.a(c)*sign;rr+=Pixels.r(c)*sign;gg+=Pixels.g(c)*sign;bb+=Pixels.b(c)*sign}
            for(i in -r..r)if(i in 0 until w)add(src[i],1)
            for(x in 0 until w){row[x]=Pixels.pack((aa/denom).toInt(),(rr/denom).toInt(),(gg/denom).toInt(),(bb/denom).toInt());if(x-r>=0)add(src[x-r],-1);if(x+r+1<w)add(src[x+r+1],1)};out.writeRow(y,row)
        }}else{
            val aa=LongArray(w);val rr=LongArray(w);val gg=LongArray(w);val bb=LongArray(w)
            fun add(row:IntArray,sign:Int){for(x in 0 until w){aa[x]+=Pixels.a(row[x])*sign;rr[x]+=Pixels.r(row[x])*sign;gg[x]+=Pixels.g(row[x])*sign;bb[x]+=Pixels.b(row[x])*sign}}
            for(y in 0..min(r,h-1))add(input.readRow(y),1)
            val row=IntArray(w)
            for(y in 0 until h){check();for(x in 0 until w)row[x]=Pixels.pack((aa[x]/denom).toInt(),(rr[x]/denom).toInt(),(gg[x]/denom).toInt(),(bb[x]/denom).toInt());out.writeRow(y,row);if(y-r>=0)add(input.readRow(y-r),-1);if(y+r+1<h)add(input.readRow(y+r+1),1)}
        };out.seal();return out
    }
    /** Own luminance-selection/combine implementation. The three-box Gaussian approximation
     * is not an assertion of the reference's exact kernel or radius mapping. */
    private fun bloomV2(input:Surface,e:Effect):Surface {
        val intensity=e.p("intensity");if(intensity==0f)return input
        val tolerance=e.p("tolerance");val hardness=e.p("hardness")
        var glow=pool.create(w,h)
        for(y in 0 until h){check();val row=input.readRow(y);for(x in row.indices){
            val c=row[x];val distance=if(tolerance==0f)if(Pixels.luma(c)>=1f)0f else Float.POSITIVE_INFINITY else abs(Pixels.luma(c)-1f)/tolerance
            val gate=if(hardness>=1f)if(distance<=1f)1f else 0f else {val t=((1f-distance)/(1f-hardness)).coerceIn(0f,1f);t*t*(3-2*t)}
            // Private scratch: straight RGB remains independent of selected alpha, as in lum-select.
            val straight=Pixels.straight(c);row[x]=((Pixels.a(c)*gate).roundToInt() shl 24)or(straight and 0xffffff)
        };glow.writeRow(y,row)};glow.seal()
        val radius=max(1,(e.p("blurRadius")*longSide-.5f).roundToInt())
        repeat(3){glow=boxStraight(boxStraight(glow,radius,true),radius,false)}
        val out=pool.create(w,h);val balance=e.p("balance");val normalization=e.p("normalization")
        val maxValue=(1+intensity)*(1-balance)+balance;val normalize=1-normalization+normalization/maxValue
        for(y in 0 until h){check();val row=input.readRow(y);val blurred=glow.readRow(y);for(x in row.indices){
            val c=row[x];val q=blurred[x];val gain=intensity*Pixels.a(q)/255f;val blend=gain.coerceAtMost(1f)
            fun channel(base:Float,light:Int):Float {val b=light/255f;val added=base+b*gain;val blended=base+(b-base)*blend;return (added+(blended-added)*balance)*normalize}
            row[x]=Pixels.rgba(channel(Pixels.red(c),Pixels.r(q)),channel(Pixels.green(c),Pixels.g(q)),channel(Pixels.blue(c),Pixels.b(q)),Pixels.a(c)/255f)
        };out.writeRow(y,row)};out.seal();return out
    }
    private fun boxStraight(input:Surface,radius:Int,horizontal:Boolean):Surface {
        val out=pool.create(w,h);val r=radius.coerceAtLeast(1);val denominator=2L*r+1
        fun pack(a:Long,rr:Long,g:Long,b:Long)=((a/denominator).toInt() shl 24)or((rr/denominator).toInt() shl 16)or((g/denominator).toInt() shl 8)or(b/denominator).toInt()
        if(horizontal){for(y in 0 until h){check();val src=input.readRow(y);val row=IntArray(w);var a=0L;var rr=0L;var g=0L;var b=0L
            fun add(c:Int,sign:Int){a+=Pixels.a(c)*sign;rr+=Pixels.r(c)*sign;g+=Pixels.g(c)*sign;b+=Pixels.b(c)*sign}
            for(i in -r..r)add(src[i.coerceIn(0,w-1)],1)
            for(x in 0 until w){row[x]=pack(a,rr,g,b);add(src[(x-r).coerceIn(0,w-1)],-1);add(src[(x+r+1).coerceIn(0,w-1)],1)};out.writeRow(y,row)
        }}else{
            val a=LongArray(w);val rr=LongArray(w);val g=LongArray(w);val b=LongArray(w)
            fun add(row:IntArray,sign:Int){for(x in row.indices){a[x]+=Pixels.a(row[x])*sign;rr[x]+=Pixels.r(row[x])*sign;g[x]+=Pixels.g(row[x])*sign;b[x]+=Pixels.b(row[x])*sign}}
            for(y in -r..r)add(input.readRow(y.coerceIn(0,h-1)),1)
            val row=IntArray(w)
            for(y in 0 until h){check();for(x in row.indices)row[x]=pack(a[x],rr[x],g[x],b[x]);out.writeRow(y,row);add(input.readRow((y-r).coerceIn(0,h-1)),-1);add(input.readRow((y+r+1).coerceIn(0,h-1)),1)}
        };out.seal();return out
    }
    private fun multiDistortV2(input:Surface,e:Effect):Surface {
        if(e.p("intensity")==0f)return input
        val field=DistortionField(e,document.width.toFloat()/document.height)
        val out=pool.create(w,h);val lighting=e.p("lighting");val epsilon=1f/document.height
        for(y in 0 until h){check();val row=IntArray(w);for(x in row.indices){
            val nx=(x+.5f)/w;val ny=(y+.5f)/h;val point=field.map(nx,ny);val px=Float.fromBits((point ushr 32).toInt());val py=Float.fromBits(point.toInt())
            var c=input.sample(px*w-.5f,py*h-.5f)
            if(lighting!=0f){
                fun distance(p:Long,yy:Float):Float=hypot((Float.fromBits((p ushr 32).toInt())-nx)*aspect,Float.fromBits(p.toInt())-yy)
                val up=distance(field.map(nx,ny-epsilon),ny-epsilon);val down=distance(field.map(nx,ny+epsilon),ny+epsilon)
                val gain=(1-lighting*(down-up)/(2*epsilon)).coerceAtLeast(0f)
                c=Pixels.rgba(Pixels.red(c)*gain,Pixels.green(c)*gain,Pixels.blue(c)*gain,Pixels.a(c)/255f)
            };row[x]=c
        };out.writeRow(y,row)};out.seal();return out
    }
    private fun bloom(input:Surface,e:Effect):Surface {
        var glow=pool.create(w,h);val threshold=e.p("threshold")
        for(y in 0 until h){check();val row=input.readRow(y);for(x in 0 until w){val t=((Pixels.luma(row[x])-threshold)/max(.0001f,1-threshold)).coerceIn(0f,1f);row[x]=Pixels.opacity(row[x],t)};glow.writeRow(y,row)};glow.seal()
        val radius=max(1,(e.p("radius")*longSide*.58f).roundToInt());repeat(3){glow=box(box(glow,radius,true),radius,false)}
        val out=pool.create(w,h);val strength=e.p("strength")
        for(y in 0 until h){check();val row=input.readRow(y);val g=glow.readRow(y);for(x in 0 until w){val c=row[x];val q=g[x];val a=min(255f,Pixels.a(c)+Pixels.a(q)*strength*(1-Pixels.a(c)/255f)).roundToInt();row[x]=Pixels.pack(a,(Pixels.r(c)+Pixels.r(q)*strength).roundToInt(),(Pixels.g(c)+Pixels.g(q)*strength).roundToInt(),(Pixels.b(c)+Pixels.b(q)*strength).roundToInt())};out.writeRow(y,row)};out.seal();return out
    }
}

/** Own seeded, resolution-independent circular marble/flower/ripple/swirl field.
 * Original PRNG/constants and runtime parity are unverified; legacy v1 remains separate. */
private class DistortionField(private val effect:Effect,private val aspect:Float) {
    private data class Circle(val x:Float,val y:Float,val radius:Float,val phase:Float,val lobes:Int,val swirl:Float,val ripples:Float,val seed:Long)
    private class Field(val seed:Long,val variability:Float,val index:Int) {
        private val tags=LongArray(2048){Long.MIN_VALUE};private val cache=arrayOfNulls<Circle>(2048)
        private val candidateTags=LongArray(2048){Long.MIN_VALUE};private val candidateCache=arrayOfNulls<Array<Circle>>(2048)
        val angle=index*.713f;val cosine=cos(angle);val sine=sin(angle);val scale=1.27f.pow(index)
        val shiftX=hash(seed,index,17)*1.7f;val shiftY=hash(seed,index,23)*1.7f
        fun circle(x:Int,y:Int):Circle {
            val key=(x.toLong() shl 32)or(y.toLong()and 0xffffffffL);val slot=((x*73856093)xor(y*19349663))and 2047
            if(tags[slot]==key)cache[slot]?.let{return it}
            val c=Circle((x+.5f)*2+(hash(seed,x,y)-.5f)*5.5f*variability,(y+.5f)*2+(hash(seed,x,y,1)-.5f)*5.5f*variability,.85f*(1+variability*(hash(seed,x,y,2)*1.5f-.5f)),hash(seed,x,y,3)*2*PI.toFloat(),3+(hash(seed,x,y,4)*7).toInt(),(hash(seed,x,y,5)-.5f)*5f,3+hash(seed,x,y,6)*6,seed xor key)
            tags[slot]=key;cache[slot]=c;return c
        }
        fun candidates(x:Int,y:Int):Array<Circle> {
            val key=(x.toLong() shl 32)or(y.toLong()and 0xffffffffL);val slot=((x*73856093)xor(y*19349663))and 2047
            if(candidateTags[slot]==key)candidateCache[slot]?.let{return it}
            val neighborhood=if(variability==0f)0 else 2
            val left=x*2f;val top=y*2f;val right=left+2f;val bottom=top+2f
            val result=ArrayList<Circle>()
            // Reject only circles that cannot reach any point in this cell. The surviving
            // accumulation order is identical to the original per-pixel neighborhood loop.
            for(j in -neighborhood..neighborhood)for(i in -neighborhood..neighborhood){
                val circle=circle(x+i,y+j)
                val dx=when{circle.x<left->left-circle.x;circle.x>right->circle.x-right;else->0f}
                val dy=when{circle.y<top->top-circle.y;circle.y>bottom->circle.y-bottom;else->0f}
                if(dx*dx+dy*dy<=circle.radius*circle.radius)result+=circle
            }
            return result.toTypedArray().also{candidateTags[slot]=key;candidateCache[slot]=it}
        }
    }
    private val intensity=effect.p("intensity");private val size=effect.p("size").coerceAtLeast(.001f)
    private val cx=effect.p("cx");private val cy=effect.p("cy");private val cosine=effect.cosine;private val sine=effect.sine
    private val variability=effect.p("variability")
    private val layers=Array(effect.p("layerCount").roundToInt().coerceIn(1,8)){Field(effect.seed+it*173L,variability,it)}
    private fun noise(seed:Long,x:Float,y:Float):Float {
        val ix=floor(x).toInt();val iy=floor(y).toInt();var fx=x-ix;var fy=y-iy;fx=fx*fx*(3-2*fx);fy=fy*fy*(3-2*fy)
        val a=hash(seed,ix,iy)*(1-fx)+hash(seed,ix+1,iy)*fx;val b=hash(seed,ix,iy+1)*(1-fx)+hash(seed,ix+1,iy+1)*fx
        return a+(b-a)*fy
    }
    private fun marble(seed:Long,x:Float,y:Float):Float {var sum=0f;var weight=.5f;var frequency=1f;repeat(6){sum+=noise(seed+it*31,x*frequency,y*frequency)*weight;frequency*=2;weight*=.5f};return sum}
    fun map(nx:Float,ny:Float):Long {
        val dx=(nx-cx)*aspect/size;val dy=(ny-cy)/size
        var px=dx*cosine+dy*sine;var py=-dx*sine+dy*cosine
        for(layer in layers){
            val qx=(px*layer.cosine-py*layer.sine)*layer.scale+layer.shiftX;val qy=(px*layer.sine+py*layer.cosine)*layer.scale+layer.shiftY
            val cellX=floor(qx*.5f).toInt();val cellY=floor(qy*.5f).toInt();var offsetX=0f;var offsetY=0f
            for(circle in layer.candidates(cellX,cellY)){
                val x=qx-circle.x;val y=qy-circle.y;val squared=x*x+y*y
                if(squared>=circle.radius*circle.radius)continue
                val r=sqrt(squared);val t=(1-r/circle.radius);val falloff=t*t*intensity
                val n=marble(circle.seed,x*.9f+circle.phase,y*.9f-circle.phase)-.5f
                val marbleX=x+sin(y*3+n*6+circle.phase)*circle.radius*.2f*falloff
                val marbleY=y+cos(x*3+n*6-circle.phase)*circle.radius*.2f*falloff
                val theta=atan2(marbleY,marbleX);val flower=1+sin(theta*circle.lobes+circle.phase)*.3f*falloff
                val ripple=sin(r/circle.radius*circle.ripples*PI.toFloat()+circle.phase)*circle.radius*.12f*falloff
                val radius=hypot(marbleX,marbleY)*flower+ripple;val turn=theta+circle.swirl*falloff
                offsetX+=cos(turn)*radius-x;offsetY+=sin(turn)*radius-y
            }
            px+=(offsetX*layer.cosine+offsetY*layer.sine)/layer.scale;py+=(-offsetX*layer.sine+offsetY*layer.cosine)/layer.scale
        }
        val resultX=(px*cosine-py*sine)*size/aspect+cx;val resultY=(px*sine+py*cosine)*size+cy
        return (resultX.toRawBits().toLong() shl 32)or(resultY.toRawBits().toLong()and 0xffffffffL)
    }
}

/** Cropping/rotating and output resizing are independent of the viewing zoom. */
fun outputRow(d:Document,s:Surface,y:Int,outW:Int,outH:Int,background:Int=0):IntArray {
    if(d.frame==Frame()&&outW==s.width&&outH==s.height){val row=s.readRow(y);if(background!=0)for(x in row.indices)row[x]=Pixels.over(Pixels.premul(background),row[x]);return row}
    val f=d.frame;return IntArray(outW){x->
        var u=(x+.5f)/outW;var v=(y+.5f)/outH
        if(f.flipX)u=1-u;if(f.flipY)v=1-v
        val point=when(f.turn){1->Point(v,1-u);2->Point(1-u,1-v);3->Point(1-v,u);else->Point(u,v)}
        val c=s.sample((f.left+point.x*f.width)*s.width-.5f,(f.top+point.y*f.height)*s.height-.5f)
        if(background==0)c else Pixels.over(Pixels.premul(background),c)
    }
}
