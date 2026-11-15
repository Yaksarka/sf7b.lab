package lab.sf7b.core

import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files
import kotlin.math.abs

class ReferenceColorEffectsTest {
    @Test fun colorSwapUsesTheSelectedColorAndCanChangeItsAlpha() {
        val effect=Catalog.newEffect("color_swap").copy(params=mapOf("tolerance" to 0f,"hardness" to 1f),palette=listOf(0xffff0000.toInt(),0x800000ff.toInt()))
        assertEquals(Pixels.premul(0x800000ff.toInt()),ReferenceColorEffects.applyPixel(effect,0xffff0000.toInt()))
        assertEquals(0xff00ff00.toInt(),ReferenceColorEffects.applyPixel(effect,0xff00ff00.toInt()))
        assertEquals(effect,Codec.decode(Codec.encode(Document.canvas(2,2).let{it.addEffect(it.layers.first().id,effect)})).nodes.first{it.kind=="effect"}.effect)
    }
    @Test fun colorSwapInterpolatesStraightRgbaRatherThanPremultipliedChannelValues() {
        val effect=Catalog.newEffect("color_swap").copy(params=mapOf("tolerance" to 1f,"hardness" to 0f),palette=listOf(0xff000000.toInt(),0x00ffffff))
        val output=ReferenceColorEffects.applyPixel(effect,0xff808080.toInt())
        assertTrue(Pixels.a(output)in 127..129)
        assertTrue(Pixels.r(Pixels.straight(output))in 189..193)
    }
    @Test fun luminanceSelectionUsesObservedCoefficientsAndPreservesSelectedRgb() {
        val source=Pixels.premul(0x80ff0000.toInt())
        val matched=Catalog.newEffect("lum_select").copy(params=mapOf("target" to .2989f,"tolerance" to .0001f,"hardness" to 1f))
        assertEquals(source,ReferenceColorEffects.applyPixel(matched,source))
        val rec709=matched.copy(params=matched.params+("target" to .2126f))
        assertEquals(0,ReferenceColorEffects.applyPixel(rec709,source))
    }
    @Test fun zeroToleranceHasDefinedExactMatchBehavior() {
        val component=128/255f;val luminance=.2989f*component+.587f*component+.114f*component
        val effect=Catalog.newEffect("lum_select").copy(params=mapOf("target" to luminance,"tolerance" to 0f,"hardness" to .5f))
        assertEquals(0xff808080.toInt(),ReferenceColorEffects.applyPixel(effect,0xff808080.toInt()))
        assertEquals(0,ReferenceColorEffects.applyPixel(effect,0xff909090.toInt()))
    }
    @Test fun inkMatchesOpaqueReferenceBehaviorAndCanExplicitlyRetainSourceAlpha() {
        val input=Pixels.premul(0x8000ff00.toInt());val effect=Catalog.newEffect("ink")
        assertEquals(0xffffffff.toInt(),ReferenceColorEffects.applyPixel(effect,input))
        assertEquals(Pixels.premul(0x80ffffff.toInt()),ReferenceColorEffects.applyPixel(effect.copy(params=mapOf("preserve_alpha" to 1f)),input))
        assertEquals(0xff000000.toInt(),ReferenceColorEffects.applyPixel(effect,0xff000000.toInt()))
    }
    @Test fun channelGainNeutralClampAndZeroIntensityAreIdentityWithinOneRoundingStep() {
        val neutral=Catalog.newEffect("channel_multiplier")
        for(straight in listOf(0xffff0000.toInt(),0xff00ff00.toInt(),0xff0000ff.toInt(),0xffffffff.toInt(),0x804080c0.toInt())) {
            val input=Pixels.premul(straight);val output=ReferenceColorEffects.applyPixel(neutral,input)
            assertEquals(Pixels.a(input),Pixels.a(output))
            assertTrue(abs(Pixels.r(input)-Pixels.r(output))<=1);assertTrue(abs(Pixels.g(input)-Pixels.g(output))<=1);assertTrue(abs(Pixels.b(input)-Pixels.b(output))<=1)
            val zero=neutral.copy(params=mapOf("intensity" to 0f,"style" to 0f,"red" to -4f,"hue" to 3f))
            assertEquals(input,ReferenceColorEffects.applyPixel(zero,input))
        }
    }
    @Test fun wrapFoldAndHueMultiplierHaveDistinctEstablishedMeaning() {
        val effect=Catalog.newEffect("channel_multiplier")
        assertEquals(0xff000000.toInt(),ReferenceColorEffects.applyPixel(effect.copy(params=mapOf("style" to 0f)),0xffffffff.toInt()))
        assertEquals(0xffffffff.toInt(),ReferenceColorEffects.applyPixel(effect.copy(params=mapOf("style" to 1f)),0xffffffff.toInt()))
        val blue=ReferenceColorEffects.applyPixel(effect.copy(params=mapOf("hue" to 2f)),Pixels.premul(0x8000ff00.toInt()))
        assertEquals(128,Pixels.a(blue));assertTrue(Pixels.b(blue)>=127);assertTrue(Pixels.g(blue)<=1)
    }
    @Test fun colorPlaneRenderingChecksCancellationAndKeepsPremultiplication() {
        val directory=Files.createTempDirectory("sf7b-color").toFile()
        try{SurfacePool(directory).use{pool->
            val input=pool.create(8,8);for(y in 0 until 8)input.writeRow(y,IntArray(8){Pixels.premul(0x8040a0e0.toInt())});input.seal()
            for(id in ReferenceColorEffects.ids){val output=ReferenceColorEffects.render(pool,input,Catalog.newEffect(id))!!;for(y in 0 until 8)for(c in output.readRow(y)){assertTrue(Pixels.r(c)<=Pixels.a(c));assertTrue(Pixels.g(c)<=Pixels.a(c));assertTrue(Pixels.b(c)<=Pixels.a(c))}}
            var rows=0
            assertThrows(InterruptedException::class.java){ReferenceColorEffects.render(pool,input,Catalog.newEffect("ink")){if(++rows>2)throw InterruptedException()}}
        }}finally{directory.deleteRecursively()}
    }
}
