package id.psw.vshlauncher.types.sequentialimages

import android.graphics.Bitmap
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import id.psw.vshlauncher.types.XMBItem
import java.io.Closeable
import kotlin.math.roundToInt

open class XMBFrameAnimation : Closeable {

    open val fps get() = 0.0f
    open val frameCount get() = 0
    open val hasRecycled get() = false
    open var currentTime = 0.0f

    open fun getFrame(deltaTime : Float = 0.0f) : Bitmap = XMBItem.TRANSPARENT_BITMAP

    open fun recycle() { }

    override fun close() {
        recycle()
    }
}