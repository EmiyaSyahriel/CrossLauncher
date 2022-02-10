package id.psw.vshlauncher.livewallpaper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import id.psw.vshlauncher.R
import id.psw.vshlauncher.vsh
import java.io.InputStream
import java.nio.charset.Charset


class XMBWaveSurfaceView : GLSurfaceView {

    companion object{
        const val PREF_NAME = "libwave_setting"
        const val KEY_STYLE = "wave_style"
        const val KEY_SPEED = "wave_speed"
        const val KEY_COLOR_BACK_A = "wave_cback_a"
        const val KEY_COLOR_BACK_B = "wave_cback_b"
        const val KEY_COLOR_FORE_A = "wave_cfore_a"
        const val KEY_COLOR_FORE_B = "wave_cfore_b"
    }

    private val TAG = "glsurface.sprx"

    constructor(context: Context?): super(context){
        init()
    }
    constructor(context: Context?, attrs: AttributeSet): super(context, attrs){
        init()
    }

    constructor(context:Context?, attrs: AttributeSet, styleSet: Int) : super(context, attrs){
        init()
    }

    lateinit var renderer : XMBWaveRenderer

    private fun readPreferences(){
        Log.d(TAG, "Re-reading preferences...")
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        NativeGL.setWaveStyle(prefs.getInt(KEY_STYLE, XMBWaveRenderer.WAVE_TYPE_PS3_NORMAL.toInt()).toByte())
        NativeGL.setSpeed(prefs.getFloat(KEY_SPEED, 1.0f))
        NativeGL.setBackgroundColor(
            prefs.getInt(KEY_COLOR_BACK_A, Color.argb(0xFF,0x99,0x00,0xFF)),
            prefs.getInt(KEY_COLOR_BACK_B, Color.argb(0xFF,0x00,0x99,0xFF))
        )
        NativeGL.setForegroundColor(
            prefs.getInt(KEY_COLOR_FORE_A, Color.argb(0xFF,0xFF,0xFF,0xFF)),
            prefs.getInt(KEY_COLOR_FORE_B, Color.argb(0x88,0xFF,0xFF,0xFF))
        )
    }

    fun init(){
        setEGLConfigChooser(8,8,8,0,8,8)
        setEGLContextClientVersion(2)
        renderer = XMBWaveRenderer()
        readPreferences()
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        holder?.setFormat(PixelFormat.TRANSLUCENT)
        Log.d(TAG, "Wave Surface Initialized")
    }

    override fun onDraw(canvas: Canvas?) {
        if(context.vsh.waveShouldReReadPreferences){
            readPreferences()
        }
        super.onDraw(canvas)
        postInvalidate()
    }
}