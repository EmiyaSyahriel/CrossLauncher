package id.psw.vshlauncher.livewallpaper

import android.content.Context
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import id.psw.vshlauncher.R
import java.io.InputStream
import java.nio.charset.Charset


class XMBWaveSurfaceView : GLSurfaceView {

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

    fun init(){
        setEGLConfigChooser(8,8,8,8,8,8)
        setEGLContextClientVersion(2)
        renderer = XMBWaveRenderer()
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        holder?.setFormat(PixelFormat.TRANSLUCENT)
        Log.d(TAG, "Wave Surface Initialized")
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        postInvalidate()
    }
}