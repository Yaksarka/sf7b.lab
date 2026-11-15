package lab.sf7b

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import lab.sf7b.core.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class GpuChainTest {
    @Test fun quantizedChainsRetainBoundsAtEveryPrefix() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val root=File(context.cacheDir,"gpu-prefix-${uid()}").also{it.mkdirs()}
        val report=StringBuilder("case,prefix,max_channel_error,over3,worst\n");var failed=false
        try {SurfacePool(root,memoryBytes=32*1024*1024,persistOnClose=false).use{pool->
            val width=2000;val height=64;val source=pool.create(width,height)
            for(y in 0 until height)source.writeRow(y,IntArray(width){x->Pixels.rgba(x/1999f,y/63f,if((x/125+y/8)%2==0)220/255f else 25/255f,if(x<200)128/255f else 1f)})
            source.seal();val provider=object:SourceProvider{override fun image(asset:String)=source;override fun text(layer:Layer,width:Int,height:Int):Surface=error("unused")}
            val cases=listOf(
                listOf(Effect("adjust",mapOf("brightness" to .08f,"contrast" to .15f,"saturation" to .2f)),Effect("invert"),Effect("rgb_split",mapOf("amount" to .01f)),Effect("curves"),Effect("posterize")),
                listOf(Effect("adjust",mapOf("exposure" to .5f,"hue" to 31f,"contrast" to -.2f)),Effect("threshold",mapOf("threshold" to .5f)))
            )
            for((index,effects)in cases.withIndex()){
                val layer=Layer(kind="image",asset="assets/fixture.png");var doc=Document(width=width,height=height).addLayer(layer)
                for((prefix,effect)in effects.withIndex()){
                    doc=doc.addEffect(layer.id,effect);val cpu=Renderer(pool,provider).render(doc,width,height);val gpu=GpuRenderer.tryRender(pool,provider,doc,width,height)
                    assertNotNull(gpu);var maximum=0;var over=0;var worst="";val shifts=intArrayOf(0,8,16,24)
                    for(y in 0 until height){val a=cpu.readRow(y);val b=gpu!!.readRow(y);for(x in a.indices)for(shift in shifts){val delta=abs(((a[x] ushr shift)and 255)-((b[x] ushr shift)and 255));if(delta>3)over++;if(delta>maximum){maximum=delta;worst="x=$x y=$y shift=$shift CPU=${a[x].toUInt().toString(16)} GPU=${b[x].toUInt().toString(16)}"}}}
                    report.appendLine("$index,${prefix+1},$maximum,$over,$worst");if(maximum>3)failed=true
                }
            }
        }}finally{root.deleteRecursively();val directory=File(context.filesDir,"test-results").also{it.mkdirs()};File(directory,"gpu-chain-prefix-parity.csv").writeText(report.toString())}
        assertFalse(report.toString(),failed)
    }
}
