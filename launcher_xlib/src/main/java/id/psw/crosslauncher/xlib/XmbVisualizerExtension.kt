package id.psw.crosslauncher.xlib

import android.content.Context
import android.graphics.Canvas
import android.graphics.Point

abstract class XmbVisualizerExtension(host: Context, self:Context) : XmbExtension(ExtensionType.Visualizer, host, self){
    enum class Backend {
        Canvas,
        OpenGL,
        Vulkan
    }

    /** What backend is the visualizer running on*/
    abstract val backend : Backend
    open fun renderCanvas(audioData : FloatArray, canvas: Canvas){}
    open fun renderGL(audioData : FloatArray, size: Point){}
    open fun renderVulkan(audioData: FloatArray, size:Point){}
}