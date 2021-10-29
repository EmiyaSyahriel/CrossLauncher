package id.psw.vshlauncher.livewallpaper

import android.content.Context
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES20
import android.util.Log
import java.io.InputStream
import java.nio.charset.Charset

class XMBWaveRenderer() : GLSurfaceView.Renderer {

    companion object{
        const val TAG = "wave.qrc"
    }

    private var lastTime = 0L

    override fun onSurfaceCreated(_gl: GL10?, config: EGLConfig?) {
        NativeGL.create()
    }

    override fun onSurfaceChanged(_gl: GL10?, width: Int, height: Int) {
        NativeGL.setup(width, height)
    }

    override fun onDrawFrame(_gl: GL10?) {
        if(lastTime == 0L) lastTime = System.currentTimeMillis();

        val cTime = System.currentTimeMillis()
        val dTime = (cTime - lastTime) / 1000f;
        lastTime = cTime


        NativeGL.draw(dTime)
    }

    fun destroy() {
        // Do not kill
    }

    var isPaused : Boolean
        get() = NativeGL.getPaused()
        set(v) = NativeGL.setPaused(v)

}