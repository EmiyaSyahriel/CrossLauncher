package id.psw.vshlauncher.livewallpaper

import android.content.Context
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES20
import android.util.Log
import id.psw.vshlauncher.livewallpaper.ogl.GLShader
import id.psw.vshlauncher.livewallpaper.ogl.GLShaders
import java.io.InputStream
import java.nio.charset.Charset

class XMBWaveRenderer(val context: Context) : GLSurfaceView.Renderer {

    companion object{
        const val TAG = "wave.qrc"
    }

    lateinit var bg: GLBackgroundPlane
    lateinit var wave: GLWavePlane

    override fun onSurfaceCreated(_gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLShaders.blankShader = GLShader(context, "blank")
        GLShaders.backgroundShader = GLShader(context, "xmb_background")
        GLShaders.waveShader = GLShader(context, "xmb_wave")

        bg = GLBackgroundPlane()
        wave = GLWavePlane()
        bg.genBuffer()
        wave.genBuffer()
    }
    override fun onSurfaceChanged(_gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(_gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glDisable(GLES20.GL_CULL_FACE)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glLineWidth(10f)
        bg.render()
        wave.render()
        GLES20.glDisable(GLES20.GL_BLEND)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
    }

}