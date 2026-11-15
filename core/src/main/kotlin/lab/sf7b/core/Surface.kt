package lab.sf7b.core

import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.*

/** Shared bounded raster memory. Small working planes stay in RAM; older planes spill to disk.
 * Large planes retain a bounded tile cache, so export never requires a bitmap per graph node. */
class SurfacePool(val directory:File,val tileSize:Int=128,val memoryBytes:Int=24*1024*1024,val residentPlanes:Boolean=true,val persistOnClose:Boolean=true):Closeable {
    private data class Key(val id:Int,val x:Int,val y:Int)
    private val cache=LinkedHashMap<Key,IntArray>(64,.75f,true)
    private val planes=LinkedHashMap<Surface,IntArray>()
    private var planeBytes=0L
    private val surfaces=mutableListOf<Surface>()
    init { require(tileSize in 8..512);require(memoryBytes>=0);directory.mkdirs() }
    fun create(w:Int,h:Int):Surface { require(w>0&&h>0&&w.toLong()*h<=240_000_000);val s=Surface(this,surfaces.size,w,h,File(directory,"${surfaces.size}.rgba"));surfaces+=s;return s }
    fun open(file:File,w:Int,h:Int):Surface {require(w>0&&h>0&&w.toLong()*h<=240_000_000);val s=Surface(this,surfaces.size,w,h,file,true);surfaces+=s;return s }
    internal fun allocate(s:Surface):IntArray? {
        val bytes=s.width.toLong()*s.height*4
        if(!residentPlanes||bytes>memoryBytes/2)return null
        // Reserve a quarter for tiled/evicted sources still used by another graph branch.
        val planeBudget=memoryBytes.toLong()*3/4
        while(planes.isNotEmpty()&&planeBytes+bytes>planeBudget){val old=planes.entries.first();old.key.spill();planeBytes-=old.value.size.toLong()*4;planes.remove(old.key)}
        trimTiles(bytes)
        return IntArray(s.width*s.height).also{planes[s]=it;planeBytes+=bytes}
    }
    private fun trimTiles(extra:Long=0){val iterator=cache.entries.iterator();while(iterator.hasNext()&&planeBytes+extra+cache.size.toLong()*tileSize*tileSize*4>memoryBytes){val key=iterator.next().key;iterator.remove();surfaces.getOrNull(key.id)?.forgetTile(key.x,key.y)}}
    internal fun tile(s:Surface,tx:Int,ty:Int):IntArray {
        val key=Key(s.id,tx,ty)
        cache[key]?.let{return it}
        val result=IntArray(tileSize*tileSize);val count=min(tileSize,s.width-tx*tileSize);val buf=ByteBuffer.allocate(count*4).order(ByteOrder.LITTLE_ENDIAN)
        for(j in 0 until min(tileSize,s.height-ty*tileSize)) {
            buf.clear();s.readFully(buf,((ty*tileSize+j).toLong()*s.width+tx*tileSize)*4);buf.flip();buf.asIntBuffer().get(result,j*tileSize,count)
        }
        cache[key]=result;trimTiles();return result
    }
    internal fun invalidate(s:Surface) { cache.keys.removeAll{it.id==s.id} }
    override fun close(){surfaces.forEach{it.close()};cache.clear();planes.clear();planeBytes=0}
}
class Surface internal constructor(private val pool:SurfacePool,val id:Int,val width:Int,val height:Int,val file:File,existing:Boolean=false):Closeable {
    private val handle=RandomAccessFile(file,if(existing)"r" else "rw")
    private val channel:FileChannel=handle.channel
    private var memory:IntArray?=null
    private var dirty=false
    private var closed=false
    private var lastX=-1;private var lastY=-1;private var lastTile:IntArray?=null
    init {
        if(!existing)handle.setLength(width.toLong()*height*4) else require(handle.length()==width.toLong()*height*4)
        memory=pool.allocate(this)
        if(existing)memory?.let{pixels->
            val buffer=ByteBuffer.allocate(min(pixels.size,16384)*4).order(ByteOrder.LITTLE_ENDIAN)
            var start=0
            while(start<pixels.size){val count=min(16384,pixels.size-start);buffer.clear();buffer.limit(count*4);readFully(buffer,start.toLong()*4);buffer.flip();buffer.asIntBuffer().get(pixels,start,count);start+=count}
        }
    }
    internal fun readFully(buf:ByteBuffer,position:Long){var p=position;while(buf.hasRemaining()){val n=channel.read(buf,p);if(n<0)throw EOFException("Truncated raster");p+=n}}
    private fun writeFully(buf:ByteBuffer,position:Long){var p=position;while(buf.hasRemaining())p+=channel.write(buf,p)}
    /** Explicitly persist a plane that must survive a nonpersistent scratch pool. */
    fun persist(){val pixels=memory?:return;if(dirty){val buffer=ByteBuffer.allocate(min(pixels.size,16384)*4).order(ByteOrder.LITTLE_ENDIAN);var start=0;while(start<pixels.size){val count=min(16384,pixels.size-start);buffer.clear();buffer.limit(count*4);buffer.asIntBuffer().put(pixels,start,count);writeFully(buffer,start.toLong()*4);start+=count};dirty=false}}
    internal fun spill(){persist();memory=null;pool.invalidate(this);lastTile=null;lastX=-1;lastY=-1}
    fun writeRow(y:Int,pixels:IntArray){require(y in 0 until height&&pixels.size==width);memory?.let{pixels.copyInto(it,y*width);dirty=true;return};val buf=ByteBuffer.allocate(width*4).order(ByteOrder.LITTLE_ENDIAN);buf.asIntBuffer().put(pixels);writeFully(buf,y.toLong()*width*4)}
    fun writeTile(x:Int,y:Int,w:Int,h:Int,pixels:IntArray){require(x>=0&&y>=0&&w>0&&h>0&&x+w<=width&&y+h<=height&&pixels.size>=w*h);memory?.let{target->for(j in 0 until h)pixels.copyInto(target,(y+j)*width+x,j*w,(j+1)*w);dirty=true;return};val buf=ByteBuffer.allocate(w*4).order(ByteOrder.LITTLE_ENDIAN);for(j in 0 until h){buf.clear();buf.asIntBuffer().put(pixels,j*w,w);writeFully(buf,((y+j).toLong()*width+x)*4)}}
    fun readRow(y:Int):IntArray {require(y in 0 until height);memory?.let{return it.copyOfRange(y*width,(y+1)*width)};val buf=ByteBuffer.allocate(width*4).order(ByteOrder.LITTLE_ENDIAN);readFully(buf,y.toLong()*width*4);buf.flip();return IntArray(width).also{buf.asIntBuffer().get(it)} }
    fun seal(){pool.invalidate(this);lastTile=null;lastX=-1;lastY=-1}
    internal fun forgetTile(x:Int,y:Int){if(lastX==x&&lastY==y){lastTile=null;lastX=-1;lastY=-1}}
    fun get(x:Int,y:Int,clamp:Boolean=true):Int {
        if(!clamp&&(x<0||y<0||x>=width||y>=height))return 0
        val xx=x.coerceIn(0,width-1);val yy=y.coerceIn(0,height-1)
        memory?.let{return it[yy*width+xx]}
        val t=pool.tileSize;val tx=xx/t;val ty=yy/t
        val pixels=if(tx==lastX&&ty==lastY)lastTile!!else pool.tile(this,tx,ty).also{lastX=tx;lastY=ty;lastTile=it}
        return pixels[(yy%t)*t+xx%t]
    }
    fun sample(x:Float,y:Float,clamp:Boolean=true):Int {
        val ix=floor(x).toInt();val iy=floor(y).toInt();val fx=x-ix;val fy=y-iy
        if(fx==0f&&fy==0f)return get(ix,iy,clamp)
        return Pixels.mix(Pixels.mix(get(ix,iy,clamp),get(ix+1,iy,clamp),fx),Pixels.mix(get(ix,iy+1,clamp),get(ix+1,iy+1,clamp),fx),fy)
    }
    override fun close(){if(closed)return;if(pool.persistOnClose)spill()else{memory=null;dirty=false;lastTile=null;lastX=-1;lastY=-1};closed=true;channel.close();handle.close()}
}
