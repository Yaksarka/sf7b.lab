package lab.sf7b

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import lab.sf7b.core.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class ToolsPersistenceTest {
    @Test fun numericInputAcceptsCommaButDoesNotCommitIntermediateOrInvalidValues() {
        assertEquals(-.25f,parseNumberInput(" -0,25 ",-1f,1f)!!,0f)
        for(text in listOf("","-",".","NaN","Infinity","1,2,3","2"))assertNull(text,parseNumberInput(text,-1f,1f))
        assertNull(parseNumberInput("1.5",0f,3f,integer=true))
        assertEquals(2f,parseNumberInput("2",0f,3f,integer=true)!!,0f)
        assertEquals(0x80ff3300.toInt(),parseColor("#80FF3300"))
        assertEquals(0xffff3300.toInt(),parseColor("FF3300"))
    }

    @Test fun cropPresetsDescribeFinalOutputEvenAfterQuarterTurns() {
        for(turn in 0..3)for(ratio in listOf(1f,.8f,9f/16,16f/9)) {
            val frame=cropFrameForAspect(Frame(turn=turn,flipX=true),1600,1200,ratio)
            val document=Document.canvas(1600,1200).copy(frame=frame)
            document.validate()
            assertTrue("turn=$turn ratio=$ratio",abs(document.outputWidth.toFloat()/document.outputHeight-ratio)<.003f)
            assertTrue(frame.flipX)
        }
    }

    @Test fun portableTextKeepsItsFontAndEditableMaskAfterOriginalAndSourceProjectAreRemoved() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val dir=File(context.cacheDir,"tool-persistence-${uid()}").also{it.mkdirs()}
        try {
            val source=File(dir,"input");File(source,"assets").mkdirs()
            val font=File("/system/fonts").listFiles().orEmpty().first{it.extension=="ttf"}
            val fontBytes=font.readBytes();File(source,"assets/font.ttf").writeBytes(fontBytes)
            val layer=Layer(kind="text",name="Text",text=TextData(text="Привет / sf7b",font="assets/font.ttf",size=.18f,outline=.003f),mask=Mask(base=.75f))
            val doc=Document(width=256,height=192).addLayer(layer).addEffect(layer.id,Effect("invert",seed=773))
            val library=lab.sf7b.core.ProjectLibrary(File(dir,"first-phone"))
            val original=library.create(doc,source,name="Cover")
            val copy=library.duplicate(original.id)
            library.delete(original.id);source.deleteRecursively()
            val bytes=ByteArrayOutputStream();library.exportProject(copy.id,bytes)
            val other=lab.sf7b.core.ProjectLibrary(File(dir,"second-phone"))
            val restored=other.importProject(bytes.toByteArray().inputStream())
            assertArrayEquals(fontBytes,File(restored.directory,"assets/font.ttf").readBytes())
            assertEquals("Привет / sf7b",restored.document.layers.single().text.text)
            assertEquals(.75f,restored.document.layers.single().mask!!.base,0f)
            assertEquals(773L,restored.document.nodes.first{it.kind=="effect"}.effect.seed)
            SurfacePool(File(dir,"render")).use{pool->
                val sourceProvider=AndroidSources(restored.directory,pool,ImageRepository(restored.directory,File(dir,"cache")))
                val surface=Renderer(pool,sourceProvider).render(restored.document,256,192)
                var visible=0;var transparent=0
                for(y in 0 until surface.height)for(pixel in surface.readRow(y)) {
                    if(Pixels.a(pixel)>0)visible++ else transparent++
                }
                assertTrue("Font produced no text",visible>20)
                assertTrue("Text layer should retain transparency",transparent>20)
            }
        } finally {dir.deleteRecursively()}
    }
}
