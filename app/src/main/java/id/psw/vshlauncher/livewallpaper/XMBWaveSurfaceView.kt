package id.psw.vshlauncher.livewallpaper

import android.content.Context
import android.graphics.Canvas
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import id.psw.vshlauncher.R
import id.psw.vshlauncher.livewallpaper.ogl.GLShader
import id.psw.vshlauncher.livewallpaper.ogl.GLShaders
import id.psw.vshlauncher.livewallpaper.ogl.GLShape
import java.io.InputStream
import java.nio.charset.Charset


class XMBWaveSurfaceView : GLSurfaceView {

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
        setEGLContextClientVersion(2)

        renderer = XMBWaveRenderer(context)
        setRenderer(renderer)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        postInvalidate()
    }

}