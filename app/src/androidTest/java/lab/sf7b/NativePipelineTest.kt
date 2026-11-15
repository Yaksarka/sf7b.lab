package lab.sf7b

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.exifinterface.media.ExifInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import lab.sf7b.core.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.*

@RunWith(AndroidJUnit4::class)
class NativePipelineTest {
    private val context get()=InstrumentationRegistry.getInstrumentation().targetContext
    private fun root()=File(context.cacheDir,"native-test-${uid()}").also{it.mkdirs()}
    private fun fixture(project:File,name:String="input.image",jpeg:Boolean=false):String {
        val file=File(project,"assets/$name");file.parentFile!!.mkdirs();val bmp=Bitmap.createBitmap(160,120,Bitmap.Config.ARGB_8888)
        val pixels=IntArray(160*120){i->val x=i%160;val y=i/160;Color.argb(if(jpeg)255 else if(x<25)0 else if(x<60)128 else 255,if(x<80)240 else 20,if(y<60)35 else 220,if((x/20+y/20)%2==0)190 else 40)}
        bmp.setPixels(pixels,0,160,0,0,160,120);file.outputStream().use{assertTrue(bmp.compress(if(jpeg)Bitmap.CompressFormat.JPEG else Bitmap.CompressFormat.PNG,100,it))};bmp.recycle();return "assets/$name"
    }
    @Test fun allExifOrientationsDecodeWithCorrectDimensionsAndCorners(){val dir=root();try{
        for(orientation in 1..8){val path=fixture(dir,"exif-$orientation.image",true);val f=File(dir,path);ExifInterface(f).apply{setAttribute(ExifInterface.TAG_ORIENTATION,orientation.toString());saveAttributes()}
            val repo=ImageRepository(dir,File(dir,"cache"));val(raw,info)=repo.ensure(path);assertEquals(if(orientation>=5)120 else 160,info.width);assertEquals(if(orientation>=5)160 else 120,info.height)
            SurfacePool(File(dir,"read-$orientation")).use{pool->val s=pool.open(raw,info.width,info.height);val expectedX=when(orientation){2,3,7,8->150;else->10};val expectedY=when(orientation){3,4,6,7->110;else->10};val c=Pixels.straight(s.get(10,10));assertEquals(expectedX<80,Color.red(c)>100);assertEquals(expectedY>=60,Color.green(c)>100)}
        }
    }finally{dir.deleteRecursively()}}
    @Test fun transparentTextMaskDrawingProjectAndPngRoundTrip(){val dir=root();val imported=root();try{val path=fixture(dir);var d=Document(width=300,height=300).addLayer(Layer(kind="image",asset=path,name="Photo"));val photo=d.layers[0].id
        listOf("patchwork_break","low_freq_banding","multi_distort","random_pixelate","bloom").forEach{d=d.addEffect(photo,Effect(it,seed=12345))}
        d=d.withLayer(d.layer(photo).copy(mask=Mask(strokes=listOf(Stroke(listOf(Point(.5f,.2f),Point(.5f,.8f)),size=.2f,erase=true))),blend="Screen"))
        val font=File("/system/fonts").listFiles()!!.first{it.extension=="ttf"};val fontAsset="assets/font.ttf";font.copyTo(File(dir,fontAsset))
        d=d.addLayer(Layer(kind="text",text=TextData(text="Привет / sf7b",font=fontAsset,size=.1f,outline=.003f,shadow=.01f),transform=Transform(y=.2f)))
        d=d.addLayer(Layer(kind="drawing",blend="Overlay",strokes=listOf(Stroke(listOf(Point(.1f,.1f),Point(.9f,.1f)),color=0x80ff3030.toInt(),size=.1f,softness=.5f))))
        val bytes=ByteArrayOutputStream();ProjectFiles(dir).pack(d,bytes);val restored=ProjectFiles(imported).unpack(ByteArrayInputStream(bytes.toByteArray()));assertEquals(d,restored)
        val output=File(dir,"result.png");SurfacePool(File(dir,"render")).use{pool->val sources=AndroidSources(imported,pool,ImageRepository(imported,File(dir,"cache")));val s=Renderer(pool,sources).render(restored,300,300);output.outputStream().use{PngWriter.write(it,300,300,{y->outputRow(restored,s,y,300,300)})}}
        val bmp=BitmapFactory.decodeFile(output.path);assertEquals(300,bmp.width);assertEquals(300,bmp.height);assertTrue(bmp.hasAlpha());val pixels=IntArray(90000);bmp.getPixels(pixels,0,300,0,0,300,300);assertTrue(pixels.any{Color.alpha(it)==0});assertTrue(pixels.any{Color.alpha(it)>0});assertTrue(pixels.any{Color.alpha(it)in 1..254});bmp.recycle()
        val results=File(context.filesDir,"test-results");results.mkdirs();output.copyTo(File(results,"native-roundtrip.png"),overwrite=true)
    }finally{dir.deleteRecursively();imported.deleteRecursively()}}
    @Test fun manifestRequiresNoInternetOrStoragePermissions(){@Suppress("DEPRECATION") val permissions=context.packageManager.getPackageInfo(context.packageName,PackageManager.GET_PERMISSIONS).requestedPermissions?.toList()?:emptyList();assertFalse(permissions.contains("android.permission.INTERNET"));assertFalse(permissions.contains("android.permission.READ_EXTERNAL_STORAGE"));assertFalse(permissions.contains("android.permission.WRITE_EXTERNAL_STORAGE"))}
    @Test fun staticWebpAndPngUseSrgbAndRetainAlpha(){val dir=root();try{val bmp=Bitmap.createBitmap(32,24,Bitmap.Config.ARGB_8888);bmp.eraseColor(0x80a0ff00.toInt());for(format in listOf(Bitmap.CompressFormat.PNG,Bitmap.CompressFormat.WEBP)){val path="assets/${uid()}.image";val f=File(dir,path);f.parentFile!!.mkdirs();f.outputStream().use{bmp.compress(format,100,it)};val(raw,info)=ImageRepository(dir,File(dir,"cache")).ensure(path);assertEquals(32,info.width);SurfacePool(File(dir,"r-${uid()}")).use{p->val s=p.open(raw,info.width,info.height);assertTrue(Pixels.a(s.get(10,10))in 126..130)}};bmp.recycle()}finally{dir.deleteRecursively()}}
}
