package lab.sf7b

import android.opengl.*
import android.util.Log
import lab.sf7b.core.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

/** GLES3 fast path for a single-image linear graph. Unsupported documents return null before
 * creating an EGL context. Intermediate effects stay on the GPU; only the final plane is read back.
 * The CPU renderer remains the compatibility path and handles all other graph/document features. */
object GpuRenderer {
    private val supported=setOf("adjust","curves","bloom","invert","threshold","posterize","rgb_split","scanlines")
    private fun neutral(effect:Effect):Boolean = when(effect.id){
        "adjust"->effect.params.all{(id,value)->value==Catalog.byId("adjust").params.first{it.id==id}.default}
        "curves"->effect.curves.all{it==listOf(Point(0f,0f),Point(1f,1f))}
        "bloom"->effect.algorithmVersion==2&&effect.p("intensity")==0f
        else->false
    }
    fun canAccelerate(doc:Document):Boolean=chain(doc)?.any{!neutral(it)}==true
    private fun chain(doc:Document):List<Effect>? {
        if(doc.layers.size!=1)return null
        val layer=doc.layers.single()
        if(layer.kind!="image"||layer.transform!=Transform()||layer.mask!=null||!layer.visible||layer.opacity!=1f||layer.blend!="Normal")return null
        val nodes=doc.nodes.associateBy{it.id};val result=mutableListOf<Effect>();val seen=hashSetOf<String>()
        var id=doc.root
        while(true){
            if(!seen.add(id))return null
            val node=nodes[id]?:return null
            if(node.maskInput.isNotEmpty())return null
            when(node.kind){
                "source"->return if(node.layer==layer.id)result.reversed()else null
                "effect"->{val e=node.effect;if(e.enabled&&e.mix>0){if(e.id !in supported||e.mask!=null||(e.algorithmVersion!=1&&!(e.id=="bloom"&&e.algorithmVersion==2)))return null;result+=e}}
                "layer"->if(node.layer!=layer.id)return null
                "blend"->if(node.opacity!=1f||node.blend!="Normal")return null
                else->return null
            }
            if(node.inputs.size!=1)return null
            id=node.inputs.single()
        }
    }
    fun tryRender(pool:SurfacePool,sources:SourceProvider,doc:Document,width:Int,height:Int,check:()->Unit={}):Surface? {
        if(width<=0||height<=0||width.toLong()*height>4_194_304L)return null
        val effects=chain(doc)?:return null
        fun bloomRadius(e:Effect)=max(1,(if(e.algorithmVersion==2)e.p("blurRadius")*max(width,height)-.5f else e.p("radius")*max(width,height)*.58f).roundToInt())
        if(effects.any{it.id=="bloom"&&bloomRadius(it)>128})return null
        doc.validate();check()
        var display=EGL14.EGL_NO_DISPLAY;var context=EGL14.EGL_NO_CONTEXT;var surface=EGL14.EGL_NO_SURFACE
        var program=0;var vertex=0;var vao=0;var framebuffer=0
        val textures=mutableListOf<Int>()
        try {
            display=EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            require(display!=EGL14.EGL_NO_DISPLAY&&EGL14.eglInitialize(display,IntArray(2),0,IntArray(2),0)){"EGL display unavailable"}
            val configs=arrayOfNulls<EGLConfig>(1);val count=IntArray(1)
            val attributes=intArrayOf(EGL14.EGL_RENDERABLE_TYPE,0x40,EGL14.EGL_SURFACE_TYPE,EGL14.EGL_PBUFFER_BIT,EGL14.EGL_RED_SIZE,8,EGL14.EGL_GREEN_SIZE,8,EGL14.EGL_BLUE_SIZE,8,EGL14.EGL_ALPHA_SIZE,8,EGL14.EGL_NONE)
            require(EGL14.eglChooseConfig(display,attributes,0,configs,0,1,count,0)&&count[0]>0){"GLES3 configuration unavailable"}
            context=EGL14.eglCreateContext(display,configs[0],EGL14.EGL_NO_CONTEXT,intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION,3,EGL14.EGL_NONE),0)
            require(context!=EGL14.EGL_NO_CONTEXT){"GLES3 context unavailable"}
            surface=EGL14.eglCreatePbufferSurface(display,configs[0],intArrayOf(EGL14.EGL_WIDTH,1,EGL14.EGL_HEIGHT,1,EGL14.EGL_NONE),0)
            require(surface!=EGL14.EGL_NO_SURFACE&&EGL14.eglMakeCurrent(display,surface,surface,context)){"EGL pbuffer unavailable"}
            val limit=IntArray(1);GLES30.glGetIntegerv(GLES30.GL_MAX_TEXTURE_SIZE,limit,0)
            if(max(width,height)>limit[0])return null
            GLES30.glDisable(GLES30.GL_DITHER);GLES30.glDisable(GLES30.GL_BLEND)
            GLES30.glPixelStorei(GLES30.GL_UNPACK_ALIGNMENT,1);GLES30.glPixelStorei(GLES30.GL_PACK_ALIGNMENT,1)
            program=linkProgram();GLES30.glUseProgram(program)
            val ids=IntArray(1);GLES30.glGenFramebuffers(1,ids,0);framebuffer=ids[0]
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,framebuffer)
            GLES30.glGenVertexArrays(1,ids,0);vao=ids[0];GLES30.glBindVertexArray(vao)
            GLES30.glGenBuffers(1,ids,0);vertex=ids[0];GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,vertex)
            val positions=ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply{put(floatArrayOf(-1f,-1f,1f,-1f,-1f,1f,1f,1f));position(0)}
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,32,positions,GLES30.GL_STATIC_DRAW)
            GLES30.glEnableVertexAttribArray(0);GLES30.glVertexAttribPointer(0,2,GLES30.GL_FLOAT,false,0,0)
            fun location(name:String)=GLES30.glGetUniformLocation(program,name)
            fun number(name:String,value:Float)=GLES30.glUniform1f(location(name),value)
            fun integer(name:String,value:Int)=GLES30.glUniform1i(location(name),value)
            fun texture(w:Int,h:Int,bytes:ByteBuffer?=null):Int {
                GLES30.glGenTextures(1,ids,0);val id=ids[0];textures+=id
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,id)
                GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,GLES30.GL_TEXTURE_MIN_FILTER,GLES30.GL_LINEAR)
                GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,GLES30.GL_TEXTURE_MAG_FILTER,GLES30.GL_LINEAR)
                GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,GLES30.GL_TEXTURE_WRAP_S,GLES30.GL_CLAMP_TO_EDGE)
                GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,GLES30.GL_TEXTURE_WRAP_T,GLES30.GL_CLAMP_TO_EDGE)
                GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D,0,GLES30.GL_RGBA8,w,h,0,GLES30.GL_RGBA,GLES30.GL_UNSIGNED_BYTE,bytes)
                glCheck();return id
            }
            val image=sources.image(doc.layers.single().asset,width,height)
            if(max(image.width,image.height)>limit[0])return null
            val upload=uploadPixels(image,check)
            val source=texture(image.width,image.height,upload)
            val imageRatio=image.width.toFloat()/image.height;val aspect=width.toFloat()/height
            GLES30.glUniform2f(location("uFit"),if(imageRatio>aspect)1f else imageRatio/aspect,if(imageRatio>aspect)aspect/imageRatio else 1f)
            GLES30.glUniform2f(location("uSize"),width.toFloat(),height.toFloat())
            integer("uInput",0);integer("uSecond",1)
            fun parameters(effect:Effect?){
                number("uMix",effect?.mix?:1f)
                if(effect==null)return
                for(name in listOf("brightness","exposure","contrast","shadows","highlights","white","black","temperature","tint","saturation","hue","threshold","levels","amount","strength","tolerance","hardness","intensity","balance","normalization"))number("u"+name.replaceFirstChar{it.uppercase()},effect.p(name))
                number("uCellSize",effect.p("size"))
                number("uCos",effect.cosine);number("uSin",effect.sine)
                number("uContrastMultiplier",effect.contrastMultiplier);number("uHueCosine",effect.hueCosine);number("uHueSine",effect.hueSine)
                if(effect.id=="curves")for(k in 0..3){val curve=effect.curves[k];val values=FloatArray(64);for(i in curve.indices){values[i*2]=curve[i].x;values[i*2+1]=curve[i].y};GLES30.glUniform2fv(location("uCurve$k"),32,values,0);integer("uCount$k",curve.size)}
            }
            fun pass(operation:Int,input:Int,second:Int=input,effect:Effect?=null,axis:Int=0,radius:Int=1):Int {
                check();val out=texture(width,height)
                GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER,GLES30.GL_COLOR_ATTACHMENT0,GLES30.GL_TEXTURE_2D,out,0)
                require(GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)==GLES30.GL_FRAMEBUFFER_COMPLETE){"Incomplete GPU framebuffer"}
                GLES30.glActiveTexture(GLES30.GL_TEXTURE0);GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,input)
                GLES30.glActiveTexture(GLES30.GL_TEXTURE1);GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,second)
                GLES30.glViewport(0,0,width,height);integer("uOperation",operation);integer("uAxis",axis);integer("uRadius",radius);parameters(effect)
                GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP,0,4);glCheck();return out
            }
            fun release(texture:Int){GLES30.glDeleteTextures(1,intArrayOf(texture),0);textures.remove(texture)}
            var current=if(image.width==width&&image.height==height)source else pass(0,source).also{release(source)}
            for(effect in effects){check();if(neutral(effect))continue;val previous=current;current=when(effect.id){
                "bloom"->{val original=current;val v2=effect.algorithmVersion==2;var glow=pass(if(v2)30 else 20,current,effect=effect);val radius=bloomRadius(effect);val operation=if(v2)31 else 21;repeat(3){val horizontal=pass(operation,glow,axis=0,radius=radius);release(glow);glow=pass(operation,horizontal,axis=1,radius=radius);release(horizontal)};val merged=pass(if(v2)32 else 22,original,glow,effect);release(glow);merged}
                else->pass(when(effect.id){"adjust"->1;"curves"->2;"invert"->3;"threshold"->4;"posterize"->5;"rgb_split"->6;"scanlines"->7;else->error("Unsupported GPU effect")},current,effect=effect)
            };release(previous)}
            // Intermediates remain textures; exactly one final readback, in document row order.
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER,GLES30.GL_COLOR_ATTACHMENT0,GLES30.GL_TEXTURE_2D,current,0)
            val bytes=ByteBuffer.allocateDirect(width*height*4).order(ByteOrder.LITTLE_ENDIAN);GLES30.glReadPixels(0,0,width,height,GLES30.GL_RGBA,GLES30.GL_UNSIGNED_BYTE,bytes);glCheck();bytes.position(0)
            return readPixels(pool,bytes,width,height,check)
        }catch(e:java.util.concurrent.CancellationException){throw e}
        catch(e:Exception){Log.w("sf7b.GPU","GLES path unavailable; using CPU: ${e.message}");return null}
        finally{
            if(display!=EGL14.EGL_NO_DISPLAY){
                if(context!=EGL14.EGL_NO_CONTEXT){if(textures.isNotEmpty())GLES30.glDeleteTextures(textures.size,textures.toIntArray(),0);if(program!=0)GLES30.glDeleteProgram(program);if(vertex!=0)GLES30.glDeleteBuffers(1,intArrayOf(vertex),0);if(vao!=0)GLES30.glDeleteVertexArrays(1,intArrayOf(vao),0);if(framebuffer!=0)GLES30.glDeleteFramebuffers(1,intArrayOf(framebuffer),0)}
                EGL14.eglMakeCurrent(display,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_CONTEXT)
                if(surface!=EGL14.EGL_NO_SURFACE)EGL14.eglDestroySurface(display,surface)
                if(context!=EGL14.EGL_NO_CONTEXT)EGL14.eglDestroyContext(display,context)
                EGL14.eglTerminate(display);EGL14.eglReleaseThread()
            }
        }
    }
    private fun swapRedBlue(color:Int):Int=(color and 0xff00ff00.toInt())or((color ushr 16)and 255)or((color and 255)shl 16)
    private fun uploadPixels(image:Surface,check:()->Unit):ByteBuffer {
        val bytes=ByteBuffer.allocateDirect(image.width*image.height*4).order(ByteOrder.LITTLE_ENDIAN)
        val integers=bytes.asIntBuffer()
        for(y in 0 until image.height){check();val row=image.readRow(y);for(x in row.indices)row[x]=swapRedBlue(row[x]);integers.put(row)}
        bytes.position(0);return bytes
    }
    private fun readPixels(pool:SurfacePool,bytes:ByteBuffer,width:Int,height:Int,check:()->Unit):Surface {
        val integers=bytes.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();val output=pool.create(width,height);val row=IntArray(width)
        for(y in 0 until height){check();integers.get(row);for(x in row.indices){
            val c=swapRedBlue(row[x]);val a=Pixels.a(c);val r=Pixels.r(c);val g=Pixels.g(c);val b=Pixels.b(c)
            row[x]=if(r>a||g>a||b>a)Pixels.pack(a,r,g,b)else c
        };output.writeRow(y,row)}
        output.seal();return output
    }
    private fun glCheck(){val error=GLES30.glGetError();check(error==GLES30.GL_NO_ERROR){"OpenGL error 0x${error.toString(16)}"}}
    private fun linkProgram():Int {
        fun compile(type:Int,source:String):Int{val shader=GLES30.glCreateShader(type);GLES30.glShaderSource(shader,source);GLES30.glCompileShader(shader);val ok=IntArray(1);GLES30.glGetShaderiv(shader,GLES30.GL_COMPILE_STATUS,ok,0);if(ok[0]==0){val log=GLES30.glGetShaderInfoLog(shader);GLES30.glDeleteShader(shader);error(log)};return shader}
        val vertex=compile(GLES30.GL_VERTEX_SHADER,"""#version 300 es
            layout(location=0) in vec2 aPosition;
            out vec2 vUv;
            void main(){vUv=(aPosition+1.0)*0.5;gl_Position=vec4(aPosition,0.0,1.0);}
        """)
        var fragment=0;var program=0
        try{fragment=compile(GLES30.GL_FRAGMENT_SHADER,fragmentSource);program=GLES30.glCreateProgram();GLES30.glAttachShader(program,vertex);GLES30.glAttachShader(program,fragment);GLES30.glLinkProgram(program);val ok=IntArray(1);GLES30.glGetProgramiv(program,GLES30.GL_LINK_STATUS,ok,0);if(ok[0]==0)error(GLES30.glGetProgramInfoLog(program));return program}
        catch(e:Exception){if(program!=0)GLES30.glDeleteProgram(program);throw e}
        finally{GLES30.glDeleteShader(vertex);if(fragment!=0)GLES30.glDeleteShader(fragment)}
    }
    private val fragmentSource="""#version 300 es
        precision highp float;
        precision highp int;
        in vec2 vUv;
        out vec4 outColor;
        uniform highp sampler2D uInput,uSecond;
        uniform int uOperation,uAxis,uRadius,uCount0,uCount1,uCount2,uCount3;
        uniform vec2 uSize,uFit;
        uniform float uMix,uBrightness,uExposure,uContrast,uShadows,uHighlights,uWhite,uBlack,uTemperature,uTint,uSaturation,uHue,uThreshold,uLevels,uAmount,uCellSize,uStrength,uCos,uSin,uTolerance,uHardness,uIntensity,uBalance,uNormalization;
        uniform float uContrastMultiplier,uHueCosine,uHueSine;
        uniform vec2 uCurve0[32],uCurve1[32],uCurve2[32],uCurve3[32];
        vec4 bytesOf(vec4 c){return floor(c*255.0+0.5);}
        vec3 straight(vec4 c){vec4 b=bytesOf(c);return b.a>0.0?b.rgb/b.a:vec3(0.0);}
        vec4 rgba(vec3 c,float a){float ai=floor(clamp(a,0.0,1.0)*255.0+0.5);return vec4(floor(clamp(c,0.0,1.0)*ai+0.5),ai)/255.0;}
        vec4 mixBytes(vec4 a,vec4 b,float t){vec4 aa=bytesOf(a);return floor(aa+(bytesOf(b)-aa)*clamp(t,0.0,1.0)+0.5)/255.0;}
        float luma(vec4 c){vec4 b=bytesOf(c);return (0.2126*b.r+0.7152*b.g+0.0722*b.b)/max(1.0,b.a);}
        float expose(float v){if(uExposure==0.0)return v;v=floor(clamp(v,0.0,1.0)*4095.0+0.5)/4095.0;float linear=v<=0.04045?v/12.92:pow((v+0.055)/1.055,2.4);float e=linear*exp2(uExposure);return e<=0.0031308?e*12.92:1.055*pow(e,1.0/2.4)-0.055;}
        float curve(float v,vec2 points[32],int count){if(v<=points[0].x)return points[0].y;for(int i=1;i<32;i++){if(i>=count)break;if(v<=points[i].x)return points[i-1].y+(points[i].y-points[i-1].y)*(v-points[i-1].x)/(points[i].x-points[i-1].x);}return points[count-1].y;}
        vec4 sourcePixel(ivec2 p,bool clampEdges){ivec2 sz=textureSize(uInput,0);if(clampEdges)p=clamp(p,ivec2(0),sz-1);else if(any(lessThan(p,ivec2(0)))||any(greaterThanEqual(p,sz)))return vec4(0.0);return texelFetch(uInput,p,0);}
        vec4 samplePixels(vec2 p,bool clampEdges){ivec2 i=ivec2(floor(p));vec2 t=p-vec2(i);return mixBytes(mixBytes(sourcePixel(i,clampEdges),sourcePixel(i+ivec2(1,0),clampEdges),t.x),mixBytes(sourcePixel(i+ivec2(0,1),clampEdges),sourcePixel(i+ivec2(1,1),clampEdges),t.x),t.y);}
        void main(){
            ivec2 pixel=ivec2(gl_FragCoord.xy);
            vec4 c=uOperation==0?vec4(0.0):texelFetch(uInput,pixel,0);vec4 result=c;
            if(uOperation==0){vec2 uv=((vec2(pixel)+0.5)/uSize-0.5)/uFit+0.5;outColor=samplePixels(uv*vec2(textureSize(uInput,0))-0.5,false);return;}
            if(uOperation==1){
                vec3 v=straight(c);float l=luma(c);float lift=uBrightness+uShadows*(1.0-l)*(1.0-l)*0.5+uHighlights*l*l*0.5;
                v=(vec3(expose(v.r),expose(v.g),expose(v.b))+lift-uBlack)/max(0.001,uWhite-uBlack);
                v=(v-0.5)*uContrastMultiplier+0.5+vec3(uTemperature*0.15+uTint*0.05,-uTint*0.1,-uTemperature*0.15+uTint*0.05);
                float gray=0.2126*v.r+0.7152*v.g+0.0722*v.b;v=gray+(v-gray)*(1.0+uSaturation);
                float cs=uHueCosine;float sn=uHueSine;float s=(v.r+v.g+v.b)/3.0;
                v=vec3(v.r*cs+(v.g-v.b)*sn+s*(1.0-cs),v.g*cs+(v.b-v.r)*sn+s*(1.0-cs),v.b*cs+(v.r-v.g)*sn+s*(1.0-cs));result=rgba(v,c.a);
            }else if(uOperation==2){vec3 v=straight(c);v=vec3(curve(curve(v.r,uCurve0,uCount0),uCurve1,uCount1),curve(curve(v.g,uCurve0,uCount0),uCurve2,uCount2),curve(curve(v.b,uCurve0,uCount0),uCurve3,uCount3));result=rgba(v,c.a);
            }else if(uOperation==3){result=rgba(1.0-straight(c),c.a);
            }else if(uOperation==4){result=rgba(vec3(luma(c)>=uThreshold?1.0:0.0),c.a);
            }else if(uOperation==5){result=rgba(roundEven(straight(c)*(uLevels-1.0))/(uLevels-1.0),c.a);
            }else if(uOperation==6){vec2 delta=uAmount*max(uSize.x,uSize.y)*vec2(uCos,uSin);vec4 r=samplePixels(vec2(pixel)+delta,true);vec4 b=samplePixels(vec2(pixel)-delta,true);result=vec4(r.r,c.g,b.b,max(c.a,max(r.a,b.a)));
            }else if(uOperation==7){float cell=max(0.002,uCellSize)*max(uSize.x,uSize.y);float gain=int((gl_FragCoord.y-0.5)/cell)%2==0?1.0:1.0-uAmount;result=rgba(straight(c)*gain,c.a);
            }else if(uOperation==20){float t=clamp((luma(c)-uThreshold)/max(0.0001,1.0-uThreshold),0.0,1.0);outColor=floor(bytesOf(c)*t+0.5)/255.0;return;
            }else if(uOperation==21){ivec4 sum=ivec4(0);ivec2 sz=textureSize(uInput,0);for(int i=-128;i<=128;i++){if(abs(i)>uRadius)continue;ivec2 q=pixel+(uAxis==0?ivec2(i,0):ivec2(0,i));if(all(greaterThanEqual(q,ivec2(0)))&&all(lessThan(q,sz)))sum+=ivec4(bytesOf(texelFetch(uInput,q,0)));}outColor=vec4(sum/(2*uRadius+1))/255.0;return;
            }else if(uOperation==22){vec4 glow=texelFetch(uSecond,pixel,0);float a=min(1.0,c.a+glow.a*uStrength*(1.0-c.a));result=vec4(clamp(c.rgb+glow.rgb*uStrength,vec3(0.0),vec3(a)),a);
            }else if(uOperation==30){float d=uTolerance==0.0?(luma(c)>=1.0?0.0:1e20):abs(luma(c)-1.0)/uTolerance;float t=clamp((1.0-d)/max(0.000001,1.0-uHardness),0.0,1.0);float gate=uHardness>=1.0?(d<=1.0?1.0:0.0):t*t*(3.0-2.0*t);outColor=vec4(floor(straight(c)*255.0+0.5),floor(bytesOf(c).a*gate+0.5))/255.0;return;
            }else if(uOperation==31){ivec4 sum=ivec4(0);ivec2 sz=textureSize(uInput,0);for(int i=-128;i<=128;i++){if(abs(i)>uRadius)continue;ivec2 q=clamp(pixel+(uAxis==0?ivec2(i,0):ivec2(0,i)),ivec2(0),sz-1);sum+=ivec4(bytesOf(texelFetch(uInput,q,0)));}outColor=vec4(sum/(2*uRadius+1))/255.0;return;
            }else if(uOperation==32){vec4 glow=texelFetch(uSecond,pixel,0);vec3 base=straight(c);float gain=uIntensity*glow.a;if(gain==0.0&&(uNormalization==0.0||uBalance==1.0)){outColor=c;return;}vec3 added=base+glow.rgb*gain;vec3 blended=mix(base,glow.rgb,min(gain,1.0));float maxValue=mix(1.0+uIntensity,1.0,uBalance);float norm=mix(1.0,1.0/maxValue,uNormalization);result=rgba(mix(added,blended,uBalance)*norm,c.a);}
            outColor=mixBytes(c,result,uMix);
        }
    """
}
