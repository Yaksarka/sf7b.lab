package lab.sf7b

import android.graphics.*
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import lab.sf7b.core.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.*
import java.security.MessageDigest
import java.util.zip.CRC32
import java.util.zip.DeflaterOutputStream
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class ImportColorTest {
    private fun root()=File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir,"import-color-${uid()}").also{File(it,"assets").mkdirs()}
    private fun write(project:File,bitmap:Bitmap,format:Bitmap.CompressFormat,name:String):String {
        val asset="assets/$name.image";File(project,asset).outputStream().use{assertTrue(bitmap.compress(format,100,it))};return asset
    }
    private fun pixel(project:File,asset:String):Int {
        val(raw,info)=ImageRepository(project,File(project,"cache")).ensure(asset)
        return SurfacePool(File(project,"read-${uid()}")).use{p->Pixels.straight(p.open(raw,info.width,info.height).get(0,0))}
    }
    private fun assertColor(expected:Int,actual:Int,tolerance:Int=2){
        assertTrue("alpha ${Color.alpha(actual)}",abs(Color.alpha(expected)-Color.alpha(actual))<=tolerance)
        for(channel in listOf<(Int)->Int>({Color.red(it)},{Color.green(it)},{Color.blue(it)}))assertTrue("expected=${expected.toUInt().toString(16)} actual=${actual.toUInt().toString(16)}",abs(channel(expected)-channel(actual))<=tolerance)
    }
    @Test fun opaqueSrgbGrayHasNoColorShiftThroughImportAndPngExport(){val dir=root();try{
        val bitmap=Bitmap.createBitmap(256,16,Bitmap.Config.ARGB_8888)
        for(x in 0..255)for(y in 0 until 16)bitmap.setPixel(x,y,Color.rgb(x,x,x))
        val asset=write(dir,bitmap,Bitmap.CompressFormat.PNG,"gray");bitmap.recycle()
        val layer=Layer(kind="image",asset=asset);val doc=Document(width=256,height=16).addLayer(layer)
        val out=ByteArrayOutputStream()
        SurfacePool(File(dir,"render")).use{p->val rendered=Renderer(p,AndroidSources(dir,p,ImageRepository(dir,File(dir,"cache")))).render(doc,256,16);PngWriter.write(out,256,16,{y->outputRow(doc,rendered,y,256,16)})}
        val bytes=out.toByteArray();val result=BitmapFactory.decodeByteArray(bytes,0,bytes.size)
        try{for(x in 0..255)assertEquals("gray=$x",Color.rgb(x,x,x),result.getPixel(x,0))}finally{result.recycle()}
    }finally{dir.deleteRecursively()}}
    @Test fun displayP3PngIsConvertedAutomaticallyToSrgb(){val dir=root();try{
        val space=ColorSpace.get(ColorSpace.Named.DISPLAY_P3)
        val bitmap=Bitmap.createBitmap(32,24,Bitmap.Config.ARGB_8888,true,space)
        val expected=0xffbc3559.toInt();Canvas(bitmap).drawColor(expected)
        val asset=write(dir,bitmap,Bitmap.CompressFormat.PNG,"display-p3");bitmap.recycle()
        val bounds=BitmapFactory.Options().apply{inJustDecodeBounds=true};BitmapFactory.decodeFile(File(dir,asset).path,bounds)
        assertFalse("Fixture must really carry a non-sRGB profile",bounds.outColorSpace?.isSrgb?:true)
        assertColor(expected,pixel(dir,asset),3)
    }finally{dir.deleteRecursively()}}
    @Test fun png16BitRgbaOpensAndRetainsAlpha(){val dir=root();try{
        val asset="assets/sixteen-bit.image";val bytes=ByteArrayOutputStream();val out=DataOutputStream(bytes)
        out.write(byteArrayOf(-119,80,78,71,13,10,26,10))
        fun chunk(name:String,data:ByteArray){val type=name.toByteArray(Charsets.US_ASCII);out.writeInt(data.size);out.write(type);out.write(data);val crc=CRC32();crc.update(type);crc.update(data);out.writeInt(crc.value.toInt())}
        val h=ByteArrayOutputStream();DataOutputStream(h).apply{writeInt(2);writeInt(1);writeByte(16);writeByte(6);writeByte(0);writeByte(0);writeByte(0)}
        chunk("IHDR",h.toByteArray());chunk("sRGB",byteArrayOf(0))
        val scan=ByteArrayOutputStream();DeflaterOutputStream(scan).use{z->val raw=ByteArrayOutputStream();DataOutputStream(raw).apply{writeByte(0);repeat(2){writeShort(65535);writeShort(32768);writeShort(0);writeShort(32768)}};z.write(raw.toByteArray())}
        chunk("IDAT",scan.toByteArray());chunk("IEND",byteArrayOf());File(dir,asset).writeBytes(bytes.toByteArray())
        assertColor(Color.argb(128,255,128,0),pixel(dir,asset),2)
    }finally{dir.deleteRecursively()}}
    @Test fun staticPngJpegAndWebpAreRecognizedWithoutFilenameExtension(){val dir=root();try{
        val bitmap=Bitmap.createBitmap(48,32,Bitmap.Config.ARGB_8888);bitmap.eraseColor(0xff7a4c29.toInt())
        @Suppress("DEPRECATION") val formats=mutableListOf(Bitmap.CompressFormat.PNG,Bitmap.CompressFormat.JPEG,Bitmap.CompressFormat.WEBP)
        if(Build.VERSION.SDK_INT>=30){formats+=Bitmap.CompressFormat.WEBP_LOSSLESS;formats+=Bitmap.CompressFormat.WEBP_LOSSY}
        formats.forEachIndexed{i,format->val asset=write(dir,bitmap,format,"format-$i");assertColor(0xff7a4c29.toInt(),pixel(dir,asset),5)}
        bitmap.recycle()
    }finally{dir.deleteRecursively()}}
    @Test fun adaptivePreviewDoesNotModifyOriginalOrDoubleRotateExif(){val dir=root();try{
        val bitmap=Bitmap.createBitmap(1200,600,Bitmap.Config.ARGB_8888);bitmap.eraseColor(Color.GREEN)
        val asset=write(dir,bitmap,Bitmap.CompressFormat.JPEG,"large-exif");bitmap.recycle()
        val file=File(dir,asset);ExifInterface(file).apply{setAttribute(ExifInterface.TAG_ORIENTATION,"6");saveAttributes()}
        val before=MessageDigest.getInstance("SHA-256").digest(file.readBytes())
        val repo=ImageRepository(dir,File(dir,"cache"));val(preview,p)=repo.ensure(asset,128)
        assertEquals(64,p.width);assertEquals(128,p.height);assertEquals(64L*128*4,preview.length())
        val(full,f)=repo.ensure(asset);assertEquals(600,f.width);assertEquals(1200,f.height);assertEquals(600L*1200*4,full.length())
        assertArrayEquals(before,MessageDigest.getInstance("SHA-256").digest(file.readBytes()))
    }finally{dir.deleteRecursively()}}
    @Test fun sharedDecodeCacheCannotSubstituteAnotherProjectsAsset(){val dir=root();try{
        val a=File(dir,"project-a").also{File(it,"assets").mkdirs()};val b=File(dir,"project-b").also{File(it,"assets").mkdirs()}
        val bitmap=Bitmap.createBitmap(32,24,Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.RED);val asset=write(a,bitmap,Bitmap.CompressFormat.PNG,"same-name")
        bitmap.eraseColor(Color.GREEN);write(b,bitmap,Bitmap.CompressFormat.PNG,"same-name");bitmap.recycle()
        File(a,asset).setLastModified(100000);File(b,asset).setLastModified(100000)
        val shared=File(dir,"shared-cache")
        val(first,ai)=ImageRepository(a,shared).ensure(asset);val(second,bi)=ImageRepository(b,shared).ensure(asset)
        assertNotEquals(first.canonicalPath,second.canonicalPath)
        SurfacePool(File(dir,"read-both")).use{p->assertEquals(Color.RED,Pixels.straight(p.open(first,ai.width,ai.height).get(0,0)));assertEquals(Color.GREEN,Pixels.straight(p.open(second,bi.width,bi.height).get(0,0)))}
    }finally{dir.deleteRecursively()}}
    @Test fun derivedCacheEvictionKeepsTheActiveRasterAndOriginalBytes(){val dir=root();try{
        val bitmap=Bitmap.createBitmap(32,24,Bitmap.Config.ARGB_8888);bitmap.eraseColor(Color.BLUE)
        val asset=write(dir,bitmap,Bitmap.CompressFormat.PNG,"eviction");bitmap.recycle()
        val source=File(dir,asset);val before=source.readBytes();val cache=File(dir,"decode-cache")
        val repository=ImageRepository(dir,cache);val(kept,_)=repository.ensure(asset)
        val stale=File(cache,"stale.rgba").also{it.writeBytes(ByteArray(2048));it.setLastModified(1)}
        repository.pruneDecodedCache(kept,kept.length())
        assertFalse(stale.exists());assertTrue(kept.exists());assertArrayEquals(before,source.readBytes())
    }finally{dir.deleteRecursively()}}
}
