package id.psw.vshlauncher.types.sequentialimages

import android.graphics.Bitmap
import id.psw.vshlauncher.types.XmbItem
import java.io.Closeable

open class XmbFrameAnimation : Closeable {

    open val fps get() = 0.0f
    open val frameCount get() = 0
    open val hasRecycled get() = false
    open var currentTime = 0.0f

    open fun getFrame(deltaTime : Float = 0.0f) : Bitmap = XmbItem.TRANSPARENT_BITMAP

    open fun recycle() { }

    override fun close() {
        recycle()
    }
}