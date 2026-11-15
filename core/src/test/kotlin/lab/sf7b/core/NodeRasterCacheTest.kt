package lab.sf7b.core

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.concurrent.CancellationException

class NodeRasterCacheTest {
    private fun directory() = Files.createTempDirectory("sf7b-node-cache").toFile()
    private val image = Layer(id="fixture",kind="image",asset="assets/grid.png")
    private fun document() = Document(width=32,height=24).addLayer(image)
        .addEffect(image.id,Effect("grain",mapOf("amount" to .25f),seed=71))
        .addEffect(image.id,Effect("threshold",mapOf("threshold" to .4f)))
    private var reads = 0
    private var textReads = 0
    private fun mixedDocument():Document {
        val picture=image.copy(transform=Transform(x=.08f,angle=17f,scale=.9f))
        val generator=Layer(id="generator",kind="generator",opacity=.4f,blend="Screen",generator=Effect("perlin",seed=42))
        val drawing=Layer(id="drawing",kind="drawing",strokes=listOf(Stroke(listOf(Point(.1f,.2f),Point(.9f,.8f)),color=0xb0ee5500.toInt(),size=.25f)))
        val text=Layer(id="text",kind="text",opacity=.8f,text=TextData(text="Cover",font="assets/font.ttf",color=0xb0a0d0ff.toInt()))
        return Document(width=32,height=24).addLayer(picture).addLayer(generator).addLayer(drawing).addLayer(text)
    }
    private fun renderMixed(root:File,doc:Document,cache:NodeRasterCache?=NodeRasterCache(File(root,"cache"),"mixed"),fontVersion:String?="font-v1",check:()->Unit={}):IntArray {
        val work=File(root,uid())
        try{return SurfacePool(work,memoryBytes=128*1024,persistOnClose=false).use{pool->
            val provider=object:SourceProvider {
                override fun resourceVersion(asset:String)=if(asset.endsWith(".ttf")||asset.isEmpty())fontVersion else "image-v1"
                override fun image(asset:String):Surface {
                    reads++
                    return pool.create(32,24).also{s->for(y in 0 until 24)s.writeRow(y,IntArray(32){x->Pixels.rgba(x/31f,y/23f,.2f,if(x<8).4f else 1f)});s.seal()}
                }
                override fun text(layer:Layer,width:Int,height:Int):Surface {
                    textReads++
                    val tint=(layer.text.text.hashCode() xor (fontVersion?.hashCode()?:0))and 255
                    val color=Pixels.premul(layer.text.color)
                    return pool.create(width,height).also{s->for(y in 0 until height)s.writeRow(y,IntArray(width){x->Pixels.rgba(((x+tint)%width).toFloat()/width,y.toFloat()/height,Pixels.blue(color),Pixels.a(color)/255f)});s.seal()}
                }
            }
            val output=Renderer(pool,provider,check,cache=cache).render(doc,32,24)
            IntArray(32*24).also{pixels->for(y in 0 until 24)output.readRow(y).copyInto(pixels,y*32)}
        }}finally{work.deleteRecursively();cache?.trim()}
    }
    private fun render(root:File,doc:Document,namespace:String="project-a",version:String?="bytes-v1",cached:Boolean=true,width:Int=32,height:Int=24):IntArray {
        val cache=if(cached)NodeRasterCache(File(root,"cache"),namespace)else null
        val work=File(root,uid())
        try { return SurfacePool(work,memoryBytes=128*1024,persistOnClose=false).use { pool ->
            val provider=object:SourceProvider {
                override fun resourceVersion(asset:String)=version
                override fun image(asset:String):Surface {
                    reads++
                    return pool.create(32,24).also { surface ->
                        for(y in 0 until 24)surface.writeRow(y,IntArray(32){x->Pixels.rgba(x/31f,y/23f,if(version=="bytes-v2").9f else .2f,if(x<8).4f else 1f)})
                        surface.seal()
                    }
                }
                override fun text(layer:Layer,width:Int,height:Int):Surface=error("unused")
            }
            val output=Renderer(pool,provider,cache=cache).render(doc,width,height)
            IntArray(width*height).also{pixels->for(y in 0 until height)output.readRow(y).copyInto(pixels,y*width)}
        }} finally { work.deleteRecursively();cache?.trim() }
    }
    @Test fun editingTheLastEffectReusesTheUnchangedUpstreamWithoutReadingItsSource() {
        val root=directory()
        try {
            val doc=document();render(root,doc)
            val last=doc.nodes.last{it.kind=="effect"}.id
            val changed=doc.copy(nodes=doc.nodes.map{if(it.id==last)it.copy(effect=it.effect.copy(params=mapOf("threshold" to .7f)))else it})
            reads=0;val actual=render(root,changed)
            assertEquals("The first effect should be loaded before visiting its image input",0,reads)
            assertArrayEquals(render(root,changed,cached=false),actual)
        } finally { root.deleteRecursively() }
    }
    @Test fun resourcesAndProjectNamespacesNeverReuseAnotherSource() {
        val root=directory()
        try {
            val doc=document();val first=render(root,doc)
            reads=0;val changed=render(root,doc,version="bytes-v2")
            assertEquals(1,reads);assertArrayEquals(render(root,doc,version="bytes-v2",cached=false),changed)
            assertFalse(first.contentEquals(changed))
            reads=0;render(root,doc,namespace="project-b");assertEquals(1,reads)
        } finally { root.deleteRecursively() }
    }
    @Test fun seedMaskParametersAndResolutionInvalidateTheDependentResult() {
        val root=directory()
        try {
            val doc=document();render(root,doc)
            val effect=doc.nodes.first{it.kind=="effect"}.id
            val variants=listOf(
                doc.copy(nodes=doc.nodes.map{if(it.id==effect)it.copy(effect=it.effect.copy(seed=92))else it}),
                doc.copy(nodes=doc.nodes.map{if(it.id==effect)it.copy(effect=it.effect.copy(mix=.3f,mask=Mask(base=.2f)))else it}),
                doc.copy(nodes=doc.nodes.map{if(it.id==effect)it.copy(effect=it.effect.copy(params=mapOf("amount" to .8f)))else it})
            )
            for(variant in variants){reads=0;val actual=render(root,variant);assertEquals(1,reads);assertArrayEquals(render(root,variant,cached=false),actual)}
            reads=0;val resized=render(root,doc,width=23,height=19);assertEquals(1,reads)
            assertArrayEquals(render(root,doc,cached=false,width=23,height=19),resized)
        } finally { root.deleteRecursively() }
    }
    @Test fun anUnknownResourceVersionDisablesReuse() {
        val root=directory()
        try {reads=0;render(root,document(),version=null);render(root,document(),version=null);assertEquals(2,reads);assertTrue(File(root,"cache").listFiles().orEmpty().isEmpty())}
        finally {root.deleteRecursively()}
    }
    @Test fun aTruncatedDerivedRasterFallsBackToTheOriginalGraph() {
        val root=directory()
        try {
            val doc=document();val expected=render(root,doc)
            File(root,"cache").listFiles()!!.forEach{it.writeBytes(byteArrayOf(1,2,3))}
            reads=0;assertArrayEquals(expected,render(root,doc));assertEquals(1,reads)
        } finally {root.deleteRecursively()}
    }
    @Test fun cancellationLeavesThePreviousEntryAndNoPartialFiles() {
        val root=directory();val cache=NodeRasterCache(File(root,"cache"),"p");val key="a".repeat(64)
        try {SurfacePool(File(root,"pool"),persistOnClose=false).use{pool->
            val source=pool.create(8,8);for(y in 0..7)source.writeRow(y,IntArray(8){0xff123456.toInt()});source.seal()
            cache.store(key,source){};val before=File(root,"cache/$key.rgba").readBytes();var calls=0
            try{cache.store(key,source){if(++calls==3)throw CancellationException()};fail("Must cancel") }catch(_:CancellationException){}
            assertArrayEquals(before,File(root,"cache/$key.rgba").readBytes());assertFalse(File(root,"cache").listFiles()!!.any{it.name.endsWith(".tmp")})
        }}finally{root.deleteRecursively()}
    }
    @Test fun evictionHonorsItsBudgetAndDoesNotDeleteUnrelatedFiles() {
        val root=directory();val directory=File(root,"cache").also{it.mkdirs()};val cache=NodeRasterCache(directory,"p",256)
        try {SurfacePool(File(root,"pool"),persistOnClose=false).use{pool->
            val unrelated=File(directory,"original.rgba").also{it.writeText("preserve")};val source=pool.create(8,8)
            val first="a".repeat(64);val second="b".repeat(64)
            cache.store(first,source){};File(directory,"$first.rgba").setLastModified(1)
            cache.store(second,source){};cache.trim()
            assertFalse(File(directory,"$first.rgba").exists());assertTrue(File(directory,"$second.rgba").exists());assertEquals("preserve",unrelated.readText())
            assertTrue(directory.listFiles()!!.filter{it.name.length==69}.sumOf{it.length()}<=256)
        }}finally{root.deleteRecursively()}
    }
    @Test fun aTextOnlyPreviewReusesItsRasterWithoutCallingTheFontRenderer(){
        val root=directory()
        try{
            val doc=Document(width=32,height=24).addLayer(mixedDocument().layer("text").copy(opacity=1f))
            val expected=renderMixed(root,doc);assertEquals(1,textReads)
            textReads=0;val cache=NodeRasterCache(File(root,"cache"),"mixed")
            assertArrayEquals(expected,renderMixed(root,doc,cache));assertEquals(0,textReads);assertEquals(1,cache.hits)
            assertEquals("Source is cached once; layer and single-input stack remain aliases",1,File(root,"cache").listFiles()!!.count{it.extension=="rgba"})
        }finally{root.deleteRecursively()}
    }
    @Test fun changingOnlyTheUpperBlendOrMaskSkipsAllUnchangedMixedSources(){
        val root=directory()
        try{
            val doc=mixedDocument();val original=renderMixed(root,doc)
            for(changed in listOf(doc.withLayer(doc.layer("text").copy(opacity=.35f,blend="Multiply")),doc.withLayer(doc.layer("text").copy(mask=Mask(base=.4f))))){
                reads=0;textReads=0;val cache=NodeRasterCache(File(root,"cache"),"mixed")
                val actual=renderMixed(root,changed,cache)
                assertEquals(0,reads);assertEquals(0,textReads);assertTrue("Upstream composite and text raster should both be reusable",cache.hits>=2)
                assertArrayEquals(renderMixed(root,changed,cache=null),actual);assertFalse(original.contentEquals(actual))
            }
        }finally{root.deleteRecursively()}
    }
    @Test fun mixedSourceEditsInvalidateOnlyTheirOwnRastersAndDependentComposites(){
        val root=directory()
        try{
            val doc=mixedDocument();val original=renderMixed(root,doc)
            val variants=listOf(
                doc.withLayer(doc.layer("text").copy(text=doc.layer("text").text.copy(text="Variant"))) to (0 to 1),
                doc.withLayer(doc.layer("drawing").copy(strokes=doc.layer("drawing").strokes+Stroke(listOf(Point(.1f,.9f),Point(.9f,.1f)),color=0xff22aa88.toInt(),size=.3f))) to (0 to 0),
                doc.withLayer(doc.layer("generator").copy(generator=doc.layer("generator").generator.copy(seed=81))) to (0 to 0),
                doc.withLayer(doc.layer("fixture").copy(transform=Transform(x=-.15f,angle=-19f))) to (1 to 0)
            )
            for((changed,counts)in variants){
                reads=0;textReads=0;val actual=renderMixed(root,changed)
                assertEquals(counts.first,reads);assertEquals(counts.second,textReads)
                assertArrayEquals(renderMixed(root,changed,cache=null),actual);assertFalse(original.contentEquals(actual))
            }
        }finally{root.deleteRecursively()}
    }
    @Test fun fontByteVersionsInvalidateTextAndUnknownFontsDisableTheirReuse(){
        val root=directory()
        try{
            val doc=Document(width=32,height=24).addLayer(mixedDocument().layer("text").copy(opacity=1f))
            val original=renderMixed(root,doc);textReads=0
            val changed=renderMixed(root,doc,fontVersion="font-v2");assertEquals(1,textReads)
            assertArrayEquals(renderMixed(root,doc,cache=null,fontVersion="font-v2"),changed);assertFalse(original.contentEquals(changed))
            textReads=0;renderMixed(root,doc,fontVersion=null);renderMixed(root,doc,fontVersion=null);assertEquals(2,textReads)
        }finally{root.deleteRecursively()}
    }
    @Test fun maskNodesCanBeLoadedBeforeTheirSourceIsEvaluated(){
        val root=directory()
        try{
            val base=Document(width=32,height=24).addLayer(image)
            val mask=Node(id="derived-mask",kind="mask",inputs=listOf("source-${image.id}"),layer=image.id,maskData=Mask(selection="luma",low=.2f,high=.8f))
            val doc=base.copy(nodes=base.nodes+mask,root=mask.id);val expected=render(root,doc)
            reads=0;assertArrayEquals(expected,render(root,doc));assertEquals(0,reads)
            val changed=doc.copy(nodes=doc.nodes.map{if(it.id==mask.id)it.copy(maskData=mask.maskData.copy(low=.6f,high=.9f))else it})
            reads=0;val actual=render(root,changed);assertEquals(1,reads)
            assertArrayEquals(render(root,changed,cached=false),actual);assertFalse(expected.contentEquals(actual))
        }finally{root.deleteRecursively()}
    }
    @Test fun plainSourcesAndIdentityAliasesDoNotCreateDuplicateRasterEntries(){
        val root=directory()
        try{
            val plain=Document(width=32,height=24).addLayer(image)
            val cases=listOf(plain,Document.canvas(32,24,0xff334455.toInt()),plain.addEffect(image.id,Effect("grain",enabled=false)),plain.addEffect(image.id,Effect("adjust")),plain.addEffect(image.id,Effect("curves")))
            for(doc in cases){render(root,doc);render(root,doc);assertTrue(File(root,"cache").listFiles().orEmpty().isEmpty())}
        }finally{root.deleteRecursively()}
    }
    @Test fun changingAnEffectVersionNeverLoadsItsOldRaster(){
        val root=directory()
        try{
            val layer=image.copy(transform=Transform(angle=11f))
            val doc=Document(width=32,height=24).addLayer(layer).addEffect(layer.id,Effect("random_pixelate",mapOf("size" to .25f,"variation" to .7f),seed=71))
            val first=renderMixed(root,doc);val id=doc.nodes.first{it.kind=="effect"}.id
            val changed=doc.copy(nodes=doc.nodes.map{if(it.id==id)it.copy(effect=Catalog.newEffect("random_pixelate").copy(seed=71,params=mapOf("depth" to 4f)))else it})
            reads=0;val actual=renderMixed(root,changed);assertEquals("The unchanged transformed source is still reusable",0,reads)
            assertArrayEquals(renderMixed(root,changed,cache=null),actual);assertFalse(first.contentEquals(actual))
        }finally{root.deleteRecursively()}
    }
    @Test fun cancellationAfterACacheHitNeverReturnsTheCachedResult(){
        val root=directory()
        try{
            val doc=mixedDocument();renderMixed(root,doc)
            val before=File(root,"cache").listFiles()!!.associate{it.name to it.readBytes()}
            val cache=NodeRasterCache(File(root,"cache"),"mixed")
            try{renderMixed(root,doc,cache,check={if(cache.hits>0)throw CancellationException()});fail("A cancelled hit must not publish a raster")}catch(_:CancellationException){}
            assertTrue(cache.hits>0)
            for((name,bytes)in before)assertArrayEquals(bytes,File(root,"cache/$name").readBytes())
            assertFalse(File(root,"cache").listFiles()!!.any{it.name.endsWith(".tmp")})
        }finally{root.deleteRecursively()}
    }
}
