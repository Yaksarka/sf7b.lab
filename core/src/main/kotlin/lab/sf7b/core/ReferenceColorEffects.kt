package lab.sf7b.core

import kotlin.math.*

/** Own implementations from the mechanisms documented in research/NEXT_COLOR_BEHAVIOR.md.
 * The original UI defaults/ranges and runtime parity remain unverified. */
object ReferenceColorEffects {
    val ids=setOf("color_swap","lum_select","channel_multiplier","ink")
    private fun interface PixelOperation { fun pixel(color:Int):Int }
    fun supports(effect:Effect)=effect.algorithmVersion==1&&effect.id in ids

    fun render(pool:SurfacePool,input:Surface,effect:Effect,check:()->Unit={}):Surface? {
        if(!supports(effect))return null
        val operation=operation(effect)
        val result=pool.create(input.width,input.height)
        for(y in 0 until input.height){check();val row=input.readRow(y);for(x in row.indices)row[x]=operation.pixel(row[x]);result.writeRow(y,row)}
        result.seal();return result
    }
    fun applyPixel(effect:Effect,color:Int):Int {require(supports(effect));return operation(effect).pixel(color)}

    private fun operation(e:Effect):PixelOperation=when(e.id){
        "color_swap"->colorSwap(e)
        "lum_select"->luminanceSelection(e)
        "channel_multiplier"->channelMultiplier(e)
        "ink"->ink(e)
        else->error("Unsupported color operator: ${e.id}")
    }
    private fun luma(r:Float,g:Float,b:Float)=.2989f*r+.587f*g+.114f*b
    private fun gate(difference:Float,tolerance:Float,hardness:Float):Float {
        // The reference expression divides by tolerance. We define exact-match selection at zero.
        val distance=if(tolerance>0f)difference/tolerance else if(difference<=.000001f)0f else Float.POSITIVE_INFINITY
        if(hardness>=1f)return if(distance<=1f)1f else 0f
        val t=((1f-distance)/(1f-hardness)).coerceIn(0f,1f)
        return t*t*(3f-2f*t)
    }
    private fun colorSwap(e:Effect):PixelOperation {
        val input=e.palette[0];val output=e.palette[1]
        val ir=Pixels.r(input)/255f;val ig=Pixels.g(input)/255f;val ib=Pixels.b(input)/255f;val ia=Pixels.a(input)/255f
        val or=Pixels.r(output)/255f;val og=Pixels.g(output)/255f;val ob=Pixels.b(output)/255f;val oa=Pixels.a(output)/255f
        val tolerance=e.p("tolerance");val hardness=e.p("hardness")
        return PixelOperation{color->
            val r=Pixels.red(color);val g=Pixels.green(color);val b=Pixels.blue(color);val alpha=Pixels.a(color)/255f
            val dr=r-ir;val dg=g-ig;val db=b-ib
            val difference=sqrt(dr*dr+dg*dg+db*db)*max(alpha,ia)/sqrt(3f)
            val selected=gate(difference,tolerance,hardness)
            if(selected<=0f)color else Pixels.rgba(r+(or-r)*selected,g+(og-g)*selected,b+(ob-b)*selected,alpha+(oa-alpha)*selected)
        }
    }
    private fun luminanceSelection(e:Effect):PixelOperation {
        val target=e.p("target");val tolerance=e.p("tolerance");val hardness=e.p("hardness")
        return PixelOperation{color->
            val selected=gate(abs(luma(Pixels.red(color),Pixels.green(color),Pixels.blue(color))-target),tolerance,hardness)
            Pixels.opacity(color,selected)
        }
    }
    private fun ink(e:Effect):PixelOperation {
        val threshold=e.p("threshold");val hardness=e.p("hardness");val preserve=e.p("preserve_alpha")>=1f
        val low=hardness*threshold;val high=1f+hardness*(threshold-1f)
        return PixelOperation{color->
            val value=luma(Pixels.red(color),Pixels.green(color),Pixels.blue(color))
            val gray=if(hardness>=1f){if(value<threshold)0f else 1f}else{val t=((value-low)/(high-low)).coerceIn(0f,1f);t*t*(3f-2f*t)}
            Pixels.rgba(gray,gray,gray,if(preserve)Pixels.a(color)/255f else 1f)
        }
    }
    private fun channelMultiplier(e:Effect):PixelOperation {
        val red=e.p("red");val green=e.p("green");val blue=e.p("blue")
        val hue=e.p("hue");val saturation=e.p("saturation");val luminance=e.p("luminance")
        val style=e.p("style").roundToInt();val intensity=e.p("intensity")
        fun bounded(value:Float):Float=when(style){
            0->value-floor(value)
            1->{val repeated=value-floor(value/2f)*2f;1f-abs(repeated-1f)}
            else->value.coerceIn(0f,1f)
        }
        return PixelOperation{color->
            if(intensity<=0f)color else {
                val r=bounded(Pixels.red(color)*red);val g=bounded(Pixels.green(color)*green);val b=bounded(Pixels.blue(color)*blue)
                val highest=max(r,max(g,b));val lowest=min(r,min(g,b));val chroma=highest-lowest
                var lightness=highest-chroma*.5f
                var sat=if(chroma<=0f)0f else chroma/(1f-abs(2f*lightness-1f)+.000001f)
                var h=if(chroma<=0f)0f else 60f*when(highest){r->(g-b)/chroma;g->(b-r)/chroma+2f;else->(r-g)/chroma+4f}
                h=((h*hue)%360f+360f)%360f;sat=bounded(sat*saturation);lightness=bounded(lightness*luminance)
                val c=(1f-abs(2f*lightness-1f))*sat
                val x=c*(1f-abs((h/60f)%2f-1f));val m=lightness-c*.5f
                val rr:Float;val gg:Float;val bb:Float
                when{
                    h<60f->{rr=c;gg=x;bb=0f}
                    h<120f->{rr=x;gg=c;bb=0f}
                    h<180f->{rr=0f;gg=c;bb=x}
                    h<240f->{rr=0f;gg=x;bb=c}
                    h<300f->{rr=x;gg=0f;bb=c}
                    else->{rr=c;gg=0f;bb=x}
                }
                val transformed=Pixels.rgba(rr+m,gg+m,bb+m,Pixels.a(color)/255f)
                Pixels.mix(color,transformed,intensity)
            }
        }
    }
}
