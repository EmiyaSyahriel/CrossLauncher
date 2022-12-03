package id.psw.vshlauncher.livewallpaper

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES20
import android.opengl.GLException
import android.util.Log
import java.io.InputStream
import java.nio.IntBuffer
import java.nio.charset.Charset

class XMBWaveRenderer() : GLSurfaceView.Renderer {

    companion object{
        const val TAG = "wave.qrc"
        const val WAVE_TYPE_PS3 : Byte = 0b0000
        const val WAVE_TYPE_PSP : Byte = 0b0100
        const val WAVE_TYPE_PS3_NORMAL : Byte = 0b0000
        const val WAVE_TYPE_PS3_BLINKS : Byte = 0b0010
        const val WAVE_TYPE_PSP_CENTER : Byte = 0b0100
        const val WAVE_TYPE_PSP_BOTTOM : Byte = 0b0110
    }

    private var lastTime = 0L
    var doReadPixel = false
    var glBitmap : Bitmap? = null
    private var width = 1
    private var height = 1
    var surfaceView : XMBWaveSurfaceView? = null

    override fun onSurfaceCreated(_gl: GL10?, config: EGLConfig?) {
        NativeGL.create()
    }

    override fun onSurfaceChanged(_gl: GL10?, width: Int, height: Int) {
        NativeGL.setup(width, height)
        this.width = width
        this.height = height
    }

    private var _hasStringChecked = false

    override fun onDrawFrame(_gl: GL10?) {
        if(lastTime == 0L) lastTime = System.currentTimeMillis()
        surfaceView?.checkPreferenceReRead()

        val cTime = System.currentTimeMillis()
        val dTime = (cTime - lastTime) / 1000f;
        lastTime = cTime

        NativeGL.draw(dTime)

        if(doReadPixel){
            // TODO : Move allocation outside of draw routine
            val bmpBuf = IntArray(width * height)
            val bmpSrc = IntArray(width * height)
            val intBuf = IntBuffer.wrap(bmpBuf).apply { position(0) }
            try{
                GLES20.glReadPixels(0,0,width,height,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE, intBuf)
                /**
                for(y in 0 until height){
                    val o1 = y * width
                    val o2 = (height - y - 1) * width
                    for(x in 0 until width){
                        val glPx = bmpBuf[o1 + x]

                    }
                }*/
                bmpBuf.copyInto(bmpSrc)
            }catch(gle:GLException){ }
            glBitmap?.recycle()
            glBitmap = Bitmap.createBitmap(bmpSrc, width, height, Bitmap.Config.ARGB_8888)
        }
    }

    fun destroy() {
        // Do not kill
        NativeGL.destroy()
    }

    var isPaused : Boolean
        get() = NativeGL.getPaused()
        set(v) = NativeGL.setPaused(v)

}