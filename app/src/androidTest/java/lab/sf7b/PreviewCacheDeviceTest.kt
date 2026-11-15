package lab.sf7b

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import lab.sf7b.core.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class PreviewCacheDeviceTest {
    private val context get()=InstrumentationRegistry.getInstrumentation().targetContext
    private fun fixture(root:File):String {
        val asset="assets/input.png";val file=File(root,asset).also{it.parentFile!!.mkdirs()}
        val image=Bitmap.createBitmap(2000,2000,Bitmap.Config.ARGB_8888)
        for(y in 0 until 2000){val row=IntArray(2000){x->(if(x<200)128 else 255) shl 24 or ((x*255/1999) shl 16) or ((y*255/1999) shl 8) or (if((x/125+y/125)%2==0)220 else 25)};image.setPixels(row,0,2000,0,y,2000,1)}
        file.outputStream().use{assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,it))};image.recycle();return asset
    }
    @Test fun editingTheLastParameterUsesCachedMultiDistortAndMatchesAColdRender() {
        val root=File(context.cacheDir,"cache-device-${uid()}").also{it.mkdirs()}
        try {
            val layer=Layer(kind="image",asset=fixture(root));val base=Document(width=2000,height=2000).addLayer(layer).addEffect(layer.id,Catalog.newEffect("multi_distort"))
            val initial=base.addEffect(layer.id,Effect("threshold",mapOf("threshold" to .4f)))
            val last=initial.nodes.last{it.kind=="effect"}.id
            val changed=initial.copy(nodes=initial.nodes.map{if(it.id==last)it.copy(effect=it.effect.copy(params=mapOf("threshold" to .6f)))else it})
            val cache=NodeRasterCache(File(root,"nodes"),root.canonicalPath);var reads=0
            fun render(doc:Document,reuse:Boolean):Pair<IntArray,Long> {
                val start=SystemClock.elapsedRealtime()
                return SurfacePool(File(root,uid()),memoryBytes=48*1024*1024,persistOnClose=false).use{pool->
                    val source=AndroidSources(root,pool,ImageRepository(root,File(root,"decoded")))
                    val counted=object:SourceProvider by source{override fun image(asset:String,width:Int,height:Int):Surface{reads++;return source.image(asset,width,height)}}
                    val output=Renderer(pool,counted,cache=if(reuse)cache else null).render(doc,1024,1024)
                    val elapsed=SystemClock.elapsedRealtime()-start
                    IntArray(1024*1024).also{pixels->for(y in 0 until 1024)output.readRow(y).copyInto(pixels,y*1024)} to elapsed
                }
            }
            val first=render(initial,true);reads=0;val warm=render(changed,true)
            assertEquals("Cache hit must precede Android image decoding",0,reads);assertTrue(cache.hits>0)
            val cold=render(changed,false);assertArrayEquals(cold.first,warm.first);assertFalse(first.first.contentEquals(warm.first))
            val reports=File(context.filesDir,"test-results").also{it.mkdirs()}
            File(reports,"preview-cache-performance.txt").writeText("multi_distort_then_threshold,1024x1024\nfirst_ms=${first.second}\nchanged_last_parameter_cached_ms=${warm.second}\nchanged_last_parameter_uncached_ms=${cold.second}\ncache_hits=${cache.hits}\nCold and warm cached/uncached outputs matched exactly. One run per case, not a median or UI latency.\n")
        }finally{root.deleteRecursively()}
    }
    @Test fun fullSizeGpuPngUsesTheOriginalDimensionsAndMatchesCpuWithinThreeChannelUnits() {
        val root=File(context.cacheDir,"export-gpu-${uid()}").also{it.mkdirs()}
        try {
            val layer=Layer(kind="image",asset=fixture(root));val base=Document(width=2000,height=2000).addLayer(layer)
            val doc=listOf(Effect("adjust",mapOf("brightness" to .08f,"contrast" to .15f,"saturation" to .2f)),Effect("invert"),Effect("rgb_split",mapOf("amount" to .01f)),Effect("curves"),Effect("posterize")).fold(base){d,e->d.addEffect(layer.id,e)}
            SurfacePool(File(root,"pool"),memoryBytes=64*1024*1024,persistOnClose=false).use{pool->
                val source=AndroidSources(root,pool,ImageRepository(root,File(root,"decoded")))
                val cpu=Renderer(pool,source).render(doc,2000,2000)
                val start=SystemClock.elapsedRealtime();val gpu=GpuRenderer.tryRender(pool,source,doc,2000,2000)
                assertNotNull("The full-size test must execute on GLES",gpu)
                var error=0;var worst="";val shifts=intArrayOf(0,8,16,24)
                for(y in 0 until 2000){val a=cpu.readRow(y);val b=gpu!!.readRow(y);for(x in a.indices)for(shift in shifts){val delta=abs(((a[x] ushr shift)and 255)-((b[x] ushr shift)and 255));if(delta>error){error=delta;worst="x=$x y=$y shift=$shift CPU=${a[x].toUInt().toString(16)} GPU=${b[x].toUInt().toString(16)}"}}}
                assertTrue("Full-size GPU max channel error=$error $worst",error<=3)
                val file=File(root,"full.png");file.outputStream().use{PngWriter.write(it,2000,2000,{y->gpu!!.readRow(y)})}
                val elapsed=SystemClock.elapsedRealtime()-start
                val options=android.graphics.BitmapFactory.Options().apply{inJustDecodeBounds=true};android.graphics.BitmapFactory.decodeFile(file.path,options)
                assertEquals(2000,options.outWidth);assertEquals(2000,options.outHeight)
                val reports=File(context.filesDir,"test-results").also{it.mkdirs()}
                File(reports,"gpu-full-export-performance.txt").writeText("2000x2000 from retained original; max_premultiplied_channel_error=$error\nrender_compare_png_ms=$elapsed\nIncludes CPU/GPU row comparison and PNG; not a pure export latency measurement.\n")
            }
        }finally{root.deleteRecursively()}
    }
}
