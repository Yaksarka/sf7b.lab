package lab.sf7b.core

import kotlin.math.*

/** Internal pixels are 8-bit premultiplied sRGB ARGB; file/Android boundaries use straight ARGB. */
object Pixels {
    fun a(c:Int)=c ushr 24
    fun r(c:Int)=(c ushr 16)and 255
    fun g(c:Int)=(c ushr 8)and 255
    fun b(c:Int)=c and 255
    fun pack(a:Int,r:Int,g:Int,b:Int):Int=(a.coerceIn(0,255) shl 24)or(r.coerceIn(0,a.coerceIn(0,255)) shl 16)or(g.coerceIn(0,a.coerceIn(0,255)) shl 8)or b.coerceIn(0,a.coerceIn(0,255))
    fun premul(c:Int):Int { val a=a(c);if(a==255)return c;if(a==0)return 0;return pack(a,(r(c)*a+127)/255,(g(c)*a+127)/255,(b(c)*a+127)/255) }
    fun straight(c:Int):Int { val a=a(c);if(a==255)return c;if(a==0)return 0;return(a shl 24)or(((r(c)*255+a/2)/a).coerceAtMost(255) shl 16)or(((g(c)*255+a/2)/a).coerceAtMost(255) shl 8)or((b(c)*255+a/2)/a).coerceAtMost(255) }
    fun rgba(r:Float,g:Float,b:Float,a:Float):Int { val ai=(a.coerceIn(0f,1f)*255).roundToInt();return pack(ai,(r.coerceIn(0f,1f)*ai).roundToInt(),(g.coerceIn(0f,1f)*ai).roundToInt(),(b.coerceIn(0f,1f)*ai).roundToInt()) }
    fun red(c:Int):Float=if(a(c)==0)0f else r(c).toFloat()/a(c)
    fun green(c:Int):Float=if(a(c)==0)0f else g(c).toFloat()/a(c)
    fun blue(c:Int):Float=if(a(c)==0)0f else b(c).toFloat()/a(c)
    fun luma(c:Int):Float=(.2126f*r(c)+.7152f*g(c)+.0722f*b(c))/max(1,a(c))
    fun mix(a:Int,b:Int,t:Float):Int { val v=t.coerceIn(0f,1f);return pack((a(a)+(a(b)-a(a))*v).roundToInt(),(r(a)+(r(b)-r(a))*v).roundToInt(),(g(a)+(g(b)-g(a))*v).roundToInt(),(b(a)+(b(b)-b(a))*v).roundToInt()) }
    fun opacity(c:Int,v:Float):Int {if(v==1f)return c;if(v<=0f)return 0;return pack((a(c)*v).roundToInt(),(r(c)*v).roundToInt(),(g(c)*v).roundToInt(),(b(c)*v).roundToInt())}
    fun over(bottom:Int,top:Int,mode:String="Normal",opacity:Float=1f):Int {
        if(a(top)==0||opacity<=0f)return bottom
        if(a(bottom)==0)return opacity(top,opacity)
        if(mode=="Normal"&&opacity==1f&&a(top)==255)return top
        val sa=a(top)/255f*opacity;val da=a(bottom)/255f;val out=sa+da*(1-sa);if(out<=0)return 0
        fun channel(s:Float,d:Float):Float {
            val blended=when(mode) {
                "Multiply"->s*d;"Screen"->s+d-s*d;"Overlay"->if(d<.5f)2*s*d else 1-2*(1-s)*(1-d)
                "Soft Light"->if(s<=.5f)d-(1-2*s)*d*(1-d) else d+(2*s-1)*((if(d<=.25f)((16*d-12)*d+4)*d else sqrt(d))-d)
                "Darken"->min(s,d);"Lighten"->max(s,d);"Difference"->abs(d-s);"Exclusion"->s+d-2*s*d;"Add"->min(1f,s+d);else->s
            }
            return ((1-sa)*da*d+(1-da)*sa*s+sa*da*blended)/out
        }
        return rgba(channel(red(top),red(bottom)),channel(green(top),green(bottom)),channel(blue(top),blue(bottom)),out)
    }
    fun palette(t:Float,colors:List<Int>):Int { val pos=t.coerceIn(0f,1f)*(colors.size-1);val i=floor(pos).toInt().coerceAtMost(colors.size-2);return mix(premul(colors[i]),premul(colors[i+1]),pos-i) }
}
fun hash(seed:Long,x:Int,y:Int,k:Int=0):Float {
    var h=seed xor (x.toLong()*0x632be59bd9b4e019L) xor (y.toLong()*0x517cc1b727220a95L) xor (k.toLong()*0x4f1bbcdcL)
    h=(h xor(h ushr 30))*-4658895280553007687L;h=(h xor(h ushr 27))*-7723592293110705685L;h=h xor(h ushr 31)
    return (h ushr 40).toFloat()/16777216f
}
fun perlin(seed:Long,x:Float,y:Float):Float {
    val ix=floor(x).toInt();val iy=floor(y).toInt();val fx=x-ix;val fy=y-iy
    fun gradient(dx:Int,dy:Int):Float { val angle=hash(seed,ix+dx,iy+dy)*2*PI;return (cos(angle)*(fx-dx)+sin(angle)*(fy-dy)).toFloat() }
    fun fade(v:Float)=v*v*v*(v*(v*6-15)+10)
    val u=fade(fx);val v=fade(fy);val a=gradient(0,0)*(1-u)+gradient(1,0)*u;val b=gradient(0,1)*(1-u)+gradient(1,1)*u
    return ((a*(1-v)+b*v)*.7f+.5f).coerceIn(0f,1f)
}
fun strokeCoverage(s:Stroke,x:Float,y:Float,aspect:Float,antialias:Float=0f):Float {
    var distance=Float.POSITIVE_INFINITY
    val px=x*aspect;val radius=s.size*.5f*max(aspect,1f)
    if(s.points.size==1) { val p=s.points[0];distance=hypot(px-p.x*aspect,y-p.y) }
    for(i in 1 until s.points.size) {
        val a=s.points[i-1];val b=s.points[i];val ax=a.x*aspect;val bx=b.x*aspect
        if(px<min(ax,bx)-radius || px>max(ax,bx)+radius || y<min(a.y,b.y)-radius || y>max(a.y,b.y)+radius)continue
        val dx=bx-ax;val dy=b.y-a.y;val t=(((px-ax)*dx+(y-a.y)*dy)/max(.0000001f,dx*dx+dy*dy)).coerceIn(0f,1f)
        distance=min(distance,hypot(px-ax-t*dx,y-a.y-t*dy))
    }
    val inner=max(0f,radius*(1-s.softness)-antialias);val outer=radius+antialias
    return if(distance<=inner)s.opacity else ((outer-distance)/max(.0000001f,outer-inner)).coerceIn(0f,1f)*s.opacity
}
fun maskValue(m:Mask?,x:Float,y:Float,c:Int,aspect:Float,antialias:Float=0f):Float {
    if(m==null)return 1f
    var value=when(m.selection){"luma"->{val l=Pixels.luma(c);if(l>=min(m.low,m.high)&&l<=max(m.low,m.high))1f else 0f};"color"->{val t=Pixels.premul(m.color);val d=sqrt((Pixels.red(c)-Pixels.red(t)).pow(2)+(Pixels.green(c)-Pixels.green(t)).pow(2)+(Pixels.blue(c)-Pixels.blue(t)).pow(2))/sqrt(3f);(1-d/m.tolerance).coerceIn(0f,1f)};else->m.base}
    for(s in m.strokes){val coverage=strokeCoverage(s,x,y,aspect,antialias);value=if(s.erase)value*(1-coverage)else value+(1-value)*coverage}
    return if(m.inverted)1-value else value
}
