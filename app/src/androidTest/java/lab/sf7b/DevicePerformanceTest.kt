package lab.sf7b

import android.graphics.Bitmap
import android.os.Debug
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import lab.sf7b.core.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.math.*

/** Explicit device measurements; these are not assertions that a latency goal is already met. */
@RunWith(AndroidJUnit4::class)
class DevicePerformanceTest {
    private val context get()=InstrumentationRegistry.getInstrumentation().targetContext
    private data class Timing(val scenario:String,val backend:String,val iteration:Int,val renderMs:Long,val bitmapMs:Long,val heapBytes:Long)
    @Test fun measureControlledFourMegapixelDocument() {
        val gpuOnly=InstrumentationRegistry.getArguments().getString("sf7bGpuOnly")=="true"
        val reportName=if(gpuOnly)"device-performance-gpu"else"device-performance"
        val root=File(context.cacheDir,"benchmark-${uid()}").also{it.mkdirs()}
        val reports=File(context.filesDir,"test-results").also{it.mkdirs()}
        val timings=mutableListOf<Timing>()
        try {
            val asset="assets/benchmark.png";val input=File(root,asset).also{it.parentFile!!.mkdirs()}
            val bitmap=Bitmap.createBitmap(2000,2000,Bitmap.Config.ARGB_8888)
            val row=IntArray(2000)
            for(y in 0 until 2000){for(x in row.indices)row[x]=0xff000000.toInt() or ((x*255/1999) shl 16) or ((y*255/1999) shl 8) or (if((x/125+y/125)%2==0)220 else 25);bitmap.setPixels(row,0,2000,0,y,2000,1)}
            input.outputStream().use{assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,it))};bitmap.recycle()
            input.copyTo(File(reports,"benchmark-input-2000.png"),overwrite=true)
            val image=Layer(kind="image",asset=asset)
            val base=Document(width=2000,height=2000).addLayer(image)
            val adjustment=Effect("adjust",mapOf("brightness" to .08f,"contrast" to .15f,"saturation" to .2f))
            val cases=listOf(
                "identity" to base,
                "one_adjustment" to base.addEffect(image.id,adjustment),
                "five_color_effects" to listOf(adjustment,Effect("invert"),Effect("rgb_split",mapOf("amount" to .01f)),Effect("curves"),Effect("posterize")).fold(base){d,e->d.addEffect(image.id,e)},
                "bloom_v2" to base.addEffect(image.id,Catalog.newEffect("bloom")),
                "multi_distort_v2_cpu" to base.addEffect(image.id,Catalog.newEffect("multi_distort"))
            )
            val sourceCache=File(root,"decoded")
            val csvFile=File(reports,"$reportName.csv");csvFile.writeText("scenario,backend,iteration,render_ms,bitmap_ms,heap_bytes\n")
            for((name,doc)in cases)for(backend in if(gpuOnly){if(name=="multi_distort_v2_cpu")emptyList()else listOf("GPU")}else if(name=="multi_distort_v2_cpu")listOf("CPU")else listOf("CPU","GPU"))for(iteration in 0..2){
                val start=SystemClock.elapsedRealtime()
                SurfacePool(File(root,"render-${uid()}"),memoryBytes=64*1024*1024,persistOnClose=false).use{pool->
                    val provider=AndroidSources(root,pool,ImageRepository(root,sourceCache))
                    val result=if(backend=="GPU")GpuRenderer.tryRender(pool,provider,doc,1024,1024) else Renderer(pool,provider).render(doc,1024,1024)
                    assertNotNull("GPU scenario must really execute on GLES: $name",result)
                    val rendered=SystemClock.elapsedRealtime()
                    val output=Bitmap.createBitmap(1024,1024,Bitmap.Config.ARGB_8888)
                    for(y in 0 until 1024){val colors=result!!.readRow(y);for(x in colors.indices)colors[x]=Pixels.straight(colors[x]);output.setPixels(colors,0,1024,0,y,1024,1)}
                    output.recycle()
                    val timing=Timing(name,backend,iteration,rendered-start,SystemClock.elapsedRealtime()-rendered,Debug.getNativeHeapAllocatedSize()+Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())
                    timings+=timing
                    csvFile.appendText("${timing.scenario},${timing.backend},${timing.iteration},${timing.renderMs},${timing.bitmapMs},${timing.heapBytes}\n")
                    android.util.Log.i("sf7b.benchmark","$name/$backend/$iteration render_ms=${timing.renderMs} bitmap_ms=${timing.bitmapMs}")
                }
            }
            for(backend in if(gpuOnly)listOf("GPU")else listOf("CPU","GPU"))for(iteration in if(backend=="CPU")0..0 else 0..2){
                val exportStart=SystemClock.elapsedRealtime();val exported=File(root,"full-$backend-$iteration.png")
                SurfacePool(File(root,"export-$backend-$iteration"),memoryBytes=64*1024*1024,persistOnClose=false).use{pool->
                    val provider=AndroidSources(root,pool,ImageRepository(root,sourceCache));val doc=cases.first{it.first=="five_color_effects"}.second
                    val result=if(backend=="GPU")GpuRenderer.tryRender(pool,provider,doc,2000,2000)else Renderer(pool,provider).render(doc,2000,2000)
                    assertNotNull("Full-size GPU measurement must execute on GLES",result)
                    exported.outputStream().use{PngWriter.write(it,2000,2000,{y->result!!.readRow(y)})}
                }
                val exportMs=SystemClock.elapsedRealtime()-exportStart
                val timing=Timing("five_color_full_2000_png",backend,iteration,exportMs,0,0);timings+=timing
                csvFile.appendText("${timing.scenario},$backend,$iteration,$exportMs,0,0\n")
                val options=android.graphics.BitmapFactory.Options().apply{inJustDecodeBounds=true};android.graphics.BitmapFactory.decodeFile(exported.path,options);assertEquals(2000,options.outWidth);assertEquals(2000,options.outHeight)
            }
            val csv=buildString{appendLine("scenario,backend,iteration,render_ms,bitmap_ms,heap_bytes");for(t in timings)appendLine("${t.scenario},${t.backend},${t.iteration},${t.renderMs},${t.bitmapMs},${t.heapBytes}")}
            File(reports,"$reportName.csv").writeText(csv)
            File(reports,"$reportName-summary.txt").writeText(timings.groupBy{it.scenario+"/"+it.backend}.entries.joinToString("\n"){(case,rows)->val middle=rows.size/2;"$case n=${rows.size} median_render_ms=${rows.map{it.renderMs}.sorted()[middle]} median_bitmap_ms=${rows.map{it.bitmapMs}.sorted()[middle]}"}+"\nInput=2000x2000 PNG; preview=1024; generated fixture; excludes file picker, UI composition and project persistence. Full-PNG rows include rendering and PNG writing; n is reported per case. gpu_only=$gpuOnly.")
            println(csv)
        } finally { root.deleteRecursively() }
    }
}
