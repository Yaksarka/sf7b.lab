package lab.sf7b.core

import kotlin.math.*

/** Row kernels retain the version-one math while resolving parameters once per operation. */
internal object PointwiseEffects {
    fun render(pool:SurfacePool,input:Surface,e:Effect,check:()->Unit):Surface?=when(e.id){
        "adjust"->adjust(pool,input,e,check)
        "invert"->map(pool,input,check){c,_,_->val a=Pixels.a(c);Pixels.pack(a,a-Pixels.r(c),a-Pixels.g(c),a-Pixels.b(c))}
        "threshold"->{val threshold=e.p("threshold");map(pool,input,check){c,_,_->val a=Pixels.a(c);val value=if(Pixels.luma(c)>=threshold)a else 0;Pixels.pack(a,value,value,value)}}
        "posterize"->{val levels=e.p("levels")-1;val values=ChannelTable{round(it*levels)/levels};map(pool,input,check){c,_,_->val a=Pixels.a(c);Pixels.pack(a,values.get(a,Pixels.r(c)),values.get(a,Pixels.g(c)),values.get(a,Pixels.b(c)))}}
        "curves"->{val tables=Array(3){channel->ChannelTable{curve(curve(it,e.curves[0]),e.curves[channel+1])}};map(pool,input,check){c,_,_->val a=Pixels.a(c);Pixels.pack(a,tables[0].get(a,Pixels.r(c)),tables[1].get(a,Pixels.g(c)),tables[2].get(a,Pixels.b(c)))}}
        "gradient_map"->{val palette=e.palette.map{Pixels.premul(it)}.toIntArray();map(pool,input,check){c,_,_->val position=Pixels.luma(c).coerceIn(0f,1f)*(palette.size-1);val index=floor(position).toInt().coerceAtMost(palette.size-2);Pixels.opacity(Pixels.mix(palette[index],palette[index+1],position-index),Pixels.a(c)/255f)}}
        "palette"->{
            val palette=e.palette.map{Pixels.premul(it)}.toIntArray();val reds=palette.map{Pixels.red(it)};val greens=palette.map{Pixels.green(it)};val blues=palette.map{Pixels.blue(it)}
            map(pool,input,check){c,_,_->val r=Pixels.red(c);val g=Pixels.green(c);val b=Pixels.blue(c);var selected=0;var best=Float.POSITIVE_INFINITY
                for(i in palette.indices){val distance=(r-reds[i]).pow(2)+(g-greens[i]).pow(2)+(b-blues[i]).pow(2);if(distance<best){best=distance;selected=i}}
                Pixels.opacity(palette[selected],Pixels.a(c)/255f)
            }
        }
        "scanlines"->{val cell=max(.002f,e.p("size"))*max(input.width,input.height);val amount=e.p("amount");map(pool,input,check){c,_,y->val gain=if((y/cell).toInt()%2==0)1f else 1-amount;Pixels.rgba(Pixels.red(c)*gain,Pixels.green(c)*gain,Pixels.blue(c)*gain,Pixels.a(c)/255f)}}
        "rgb_split"->{val distance=e.p("amount")*max(input.width,input.height);val dx=distance*e.cosine;val dy=distance*e.sine;map(pool,input,check){c,x,y->val red=input.sample(x+dx,y+dy);val blue=input.sample(x-dx,y-dy);Pixels.pack(max(Pixels.a(c),max(Pixels.a(red),Pixels.a(blue))),Pixels.r(red),Pixels.g(c),Pixels.b(blue))}}
        else->null
    }

    private inline fun map(pool:SurfacePool,input:Surface,check:()->Unit,operation:(Int,Int,Int)->Int):Surface {
        val out=pool.create(input.width,input.height)
        for(y in 0 until input.height){check();val row=input.readRow(y);for(x in row.indices)row[x]=operation(row[x],x,y);out.writeRow(y,row)}
        out.seal();return out
    }
    private class ChannelTable(private val operation:(Float)->Float) {
        private val rows=arrayOfNulls<IntArray>(256)
        fun get(alpha:Int,channel:Int):Int=(rows[alpha]?:build(alpha))[channel]
        private fun build(alpha:Int):IntArray=IntArray(256){channel->
            val value=if(alpha==0)0f else channel.toFloat()/alpha
            (operation(value).coerceIn(0f,1f)*alpha).roundToInt()
        }.also{rows[alpha]=it}
    }
    private fun curve(value:Float,points:List<Point>):Float {
        val hi=points.indexOfFirst{it.x>=value}.let{if(it<0)points.lastIndex else it};if(hi==0)return points[0].y
        val a=points[hi-1];val b=points[hi];return a.y+(b.y-a.y)*(value-a.x)/(b.x-a.x)
    }
    private fun adjust(pool:SurfacePool,input:Surface,e:Effect,check:()->Unit):Surface {
        val brightness=e.p("brightness");val shadows=e.p("shadows");val highlights=e.p("highlights");val black=e.p("black");val range=max(.001f,e.p("white")-black)
        val contrast=e.contrastMultiplier;val temperature=e.p("temperature");val tint=e.p("tint");val saturation=1+e.p("saturation");val cs=e.hueCosine;val sn=e.hueSine
        return map(pool,input,check){c,_,_->
            var r=Pixels.red(c);var g=Pixels.green(c);var b=Pixels.blue(c);val luma=Pixels.luma(c)
            val lift=brightness+shadows*(1-luma).pow(2)*.5f+highlights*luma.pow(2)*.5f
            r=(((e.exposed(r)+lift-black)/range-.5f)*contrast+.5f)+temperature*.15f+tint*.05f
            g=(((e.exposed(g)+lift-black)/range-.5f)*contrast+.5f)-tint*.1f
            b=(((e.exposed(b)+lift-black)/range-.5f)*contrast+.5f)-temperature*.15f+tint*.05f
            val gray=.2126f*r+.7152f*g+.0722f*b;r=gray+(r-gray)*saturation;g=gray+(g-gray)*saturation;b=gray+(b-gray)*saturation
            val sum=(r+g+b)/3
            Pixels.rgba(r*cs+(g-b)*sn+sum*(1-cs),g*cs+(b-r)*sn+sum*(1-cs),b*cs+(r-g)*sn+sum*(1-cs),Pixels.a(c)/255f)
        }
    }
}
