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

    private var released = false

    init {
        BitmapManager.instance.load(this)
    }

    val bitmap : Bitmap get() = BitmapManager.instance.get(this)
    val isLoading : Boolean get() = BitmapManager.instance.isLoading(this)
    val isLoaded : Boolean get() = BitmapManager.instance.isLoaded(this)

    fun release() {
        if(!released){
            BitmapManager.instance.release(this)
            released = true
        }
    }

    fun finalize() = release()
}