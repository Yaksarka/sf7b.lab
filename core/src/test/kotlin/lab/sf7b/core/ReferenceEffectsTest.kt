package lab.sf7b.core

import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.abs

class ReferenceEffectsTest {
    private fun render(e:Effect,w:Int=16,h:Int=12,tile:Int=8,check:()->Unit={},alpha:Float=1f,referenceColor:Int?=null):IntArray {
        val directory=Files.createTempDirectory("sf7b-reference").toFile()
        try{return SurfacePool(directory,tile,memoryBytes=1024*1024).use{pool->
            val source=pool.create(w,h)
            for(y in 0 until h)source.writeRow(y,IntArray(w){x->Pixels.rgba(x.toFloat()/maxOf(1,w-1),y.toFloat()/maxOf(1,h-1),.2f,alpha)})
            source.seal()
            val reference=referenceColor?.let{color->pool.create(w,h).also{ref->for(y in 0 until h)ref.writeRow(y,IntArray(w){Pixels.premul(color)});ref.seal()}}
            val output=ReferenceEffects.render(pool,source,e,w,h,w,h,check,reference)!!
            IntArray(w*h).also{pixels->for(y in 0 until h)output.readRow(y).copyInto(pixels,y*w)}
        }}finally{directory.deleteRecursively()}
    }
    @Test fun noSplitPixelateSamplesTheWholeCellCenterRatherThanGeneratingNoise() {
        val effect=Effect("random_pixelate",mapOf("depth" to 0f,"regularity" to 100f,"intensity" to 0f),algorithmVersion=2)
        val pixels=render(effect)
        assertEquals(1,pixels.distinct().size)
        val center=Pixels.straight(pixels.first())
        assertTrue(Pixels.r(center)in 127..128);assertTrue(Pixels.g(center)in 127..128)
    }
    @Test fun recursivePixelatePreservesItsSeedPatternAcrossSerializationAndTileSizes() {
        val effect=Catalog.newEffect("random_pixelate").copy(params=mapOf("depth" to 6f,"regularity" to 25f),seed=12345678)
        val a=render(effect,64,48,8)
        val base=Document.canvas(64,48);val document=base.addEffect(base.layers.first().id,effect)
        val restored=Codec.decode(Codec.encode(document)).nodes.first{it.kind=="effect"}.effect
        assertArrayEquals(a,render(restored,64,48,32))
        assertFalse(a.contentEquals(render(effect.copy(seed=87654321),64,48,8)))
    }
    @Test fun patchworkNeutralIntensityIsIdentityAndScaleModeChangesTheImage() {
        val neutral=Catalog.newEffect("patchwork_break").copy(params=mapOf("intensity" to 0f,"count" to 1f,"thickness" to 0f))
        val pixels=render(neutral)
        for(y in 0 until 12)for(x in 0 until 16)assertEquals(Pixels.rgba(x/15f,y/11f,.2f,1f),pixels[y*16+x])
        assertFalse(pixels.contentEquals(render(neutral.copy(params=neutral.params+("intensity" to 60f)))))
    }
    @Test fun angularPickerChoosesTheNearestBoundaryColorInsteadOfSinusoidalBrightness() {
        val effect=Catalog.newEffect("low_freq_banding").copy(params=mapOf("count" to 4f,"cell_size" to 1f,"angle" to 0f,"reference" to 0f))
        val pixels=render(effect,4,4)
        val actual=Pixels.straight(pixels[1*4+1])
        // The point (1,1) is equally close in color to the left and top boundaries.
        // The four-ray clockwise order reaches the left boundary before the top one.
        assertEquals(0,Pixels.r(actual));assertEquals(85,Pixels.g(actual));assertEquals(51,Pixels.b(actual))
    }
    @Test fun legacyDocumentsKeepVersionOneAndNewNodesUseExplicitVersionTwo() {
        val old=Effect("patchwork_break",mapOf("amount" to .7f,"size" to .12f),seed=445)
        val base=Document.canvas(16,16);val d=base.addEffect(base.layers.first().id,old)
        val legacy=Codec.decode(Codec.encode(d).replace(",\"algorithmVersion\":1",""))
        assertEquals(1,legacy.nodes.first{it.kind=="effect"}.effect.algorithmVersion)
        assertEquals(.7f,legacy.nodes.first{it.kind=="effect"}.effect.p("amount"),0f)
        assertEquals(2,Catalog.newEffect("patchwork_break").algorithmVersion)
        assertThrows(IllegalArgumentException::class.java){validateEffect(old.copy(params=mapOf("depth" to 3f)))}
    }
    @Test fun referenceRenderHonorsCancellationBeforePublishingAllRows() {
        var calls=0
        assertThrows(InterruptedException::class.java){render(Catalog.newEffect("random_pixelate"),check={if(++calls>2)throw InterruptedException()})}
        assertEquals(3,calls)
    }
    @Test fun paletteAndBordersRetainSourceAlphaAndPremultiplication() {
        val effect=Catalog.newEffect("random_pixelate").copy(params=mapOf("palette" to 1f,"thickness" to 20f),palette=listOf(0xffff0000.toInt(),0xff00ff00.toInt()))
        val output=render(effect,alpha=.25f)
        for(pixel in output){assertEquals(64,Pixels.a(pixel));assertTrue(Pixels.r(pixel)<=64);assertTrue(Pixels.g(pixel)<=64);assertTrue(Pixels.b(pixel)<=64)}
    }
    @Test fun angularPickerSamplesSecondSourceAndUsesItsRgbaCandidates() {
        val effect=Catalog.newEffect("low_freq_banding").copy(params=mapOf("count" to 4f,"cell_size" to 1f))
        val color=0x8040a0e0.toInt()
        val own=render(effect,4,4);val external=render(effect,4,4,referenceColor=color)
        assertFalse(own.contentEquals(external))
        assertTrue(external.all{it==Pixels.premul(color)})
    }
    @Test fun lowFrequencySecondInputAndAlgorithmVersionSurvivePortableProject() {
        val dir=Files.createTempDirectory("sf7b-lowfreq-graph").toFile();val imported=Files.createTempDirectory("sf7b-lowfreq-import").toFile()
        try {
            val base=Document.canvas(32,32);val owner=base.layers.first().id
            val reference=Layer(name="Reference",color=0xff2090ff.toInt(),visible=false)
            val doc=base.addLayer(reference).addEffect(owner,Catalog.newEffect("low_freq_banding"))
            val effectNode=doc.nodes.first{it.kind=="effect"}
            val withSource2=doc.copy(nodes=doc.nodes.map{if(it.id==effectNode.id)it.copy(inputs=it.inputs+doc.layer(reference.id).head)else it})
            withSource2.validate()
            val bytes=ByteArrayOutputStream();ProjectFiles(dir).pack(withSource2,bytes)
            val restored=ProjectFiles(imported).unpack(bytes.toByteArray().inputStream())
            assertEquals(withSource2,restored)
            val node=restored.nodes.first{it.kind=="effect"}
            assertEquals(2,node.effect.algorithmVersion);assertEquals(2,node.inputs.size)
        } finally {dir.deleteRecursively();imported.deleteRecursively()}
    }
}
