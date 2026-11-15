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

/** A single cold/edit/uncached experiment, not a median or a comparison with original apps. */
@RunWith(AndroidJUnit4::class)
class MixedPreviewCacheDeviceTest {
    private data class Result(val pixels:IntArray,val millis:Double,val imageCalls:Int,val textCalls:Int,val hits:Int,val misses:Int)

    private fun fixture(project:File,inverted:Boolean=false):String {
        val asset="assets/source.png";val file=File(project,asset);file.parentFile!!.mkdirs()
        val bitmap=Bitmap.createBitmap(2000,2000,Bitmap.Config.ARGB_8888)
        try{val row=IntArray(2000);for(y in 0 until 2000){for(x in row.indices){val alpha=if(x<500)128 else 255;val red=x*255/1999;val green=y*255/1999;row[x]=(alpha shl 24)or((if(inverted)255-red else red)shl 16)or((if(inverted)255-green else green)shl 8)or(if((x/125+y/125)%2==0)40 else 210)};bitmap.setPixels(row,0,2000,0,y,2000,1)}
            file.outputStream().use{assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,it))}
        }finally{bitmap.recycle()}
        return asset
    }
    private fun document(asset:String):Document {
        var doc=Document(width=2000,height=2000,name="Mixed preview cache fixture")
        for(i in 0 until 15){
            val kind=if(i==14)"text"else when(i%5){0->"image";1->"text";2->"drawing";3->"generator";else->"solid"}
            val layer=Layer(id="mixed-$i",name="Layer $i",kind=kind,asset=if(kind=="image")asset else "",
                color=0x805040a0.toInt(),opacity=if(i==0)1f else .65f,blend=if(i%3==0)"Screen"else"Normal",
                transform=if(kind=="image")Transform(x=(i-5)*.012f,y=.02f,scale=.82f,angle=i*3f+7)else Transform(),
                text=TextData(text="Layer $i / sf7b",size=.09f,color=0xc0f0e0d0.toInt(),outline=.002f),
                strokes=if(kind=="drawing")listOf(Stroke(listOf(Point(.1f,.1f+i*.02f),Point(.85f,.75f)),color=0xb0ee5500.toInt(),size=.055f,softness=.3f))else emptyList(),
                generator=Effect(if(i==8)"perlin"else"linear_gradient",seed=71+i.toLong()),
                mask=if(i%4==0)Mask(base=.8f,strokes=listOf(Stroke(listOf(Point(.3f,.2f),Point(.65f,.8f)),size=.18f,erase=true)))else null)
            doc=doc.addLayer(layer)
        }
        return doc.also{it.validate()}
    }
    private fun render(root:File,project:File,doc:Document,cached:Boolean):Result {
        val work=File(root,"work-${uid()}")
        val cache=if(cached)NodeRasterCache(File(root,"node-cache"),"isolated-mixed-fixture",64L*1024*1024)else null
        var imageCalls=0;var textCalls=0
        val start=SystemClock.elapsedRealtimeNanos()
        try{
            val pixels=SurfacePool(work,memoryBytes=48*1024*1024,persistOnClose=false).use{pool->
                val sources=AndroidSources(project,pool,ImageRepository(project,File(root,"decoded-cache")))
                val counted=object:SourceProvider {
                    override fun resourceVersion(asset:String)=sources.resourceVersion(asset)
                    override fun image(asset:String):Surface{imageCalls++;return sources.image(asset)}
                    override fun image(asset:String,width:Int,height:Int):Surface{imageCalls++;return sources.image(asset,width,height)}
                    override fun text(layer:Layer,width:Int,height:Int):Surface{textCalls++;return sources.text(layer,width,height)}
                }
                val output=Renderer(pool,counted,cache=cache).render(doc,512,512)
                IntArray(512*512).also{result->for(y in 0 until 512)output.readRow(y).copyInto(result,y*512)}
            }
            return Result(pixels,(SystemClock.elapsedRealtimeNanos()-start)/1_000_000.0,imageCalls,textCalls,cache?.hits?:0,cache?.misses?:0)
        }finally{work.deleteRecursively();cache?.trim()}
    }
    @Test fun fifteenMixedLayersReuseSourcesAfterAnUpperBlendEditAndMatchUncachedPixels(){
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val root=File(context.cacheDir,"mixed-preview-test-${uid()}").also{it.mkdirs()}
        try{
            val project=File(root,"project").also{it.mkdirs()};val doc=document(fixture(project))
            val cold=render(root,project,doc,true)
            assertEquals(15,doc.layers.size);assertTrue(cold.imageCalls>0);assertTrue(cold.textCalls>0)
            val last=doc.layers.last();val edited=doc.withLayer(last.copy(opacity=.3f,blend="Multiply"))
            val warm=render(root,project,edited,true)
            assertEquals("Unchanged transformed image sources must not be evaluated",0,warm.imageCalls)
            assertEquals("Unchanged text must not be rerasterized",0,warm.textCalls)
            assertTrue("The lower composite and upper text raster should both be cache hits",warm.hits>=2)
            val uncached=render(root,project,edited,false)
            assertArrayEquals("Caching must preserve every premultiplied output pixel",uncached.pixels,warm.pixels)
            assertFalse("The blend edit must really change the result",cold.pixels.contentEquals(warm.pixels))
            val size=File(root,"node-cache").listFiles().orEmpty().filter{it.name.matches(Regex("[0-9a-f]{64}\\.rgba"))}.sumOf{it.length()}
            assertTrue("Derived cache must stay within its 64 MiB budget",size<=64L*1024*1024)
            val output=File(context.filesDir,"test-results").also{it.mkdirs()}
            File(output,"mixed-preview-cache.csv").writeText(buildString{
                append("phase,document_size,preview_size,layers,milliseconds,image_provider_calls,text_raster_calls,cache_hits,cache_misses,scope\n")
                for((name,result)in listOf("cold_graph_and_decode" to cold,"upper_blend_edit_warm_cache" to warm,"uncached_graph_warm_decode" to uncached))append("$name,2000x2000,512x512,15,${result.millis},${result.imageCalls},${result.textCalls},${result.hits},${result.misses},single-run-debug-not-median-not-original-parity\n")
            })
        }finally{root.deleteRecursively()}
    }
    @Test fun sourceReplacementMaskAndEarlyEffectEditsInvalidateOnlyDependentRasters(){
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val root=File(context.cacheDir,"mixed-preview-test-${uid()}").also{assertTrue(it.mkdirs())}
        try{
            val project=File(root,"project").also{assertTrue(it.mkdirs())}
            val asset=fixture(project)
            val image=Layer(id="image",kind="image",asset=asset,transform=Transform(scale=.9f,angle=7f))
            val text=Layer(id="text",kind="text",text=TextData(text="Cache / sf7b",size=.13f,color=0xc0f0e0d0.toInt()),opacity=.7f)
            val doc=Document(width=2000,height=2000).addLayer(image)
                .addEffect(image.id,Effect("adjust",mapOf("brightness" to .1f,"contrast" to .15f)))
                .addLayer(text).also{it.validate()}
            val rows=mutableListOf<Pair<String,Result>>()
            val cold=render(root,project,doc,true).also{rows+="cold" to it}
            assertEquals(1,cold.imageCalls);assertEquals(1,cold.textCalls)
            val reference=render(root,project,doc,false).also{rows+="uncached_baseline" to it}
            assertArrayEquals("The initial cold cache must preserve every pixel",reference.pixels,cold.pixels)
            val unchanged=render(root,project,doc,true).also{rows+="unchanged_warm" to it}
            assertArrayEquals(cold.pixels,unchanged.pixels)
            assertEquals(0,unchanged.imageCalls);assertEquals(0,unchanged.textCalls);assertTrue(unchanged.hits>0)

            val maskEdit=doc.withLayer(doc.layer(text.id).copy(mask=Mask(base=.2f)))
            val effectId=doc.nodes.single{it.kind=="effect"}.id
            val earlyEdit=doc.copy(nodes=doc.nodes.map{if(it.id==effectId)it.copy(effect=it.effect.copy(params=mapOf("brightness" to -.25f,"contrast" to .15f)))else it})
            for((name,edited)in listOf("upper_mask_edit" to maskEdit,"early_effect_edit" to earlyEdit)){
                edited.validate()
                val cached=render(root,project,edited,true).also{rows+=name to it}
                assertEquals("$name must reuse the unchanged image raster",0,cached.imageCalls)
                assertEquals("$name must reuse the unchanged text raster",0,cached.textCalls)
                assertTrue("$name must reuse upstream rasters",cached.hits>=2)
                val uncached=render(root,project,edited,false).also{rows+="${name}_uncached" to it}
                assertArrayEquals("$name must invalidate dependent output",uncached.pixels,cached.pixels)
                assertFalse("$name must really change the result",cold.pixels.contentEquals(cached.pixels))
            }

            val file=File(project,asset);val oldModified=file.lastModified();val oldLength=file.length()
            assertEquals(asset,fixture(project,inverted=true))
            assertTrue(file.setLastModified(maxOf(System.currentTimeMillis(),oldModified+2000)))
            assertTrue("Replacement must change AndroidSources' resource version",file.length()!=oldLength||file.lastModified()!=oldModified)
            val replaced=render(root,project,doc,true).also{rows+="source_replaced" to it}
            assertEquals("The replaced image must be read again",1,replaced.imageCalls)
            assertEquals("The independent text raster must remain reusable",0,replaced.textCalls)
            assertTrue(replaced.hits>0)
            val replacedReference=render(root,project,doc,false).also{rows+="source_replaced_uncached" to it}
            assertArrayEquals("Source replacement must invalidate both derived cache and decoded source",replacedReference.pixels,replaced.pixels)
            assertFalse("Source replacement must really change the result",cold.pixels.contentEquals(replaced.pixels))
            val repeat=render(root,project,doc,true).also{rows+="source_replaced_warm" to it}
            assertArrayEquals(replaced.pixels,repeat.pixels);assertEquals(0,repeat.imageCalls);assertEquals(0,repeat.textCalls)

            val output=File(context.filesDir,"test-results").also{it.mkdirs()}
            File(output,"mixed-preview-cache-invalidation.csv").writeText(buildString{
                append("phase,document_size,preview_size,layers,milliseconds,image_provider_calls,text_raster_calls,cache_hits,cache_misses,scope\n")
                for((name,result)in rows)append("$name,2000x2000,512x512,2,${result.millis},${result.imageCalls},${result.textCalls},${result.hits},${result.misses},single-run-debug-exact-pixels-not-median-not-original-parity\n")
            })
        }finally{root.deleteRecursively()}
    }
}
