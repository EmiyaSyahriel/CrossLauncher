package id.psw.vshlauncher.types.sequentialimages

import android.graphics.drawable.Drawable
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.github.penfeizhou.animation.FrameAnimationDrawable
import com.github.penfeizhou.animation.apng.APNGDrawable
import com.github.penfeizhou.animation.decode.FrameSeqDecoder
import com.github.penfeizhou.animation.gif.GifDrawable
import com.github.penfeizhou.animation.io.FilterReader
import com.github.penfeizhou.animation.io.ByteBufferWriter
import com.github.penfeizhou.animation.webp.WebPDrawable

class XMBAnimCallback : Animatable2Compat.AnimationCallback() {
    override fun onAnimationEnd(drawable: Drawable?) {
        when (drawable) {
            is APNGDrawable -> drawable.reset()
            is GifDrawable -> drawable.reset()
            is WebPDrawable -> drawable.reset()
            else -> super.onAnimationEnd(drawable)
        }
    }

    override fun onAnimationStart(drawable: Drawable?) {
        super.onAnimationStart(drawable)
    }
}