package lab.sf7b

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import lab.sf7b.core.*
import org.junit.Assert.*
import org.junit.Assume.assumeNotNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class GpuRenderTest {
    private fun root()=File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir,"gpu-parity-${uid()}").also{it.mkdirs()}
    private fun compare(effects:List<Effect>,allowed:Int):Int {
        val dir=root()
        try{return SurfacePool(dir,memoryBytes=8*1024*1024).use{pool->
            val w=96;val h=80;val source=pool.create(w,h)
            for(y in 0 until h)source.writeRow(y,IntArray(w){x->Pixels.rgba(x.toFloat()/(w-1),y.toFloat()/(h-1),((x/8+y/8)%2)*.8f,if(x<16)0f else if(x<48).5f else 1f)})
            source.seal()
            val provider=object:SourceProvider{override fun image(asset:String)=source;override fun text(layer:Layer,width:Int,height:Int)=error("unused")}
            val layer=Layer(kind="image",asset="assets/fixture.image");var d=Document(width=w,height=h).addLayer(layer);effects.forEach{d=d.addEffect(layer.id,it)}
            val cpu=Renderer(pool,provider).render(d,w,h);val gpu=GpuRenderer.tryRender(pool,provider,d,w,h)
            assumeNotNull(gpu)
            var maxDifference=0;var worstPixel=""
            for(y in 0 until h){val a=cpu.readRow(y);val b=gpu!!.readRow(y);for(x in 0 until w)for(shift in listOf(0,8,16,24)){val delta=abs(((a[x] ushr shift)and 255)-((b[x] ushr shift)and 255));if(delta>maxDifference){maxDifference=delta;worstPixel="at ($x,$y) channel=$shift CPU=${a[x].toUInt().toString(16)} GPU=${b[x].toUInt().toString(16)}"}}}
            assertTrue("$effects: maximum premultiplied channel error=$maxDifference, allowed=$allowed $worstPixel",maxDifference<=allowed)
            maxDifference
        }}finally{dir.deleteRecursively()}
    }
    @Test fun identityMatchesCpuExactly(){assertEquals(0,compare(emptyList(),0))}
    @Test fun basicColorEffectsAndCurvesHaveBoundedChannelDifference(){
        val effects=listOf(Effect("adjust",mapOf("brightness" to .08f,"contrast" to .15f,"exposure" to .4f,"temperature" to -.1f,"saturation" to .25f,"hue" to 20f)),Effect("curves",curves=List(4){listOf(Point(0f,0f),Point(.4f,.65f),Point(1f,1f))}),Effect("invert"),Effect("threshold",mapOf("threshold" to .53217f)),Effect("posterize"),Effect("rgb_split"),Effect("scanlines"))
        effects.forEach{compare(listOf(it),2)}
        compare(listOf(effects[0],effects[1],effects[2]),3)
    }
    @Test fun bloomKeepsTransparentBordersAndMatchesCpuWithinRounding(){compare(listOf(Effect("bloom",mapOf("radius" to .025f,"strength" to .8f,"threshold" to .4f))),3)}
    @Test fun versionTwoBloomMatchesItsOwnCpuSelectionCombineAndPreservesAlpha(){
        val bloom=Catalog.newEffect("bloom").copy(params=mapOf("blurRadius" to .025f,"intensity" to .8f,"tolerance" to .6f,"hardness" to .3f,"balance" to .35f,"normalization" to .6f))
        compare(listOf(bloom),3)
        compare(listOf(bloom.copy(params=bloom.params+("intensity" to 0f))),0)
        compare(listOf(bloom.copy(params=bloom.params+mapOf("tolerance" to 0f,"normalization" to 0f))),0)
    }
    @Test fun unsupportedGraphsFallBackBeforeLoadingSources(){
        val dir=root();try{SurfacePool(dir).use{p->
            val never=object:SourceProvider{override fun image(asset:String):Surface=error("Unsupported graph must not load resources");override fun text(layer:Layer,width:Int,height:Int):Surface=error("unused")}
            assertNull(GpuRenderer.tryRender(p,never,Document.canvas(100,100),100,100))
            val l=Layer(kind="image",asset="assets/fixture.image");val d=Document(width=100,height=100).addLayer(l).addEffect(l.id,Effect("multi_distort"))
            assertNull(GpuRenderer.tryRender(p,never,d,100,100))
            assertNull(GpuRenderer.tryRender(p,never,Document(width=100,height=100).addLayer(l).addEffect(l.id,Catalog.newEffect("patchwork_break")),100,100))
        }}finally{dir.deleteRecursively()}
    }
}
