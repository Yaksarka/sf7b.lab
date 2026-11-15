package lab.sf7b.core

import java.io.*
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

/** Streaming PNG RGBA8. Cancellation leaves only the caller's temporary file. */
object PngWriter {
    fun write(output:OutputStream,width:Int,height:Int,row:(Int)->IntArray,check:()->Unit={},progress:(Float)->Unit={}) {
        require(width>0&&height>0&&width.toLong()*height<=240_000_000L)
        val out=DataOutputStream(output);out.write(byteArrayOf(-119,80,78,71,13,10,26,10))
        fun chunk(type:String,bytes:ByteArray){out.writeInt(bytes.size);val t=type.toByteArray(Charsets.US_ASCII);out.write(t);out.write(bytes);val crc=CRC32();crc.update(t);crc.update(bytes);out.writeInt(crc.value.toInt())}
        val header=ByteArrayOutputStream();DataOutputStream(header).apply{writeInt(width);writeInt(height);writeByte(8);writeByte(6);writeByte(0);writeByte(0);writeByte(0)}
        chunk("IHDR",header.toByteArray());chunk("sRGB",byteArrayOf(0))
        val chunks=object:OutputStream(){val bytes=ByteArrayOutputStream(65536);override fun write(v:Int){bytes.write(v);if(bytes.size()>=65536)flush()};override fun write(b:ByteArray,o:Int,l:Int){bytes.write(b,o,l);if(bytes.size()>=65536)flush()};override fun flush(){if(bytes.size()>0){chunk("IDAT",bytes.toByteArray());bytes.reset()}}}
        val deflater=Deflater(4);val zip=DeflaterOutputStream(chunks,deflater,32768)
        try {
            val raw=ByteArray(width*4);var previous=ByteArray(width*4)
            val sub=ByteArray(width*4+1).also{it[0]=1};val up=ByteArray(width*4+1).also{it[0]=2}
            for(y in 0 until height){check();val pixels=row(y);require(pixels.size==width);for(x in 0 until width){val c=Pixels.straight(pixels[x]);val offset=x*4;raw[offset]=(c ushr 16).toByte();raw[offset+1]=(c ushr 8).toByte();raw[offset+2]=c.toByte();raw[offset+3]=(c ushr 24).toByte()}
                var subScore=0L;var upScore=0L
                for(i in raw.indices){val s=((raw[i].toInt() and 255)-(if(i>=4)raw[i-4].toInt() and 255 else 0)).toByte();val u=(raw[i]-previous[i]).toByte();sub[i+1]=s;up[i+1]=u;subScore+=kotlin.math.abs(s.toInt());upScore+=kotlin.math.abs(u.toInt())}
                zip.write(if(subScore<=upScore)sub else up);raw.copyInto(previous);progress((y+1).toFloat()/height)
            }
            zip.finish();chunks.flush();chunk("IEND",byteArrayOf());out.flush()
        } finally {deflater.end()}
    }
}
