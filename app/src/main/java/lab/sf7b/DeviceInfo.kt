package lab.sf7b

import android.app.ActivityManager
import android.content.Context
import android.opengl.EGL14
import android.opengl.GLES20
import android.os.Build

/** Capability probe only: rendering uses the tested CPU path on every device. */
fun deviceInfo(context:Context):String {
    val memory=ActivityManager.MemoryInfo();(context.getSystemService(Context.ACTIVITY_SERVICE)as ActivityManager).getMemoryInfo(memory)
    val base="${Build.MANUFACTURER} ${Build.MODEL}\nAndroid ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\nABI: ${Build.SUPPORTED_ABIS.joinToString()}\nRAM: ${memory.totalMem/1048576} MB · available: ${memory.availMem/1048576} MB\nHeap limit: ${Runtime.getRuntime().maxMemory()/1048576} MB"
    val gl=runCatching{
        val display=EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);val versions=IntArray(2);check(EGL14.eglInitialize(display,versions,0,versions,1))
        try{val configs=arrayOfNulls<android.opengl.EGLConfig>(1);val count=IntArray(1);check(EGL14.eglChooseConfig(display,intArrayOf(EGL14.EGL_SURFACE_TYPE,EGL14.EGL_PBUFFER_BIT,EGL14.EGL_RENDERABLE_TYPE,EGL14.EGL_OPENGL_ES2_BIT,EGL14.EGL_NONE),0,configs,0,1,count,0)&&count[0]>0)
            var glContext=EGL14.eglCreateContext(display,configs[0],EGL14.EGL_NO_CONTEXT,intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION,3,EGL14.EGL_NONE),0)
            if(glContext==EGL14.EGL_NO_CONTEXT)glContext=EGL14.eglCreateContext(display,configs[0],EGL14.EGL_NO_CONTEXT,intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION,2,EGL14.EGL_NONE),0)
            val surface=EGL14.eglCreatePbufferSurface(display,configs[0],intArrayOf(EGL14.EGL_WIDTH,1,EGL14.EGL_HEIGHT,1,EGL14.EGL_NONE),0)
            try{check(EGL14.eglMakeCurrent(display,surface,surface,glContext));val size=IntArray(1);GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE,size,0);"${GLES20.glGetString(GLES20.GL_VERSION)}\n${GLES20.glGetString(GLES20.GL_RENDERER)}\nMax texture: ${size[0]}"}finally{EGL14.eglMakeCurrent(display,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_CONTEXT);EGL14.eglDestroySurface(display,surface);EGL14.eglDestroyContext(display,glContext)}
        }finally{EGL14.eglTerminate(display)}
    }.getOrDefault("OpenGL: unavailable / недоступен")
    return "$base\n$gl"
}
