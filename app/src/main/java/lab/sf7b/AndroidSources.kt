package lab.sf7b

import android.graphics.*
import androidx.exifinterface.media.ExifInterface
import lab.sf7b.core.*
import java.io.File
import kotlin.math.*

data class ImageInfo(val width:Int,val height:Int,val orientation:Int,val rawWidth:Int,val rawHeight:Int)
class ImageRepository(private val project:File,private val cache:File,private val check:()->Unit={}) {
    init{cache.mkdirs()}
    fun inspect(file:File):ImageInfo {
        val opts=BitmapFactory.Options().apply{inJustDecodeBounds=true}
        BitmapFactory.decodeFile(file.path,opts)
        require(opts.outWidth>0&&opts.outHeight>0) { "Cannot decode image / Не удалось прочитать изображение" }
        require(opts.outMimeType in listOf("image/jpeg","image/png","image/webp")) { "Use JPEG, PNG or static WebP / Нужен JPEG, PNG или статический WebP" }
        require(opts.outWidth.toLong()*opts.outHeight<=240_000_000L&&max(opts.outWidth,opts.outHeight)<=30000){"Image exceeds 240 MP / Изображение превышает 240 Мп"}
        // A profile is input metadata, not a reason to reject a decodable image.
        // Actual conversion is performed by the platform decoder/managed Canvas below.
        if(opts.outMimeType=="image/webp") {
            val bytes=ByteArray(32);val count=file.inputStream().use{it.read(bytes)}
            require(!(count>20&&String(bytes,12,4,Charsets.US_ASCII)=="VP8X"&&(bytes[20].toInt() and 2)!=0)) { "Animated WebP is not supported / Анимированный WebP не поддерживается" }
        }
        val orientation=runCatching{ExifInterface(file).getAttributeInt(ExifInterface.TAG_ORIENTATION,1)}.getOrDefault(1)
        val turn=orientation in 5..8
        return ImageInfo(if(turn)opts.outHeight else opts.outWidth,if(turn)opts.outWidth else opts.outHeight,orientation,opts.outWidth,opts.outHeight)
    }
    fun ensure(asset:String,maxSide:Int=Int.MAX_VALUE):Pair<File,ImageInfo> {
        require(safeAsset(asset));require(maxSide>0)
        val source=File(project,asset);val original=inspect(source)
        val scale=min(1.0,maxSide.toDouble()/max(original.width,original.height))
        val width=max(1,(original.width*scale).roundToInt());val height=max(1,(original.height*scale).roundToInt())
        val info=original.copy(width=width,height=height)
        // Version the decoded representation so old incorrectly decoded planes are never reused.
        val sourceKey=java.security.MessageDigest.getInstance("SHA-256").digest(source.canonicalPath.toByteArray(Charsets.UTF_8)).take(12).joinToString(""){"%02x".format(it)}
        val final=File(cache,"$sourceKey-${File(asset).name}-${source.length()}-${source.lastModified()}-srgb-v2-${width}x$height.rgba")
        if(final.length()==width.toLong()*height*4){final.setLastModified(System.currentTimeMillis());pruneDecodedCache(final);return final to info}
        pruneDecodedCache(limitBytes=(384L*1024*1024-width.toLong()*height*4).coerceAtLeast(0))
        require(cache.usableSpace>width.toLong()*height*4+32L*1024*1024) { "Not enough storage / Недостаточно места" }
        val work=File(cache,"decode-${uid()}");val pool=SurfacePool(work,memoryBytes=8*1024*1024,persistOnClose=false)
        try {
            val surface=pool.create(width,height)
            if(width.toLong()*height<=16_000_000L){
                val bitmap=decodeBitmap(source,original,width,height)
                try{val row=IntArray(width);for(y in 0 until height){check();bitmap.getPixels(row,0,width,0,y,width,1);for(x in row.indices)row[x]=Pixels.premul(row[x]);surface.writeRow(y,row)}}finally{bitmap.recycle()}
            }else{
                // Large exports use bounded regions; source bytes and original resolution stay intact.
                require(scale==1.0){"Cannot allocate image / Недостаточно памяти для изображения"}
                decodeRegions(source,original,surface)
            }
            surface.seal();surface.persist();pool.close();atomicReplace(surface.file,final);pruneDecodedCache(final)
        }finally{runCatching{pool.close()};work.deleteRecursively()}
        return final to info
    }
    /** Derived rasters are disposable; originals/fonts/projects are never touched. A single active
     * raster larger than the budget is retained until the next cache operation replaces it. */
    internal fun pruneDecodedCache(keep:File?=null,limitBytes:Long=384L*1024*1024){
        val files=cache.listFiles()?.filter{it.isFile&&it.extension=="rgba"}?:return
        var total=files.sumOf{it.length()};val protected=keep?.canonicalPath
        for(file in files.sortedBy{it.lastModified()}){if(total<=limitBytes)break;if(file.canonicalPath==protected)continue;val size=file.length();if(file.delete())total-=size}
    }
    private fun decodeBitmap(file:File,info:ImageInfo,width:Int,height:Int):Bitmap {
        if(android.os.Build.VERSION.SDK_INT>=28){
            try {
                val decoded=ImageDecoder.decodeBitmap(ImageDecoder.createSource(file)){decoder,_,_->
                    decoder.allocator=ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.setTargetColorSpace(ColorSpace.get(ColorSpace.Named.SRGB))
                    // ImageDecoder applies EXIF orientation itself; do not apply it again.
                    decoder.setTargetSize(width,height)
                }
                return normalized(decoded,width,height)
            }catch(_:Exception){check()}
        }
        val opts=BitmapFactory.Options().apply{
            inPreferredConfig=Bitmap.Config.ARGB_8888;inPreferredColorSpace=ColorSpace.get(ColorSpace.Named.SRGB);inScaled=false
            val rawTargetW=if(info.orientation in 5..8)height else width
            val rawTargetH=if(info.orientation in 5..8)width else height
            var sample=1;while(info.rawWidth/(sample*2)>=rawTargetW&&info.rawHeight/(sample*2)>=rawTargetH)sample*=2
            inSampleSize=sample
        }
        var bitmap=BitmapFactory.decodeFile(file.path,opts)?:BitmapFactory.decodeFile(file.path)?:error("Image decoder failed / Не удалось декодировать изображение")
        if(info.orientation!=1){
            val matrix=Matrix().apply{when(info.orientation){2->setScale(-1f,1f);3->setRotate(180f);4->setScale(1f,-1f);5->{setRotate(90f);postScale(-1f,1f)};6->setRotate(90f);7->{setRotate(90f);postScale(1f,-1f)};8->setRotate(-90f)}}
            val rotated=Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,matrix,true)
            if(rotated!==bitmap)bitmap.recycle();bitmap=rotated
        }
        return normalized(bitmap,width,height)
    }
    private fun normalized(input:Bitmap,width:Int,height:Int):Bitmap {
        var bitmap=input
        if(bitmap.width!=width||bitmap.height!=height){val scaled=Bitmap.createScaledBitmap(bitmap,width,height,true);if(scaled!==bitmap)bitmap.recycle();bitmap=scaled}
        if(bitmap.colorSpace?.isSrgb==false){
            // Draw into a real sRGB destination; changing a color-space label is not conversion.
            val converted=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888,true,ColorSpace.get(ColorSpace.Named.SRGB))
            Canvas(converted).drawBitmap(bitmap,0f,0f,Paint())
            bitmap.recycle();bitmap=converted
        }
        return bitmap
    }
    @Suppress("DEPRECATION") private fun decodeRegions(file:File,info:ImageInfo,surface:Surface) {
        val decoder=BitmapRegionDecoder.newInstance(file.path,false)?:error("Image region decoder failed / Не удалось декодировать большое изображение")
        try{
            val opts=BitmapFactory.Options().apply{inPreferredConfig=Bitmap.Config.ARGB_8888;inPreferredColorSpace=ColorSpace.get(ColorSpace.Named.SRGB)}
            val tile=256
            for(y in 0 until info.rawHeight step tile)for(x in 0 until info.rawWidth step tile){
                check();val tw=min(tile,info.rawWidth-x);val th=min(tile,info.rawHeight-y)
                val decoded=decoder.decodeRegion(Rect(x,y,x+tw,y+th),opts)?:error("Image region failed")
                val bmp=normalized(decoded,tw,th);val pixels=IntArray(tw*th)
                try{bmp.getPixels(pixels,0,tw,0,0,tw,th)}finally{bmp.recycle()}
                if(info.orientation==1){for(i in pixels.indices)pixels[i]=Pixels.premul(pixels[i]);surface.writeTile(x,y,tw,th,pixels);continue}
                fun destX(px:Int,py:Int)=when(info.orientation){2,3->info.rawWidth-1-px;5,8->py;6,7->info.rawHeight-1-py;else->px}
                fun destY(px:Int,py:Int)=when(info.orientation){3,4->info.rawHeight-1-py;5,6->px;7,8->info.rawWidth-1-px;else->py}
                val left=min(destX(x,y),destX(x+tw-1,y+th-1));val top=min(destY(x,y),destY(x+tw-1,y+th-1))
                val dw=if(info.orientation in 5..8)th else tw;val dh=if(info.orientation in 5..8)tw else th;val out=IntArray(dw*dh)
                for(j in 0 until th)for(i in 0 until tw)out[(destY(x+i,y+j)-top)*dw+destX(x+i,y+j)-left]=Pixels.premul(pixels[j*tw+i])
                surface.writeTile(left,top,dw,dh,out)
            }
        }finally{decoder.recycle()}
    }
}
class AndroidSources(private val project:File,private val pool:SurfacePool,private val repo:ImageRepository,private val check:()->Unit={}):SourceProvider {
    override fun resourceVersion(asset:String):String? {
        if(asset.isEmpty())return "android-sans-v1:${android.os.Build.FINGERPRINT}"
        val file=File(project,asset)
        return if(file.isFile)"${file.canonicalPath}:${file.length()}:${file.lastModified()}" else null
    }
    private val images=hashMapOf<String,Surface>()
    override fun image(asset:String):Surface=images.getOrPut(asset){val(f,info)=repo.ensure(asset);pool.open(f,info.width,info.height)}
    override fun image(asset:String,width:Int,height:Int):Surface=images.getOrPut("$asset@${max(width,height)}"){val(f,info)=repo.ensure(asset,max(width,height));pool.open(f,info.width,info.height)}
    override fun text(layer:Layer,width:Int,height:Int):Surface {
        val surface=pool.create(width,height);val data=layer.text
        val typeface=if(data.font.isNotEmpty())Typeface.createFromFile(File(project,data.font))else Typeface.create("sans-serif",Typeface.NORMAL)
        val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply{this.typeface=typeface;textSize=data.size*max(width,height);color=data.color}
        val lines=data.text.split('\n');val lineHeight=paint.fontSpacing*data.lineSpacing
        val startY=(height-lines.size*lineHeight)/2-paint.ascent()
        val tile=256
        for(y in 0 until height step tile)for(x in 0 until width step tile){check();val tw=min(tile,width-x);val th=min(tile,height-y);val bitmap=Bitmap.createBitmap(tw,th,Bitmap.Config.ARGB_8888);val canvas=Canvas(bitmap);canvas.translate(-x.toFloat(),-y.toFloat())
            for((index,line)in lines.withIndex()){
                val spacing=data.letterSpacing*paint.textSize;val lineWidth=paint.measureText(line)+max(0,line.length-1)*spacing
                val left=when(data.align){"left"->width*.05f;"right"->width*.95f-lineWidth;else->(width-lineWidth)/2}
                fun draw(){var px=left;for(char in line){canvas.drawText(char.toString(),px,startY+index*lineHeight,paint);px+=paint.measureText(char.toString())+spacing}}
                paint.clearShadowLayer()
                if(data.outline>0){paint.style=Paint.Style.STROKE;paint.strokeJoin=Paint.Join.ROUND;paint.strokeWidth=data.outline*max(width,height);paint.color=data.outlineColor;draw()}
                paint.style=Paint.Style.FILL;paint.color=data.color
                if(data.shadow>0){paint.setShadowLayer(data.shadow*max(width,height),data.shadow*max(width,height)*.4f,data.shadow*max(width,height)*.4f,Color.BLACK)}
                draw();paint.clearShadowLayer()
            }
            val pixels=IntArray(tw*th);bitmap.getPixels(pixels,0,tw,0,0,tw,th);bitmap.recycle();for(i in pixels.indices)pixels[i]=Pixels.premul(pixels[i]);surface.writeTile(x,y,tw,th,pixels)
        };surface.seal();return surface
    }
}

