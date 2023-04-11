package id.psw.vshlauncher.submodules

import android.graphics.Bitmap
import android.graphics.Color
import java.lang.ref.WeakReference

class BitmapRef (
    val id:String,
    val loader: () -> Bitmap?,
    val defColor : FallbackColor = FallbackColor.Transparent
) {
    enum class FallbackColor {
        Transparent,
        Black,
        White
    }

    init {
        BitmapManager.instance.load(this)
    }

    val bitmap : Bitmap get() = BitmapManager.instance.get(this)
    val isLoading : Boolean get() = BitmapManager.instance.isLoading(this)
    fun release() = BitmapManager.instance.release(this)

    fun finalize() = release()
}