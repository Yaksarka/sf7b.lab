package lab.sf7b.core

import org.junit.Test
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import java.awt.*
import java.awt.image.BufferedImage
import java.io.*
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.math.*

class GraphicsTest {
    private val report=File(System.getProperty("sf7b.reportDir","build/verification")).also{it.mkdirs()}
    private fun fixture(kind:String):BufferedImage {
        val image=BufferedImage(256,192,BufferedImage.TYPE_INT_ARGB);val g=image.createGraphics()
        if(kind=="drawing"){g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.color=Color(240,85,40,170);g.fillOval(10,10,170,160);g.color=Color(20,180,240,200);g.fillRect(100,70,150,100);g.color=Color.WHITE;g.stroke=BasicStroke(9f);g.drawLine(10,180,220,5);g.font=Font("SansSerif",Font.BOLD,28);g.drawString("sf7b",100,58)}
        else for(y in 0 until 192)for(x in 0 until 256)image.setRGB(x,y,java.awt.Color(x,255-x,(y*255/191),if(x<32)0 else if(x<80)128 else 255).rgb)
        g.dispose();return image
    }
    private fun source(pool:SurfacePool,image:BufferedImage):SourceProvider=object:SourceProvider {
        override fun image(asset:String):Surface {val s=pool.create(image.width,image.height);for(y in 0 until image.height)s.writeRow(y,IntArray(image.width){Pixels.premul(image.getRGB(it,y))});s.seal();return s}
        override fun text(layer:Layer,width:Int,height:Int)=error("Native-only text renderer")
    }
    private fun render(effects:List<Effect>,fixture:BufferedImage,tile:Int=32):BufferedImage {
        val dir=Files.createTempDirectory("sf7b-visual").toFile()
        try{return SurfacePool(dir,tile).use{pool->val l=Layer(kind="image",asset="assets/fixture.image");var d=Document(width=256,height=192).addLayer(l);effects.forEach{d=d.addEffect(l.id,it)};val s=Renderer(pool,source(pool,fixture)).render(d,256,192);val image=BufferedImage(256,192,BufferedImage.TYPE_INT_ARGB);for(y in 0 until 192){val row=s.readRow(y);for(x in row.indices)image.setRGB(x,y,Pixels.straight(row[x]))};image}}finally{dir.deleteRecursively()}
    }
    private fun sheet(items:List<Pair<String,BufferedImage>>,file:String,columns:Int=4){val cw=256;val ch=222;val image=BufferedImage(columns*cw,ceil(items.size.toDouble()/columns).toInt()*ch,BufferedImage.TYPE_INT_RGB);val g=image.createGraphics();g.color=Color(25,25,28);g.fillRect(0,0,image.width,image.height);g.font=Font("SansSerif",Font.PLAIN,13)
        for((i,item)in items.withIndex()){val x=i%columns*cw;val y=i/columns*ch;for(yy in 0 until 192 step 12)for(xx in 0 until 256 step 12){g.color=if((xx/12+yy/12)%2==0)Color(70,70,73)else Color(90,90,93);g.fillRect(x+xx,y+30+yy,12,12)};g.color=Color.WHITE;g.drawString(item.first,x+8,y+20);g.drawImage(item.second,x,y+30,null)};g.dispose();ImageIO.write(image,"png",File(report,file))
    }
    @Test fun visualCatalogueAndPresetFixtures(){
        val documentation=buildString {
            append("# Каталог параметров sf7b.lab\n\nАвтоматически получен из Catalog.kt. Все ID стабильны в формате проекта v1.\n\n")
            append("Общие настройки каждого эффекта: enabled (вкл/выкл, по умолчанию true), mix (0…1, по умолчанию 1), seed (64-битное целое, по умолчанию 7), mask (маска, по умолчанию отсутствует). Цвета палитры — ARGB sRGB.\n\n")
            for(def in Catalog.effects){append("## ${def.id} — ${def.ru} / ${def.en}\n\n");append("Реализация: CPU. Контрактные проверки: реальные изменения пикселей, сериализация и независимость от размера блока. Сходство с Ilixa: ${if(def.priority)"не подтверждено; самостоятельный аналог"else "не заявляется"}.\n\n");if(def.params.isNotEmpty()){append("| ID | Параметр | Диапазон | По умолчанию | Единицы |\n|---|---|---|---|---|\n");for(p in def.params)append("| ${p.id} | ${p.ru} / ${p.en} | ${p.min}…${p.max} | ${p.default} | ${if(p.unit.isEmpty())if(p.integral)"целое"else "безразмерные"else p.unit} |\n")}else append("Нет отдельных числовых параметров.\n");if(def.id in listOf("gradient_map","palette","linear_gradient","radial_gradient","perlin","voronoi","stripes","checker"))append("\nРедактируемая палитра: 2…32 цвета; по умолчанию #FF101020, #FF246BCE, #FFF7CD86.\n");if(def.id=="curves")append("\nЧетыре кусочно-линейные кривые: общая RGB, R, G, B. Каждая содержит 2…32 точки (x/y в 0…1), исходно (0,0)→(1,1).\n");append("\n")}
        };File(report,"CATALOG_PARAMETERS.md").writeText(documentation)
        for(kind in listOf("photo","drawing","gradient-alpha")){val input=fixture(kind);val entries=mutableListOf("Original / $kind" to input);for(def in Catalog.effects){var e=Catalog.newEffect(def.id);if(def.id=="adjust")e=e.copy(params=mapOf("contrast" to .3f,"exposure" to .3f));if(def.id=="curves")e=e.copy(curves=listOf(listOf(Point(0f,0f),Point(.4f,.7f),Point(1f,1f)))+e.curves.drop(1));entries+=def.en to render(listOf(e),input)};sheet(entries,"catalogue-$kind.png")
            sheet(listOf("Original" to input)+Catalog.builtinPresets().map{p->p.validate();p.name.substringAfter(" / ") to render(p.effects,input)},"presets-$kind.png",3)
        }
        val input=fixture("photo");val entries=mutableListOf<Pair<String,BufferedImage>>()
        for(def in Catalog.effects.filter{it.priority})for((i,amount)in listOf(.2f,.5f,.85f).withIndex()){val params=when(def.id){"bloom"->mapOf("intensity" to amount*2,"blurRadius" to .02f);"random_pixelate"->mapOf("depth" to (3+i*3).toFloat());"patchwork_break"->mapOf("intensity" to amount*80);"low_freq_banding"->mapOf("cell_size" to amount);else->mapOf("intensity" to amount)};entries+="${def.en} / ${i+1}" to render(listOf(Catalog.newEffect(def.id).copy(params=params,seed=123)),input)}
        sheet(entries,"priority-variants.png",3)
    }
    @Test fun nonLocalOperationsHaveNoTileSeams(){val image=fixture("drawing");val effects=listOf(Effect("bloom",mapOf("radius" to .025f)),Effect("waves"),Effect("multi_distort",seed=9));val a=render(effects,image,16);val b=render(effects,image,128);var difference=0;val heat=BufferedImage(256,192,BufferedImage.TYPE_INT_RGB);for(y in 0 until 192)for(x in 0 until 256){val equal=a.getRGB(x,y)==b.getRGB(x,y);if(!equal)difference++;heat.setRGB(x,y,if(equal)Color.BLACK.rgb else Color.RED.rgb)};sheet(listOf("Tile 16" to a,"Tile 128" to b,"Difference" to heat),"tile-boundaries.png",3);assertEquals(0,difference)}
    @Test fun residentPlanesSpillAndReopenWithoutLosingPixels(){
        val dir=Files.createTempDirectory("sf7b-spill").toFile()
        try{
            val backing=SurfacePool(dir,memoryBytes=16*1024).use{pool->
                val first=pool.create(47,39)
                for(y in 0 until 39)first.writeRow(y,IntArray(47){x->Pixels.rgba(x/46f,y/38f,.3f,.6f)})
                first.seal();val expected=first.readRow(21)
                repeat(4){val other=pool.create(47,39);for(y in 0 until 39)other.writeRow(y,IntArray(47){0xff123456.toInt()});other.seal()}
                assertArrayEquals(expected,first.readRow(21))
                val patch=intArrayOf(0xff010203.toInt(),0xff040506.toInt());first.writeTile(3,4,2,1,patch);first.seal()
                assertEquals(patch[0],first.get(3,4));assertEquals(patch[1],first.get(4,4))
                first.file
            }
            SurfacePool(File(dir,"reopen")).use{p->val s=p.open(backing,47,39);assertEquals(0xff010203.toInt(),s.get(3,4));assertEquals(Pixels.rgba(46/46f,38/38f,.3f,.6f),s.get(46,38))}
        }finally{dir.deleteRecursively()}
    }
    @Test fun unpremultiplicationIsStableForEveryRepresentableAlphaChannel(){
        for(alpha in 1..255)for(value in 0..alpha){val c=Pixels.pack(alpha,value,value,value);assertEquals(c,Pixels.premul(Pixels.straight(c)))}
    }
    @Test fun scratchCloseDiscardsUnneededPlanesButExplicitPersistenceSurvives(){
        val dir=Files.createTempDirectory("sf7b-scratch").toFile()
        try{
            val files=SurfacePool(dir,memoryBytes=1024*1024,persistOnClose=false).use{p->
                val discard=p.create(32,24);val retained=p.create(32,24)
                for(y in 0 until 24){discard.writeRow(y,IntArray(32){0xff112233.toInt()});retained.writeRow(y,IntArray(32){0xff445566.toInt()})}
                discard.seal();retained.seal();retained.persist()
                assertEquals(0xff112233.toInt(),discard.get(10,10))
                discard.file to retained.file
            }
            SurfacePool(File(dir,"read")).use{p->assertEquals(0,p.open(files.first,32,24).get(10,10));assertEquals(0xff445566.toInt(),p.open(files.second,32,24).get(10,10))}
        }finally{dir.deleteRecursively()}
    }
    @Test fun identityAndNeutralCorrectionsPreserveEveryOpaqueGrayInExport(){
        val dir=Files.createTempDirectory("sf7b-identity").toFile()
        try{SurfacePool(dir).use{p->
            val src=p.create(256,8);for(y in 0 until 8)src.writeRow(y,IntArray(256){x->0xff000000.toInt()or(x shl 16)or(x shl 8)or x});src.seal()
            val provider=object:SourceProvider{override fun image(asset:String)=src;override fun text(layer:Layer,width:Int,height:Int)=error("unused")}
            val layer=Layer(kind="image",asset="assets/fixture.image");var doc=Document(width=256,height=8).addLayer(layer)
            for(effects in listOf(emptyList(),listOf(Effect("adjust")),listOf(Effect("curves")))){
                var candidate=doc;effects.forEach{candidate=candidate.addEffect(layer.id,it)}
                val s=Renderer(p,provider).render(candidate,256,8);val bytes=ByteArrayOutputStream()
                PngWriter.write(bytes,256,8,{y->outputRow(candidate,s,y,256,8)})
                val image=ImageIO.read(ByteArrayInputStream(bytes.toByteArray()))
                for(x in 0..255)assertEquals("Gray $x / $effects",src.get(x,0),image.getRGB(x,0))
            }
        }}finally{dir.deleteRecursively()}
    }
    @Test fun residentAndTiledRasterPathsMatchAndRecordHostTimings(){
        fun run(resident:Boolean,side:Int):Pair<IntArray,Double>{
            val dir=Files.createTempDirectory("sf7b-raster-timing").toFile()
            var start=0L
            try{val pixels=SurfacePool(dir,memoryBytes=16*1024*1024,residentPlanes=resident,persistOnClose=false).use{p->
                val layer=Layer(kind="image",asset="assets/fixture.image")
                val d=Document(width=side,height=side).addLayer(layer).addEffect(layer.id,Effect("rgb_split",mapOf("amount" to .025f,"angle" to 20f))).addEffect(layer.id,Effect("adjust",mapOf("brightness" to .1f)))
                val src=p.create(side,side);for(y in 0 until side)src.writeRow(y,IntArray(side){x->Pixels.rgba(x.toFloat()/(side-1),y.toFloat()/(side-1),.4f,if(x<side/4).5f else 1f)});src.seal()
                val provider=object:SourceProvider{override fun image(asset:String)=src;override fun text(layer:Layer,width:Int,height:Int)=error("unused")}
                start=System.nanoTime();val output=Renderer(p,provider).render(d,side,side)
                val pixels=IntArray(side*side);for(y in 0 until side)output.readRow(y).copyInto(pixels,y*side)
                pixels
            };return pixels to (System.nanoTime()-start)/1e9}finally{dir.deleteRecursively()}
        }
        val tiled=run(false,256);val resident=run(true,256);assertArrayEquals(tiled.first,resident.first)
        val file=File(report,"raster-path-host.csv");file.writeText("mode,side,run,seconds,scope\n")
        for(mode in listOf(false,true))repeat(3){i->val result=run(mode,1024);file.appendText("${if(mode)"resident"else"tiled"},1024,$i,${result.second},JVM-render-readRows-scratchClose-not-A51\n")}
    }
    @Test fun versionTwoBloomCombineNormalizationAndAlphaFollowTheirDefinedSemantics(){
        val image=BufferedImage(256,192,BufferedImage.TYPE_INT_ARGB)
        for(y in 0 until image.height)for(x in 0 until image.width)image.setRGB(x,y,0xff808080.toInt())
        val base=Catalog.newEffect("bloom").copy(params=mapOf("intensity" to 1f,"tolerance" to 1f,"hardness" to 1f,"balance" to 0f,"normalization" to 0f))
        val added=render(listOf(base),image);assertEquals(0xffffffff.toInt(),added.getRGB(128,96))
        val normalized=render(listOf(base.copy(params=base.params+("normalization" to 1f))),image);assertEquals(0xff808080.toInt(),normalized.getRGB(128,96))
        val blended=render(listOf(base.copy(params=base.params+("balance" to 1f))),image);assertEquals(0xff808080.toInt(),blended.getRGB(128,96))
        val alphaImage=fixture("gradient-alpha");val result=render(listOf(base),alphaImage)
        for(y in 0 until 192)for(x in 0 until 256)assertEquals(alphaImage.getRGB(x,y) ushr 24,result.getRGB(x,y) ushr 24)
        assertArrayEquals(alphaImage.getRGB(0,0,256,192,null,0,256).map{Pixels.premul(it)}.toIntArray(),render(listOf(base.copy(params=base.params+("intensity" to 0f))),alphaImage).getRGB(0,0,256,192,null,0,256).map{Pixels.premul(it)}.toIntArray())
    }
    @Test fun versionTwoMultiDistortIsSeededAndItsLightingDoesNotChangeAlpha(){
        val input=fixture("gradient-alpha");val effect=Catalog.newEffect("multi_distort").copy(seed=12345)
        val first=render(listOf(effect),input);val second=render(listOf(effect),input,128)
        assertArrayEquals(first.getRGB(0,0,256,192,null,0,256),second.getRGB(0,0,256,192,null,0,256))
        val changed=render(listOf(effect.copy(seed=54321)),input)
        assertFalse(first.getRGB(0,0,256,192,null,0,256).contentEquals(changed.getRGB(0,0,256,192,null,0,256)))
        val lit=render(listOf(effect.copy(params=mapOf("lighting" to .3f))),input)
        for(y in 0 until 192)for(x in 0 until 256)assertEquals(first.getRGB(x,y) ushr 24,lit.getRGB(x,y) ushr 24)
        val neutral=render(listOf(effect.copy(params=mapOf("intensity" to 0f))),input)
        for(y in 0 until 192)for(x in 0 until 256)assertEquals(Pixels.premul(input.getRGB(x,y)),Pixels.premul(neutral.getRGB(x,y)))
    }
    @Test fun largeCanvasBenchmarks(){assumeTrue(System.getProperty("sf7b.benchmark")=="true")
        val output=File(report,"desktop-performance.csv");output.writeText("width,height,effects,render_seconds,png_seconds,png_bytes,heap_used_mb\n")
        val sizes=listOf(3000 to 3000,4000 to 3000,8000 to 6000)
        for((width,height)in sizes)for(count in listOf(1,5,10)){
            val dir=Files.createTempDirectory("sf7b-bench").toFile();val target=File(report,"benchmark-${width}x$height-$count.png")
            try{SurfacePool(dir,memoryBytes=24*1024*1024).use{pool->val layer=Layer(kind="generator",generator=Effect("linear_gradient"));var d=Document(width=width,height=height).addLayer(layer);val ids=listOf("rgb_split","patchwork_break","low_freq_banding","multi_distort","random_pixelate","adjust","grain","bloom","posterize","kaleidoscope");for(i in 0 until count)d=d.addEffect(layer.id,Effect(ids[i]));val start=System.nanoTime();val surface=Renderer(pool,source(pool,fixture("drawing"))).render(d,width,height);val end=System.nanoTime();target.outputStream().use{PngWriter.write(it,width,height,{y->surface.readRow(y)})};val finish=System.nanoTime();val runtime=Runtime.getRuntime();output.appendText("$width,$height,$count,${(end-start)/1e9},${(finish-end)/1e9},${target.length()},${(runtime.totalMemory()-runtime.freeMemory())/1024/1024}\n");val decoded=ImageIO.read(target);assertEquals(width,decoded.width);assertEquals(height,decoded.height)}}finally{dir.deleteRecursively()}
        }
    }
}
