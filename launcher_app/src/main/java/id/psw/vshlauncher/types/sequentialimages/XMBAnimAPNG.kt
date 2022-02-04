package id.psw.vshlauncher.types.sequentialimages
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import androidx.annotation.IntRange
import com.github.penfeizhou.animation.apng.APNGDrawable
//import com.linecorp.apng.ApngDrawable
import java.io.File
import java.lang.Exception

// APNG Library for unknown reason shows range error (from @IntRange attribute)
// it gives error indicator to IDE but is still compilable, therefore we suppress the range error.
@SuppressLint("Range")
class XMBAnimAPNG(file: File) : XMBFrameAnimation() {
    private val apng = APNGDrawable.fromFile(file.absolutePath).apply {
        setAutoPlay(false)
        setLoopLimit(0)
        registerAnimationCallback(XMBAnimCallback())
        start()
    }
    override val frameCount: Int get() = 0
    override val fps: Float get() = 0.0f
    private var pHasRecycled = false
    override val hasRecycled: Boolean get() = pHasRecycled
    private val compBitmap = Bitmap.createBitmap(apng.intrinsicWidth,apng.minimumHeight,Bitmap.Config.ARGB_8888)
    private val composer = Canvas(compBitmap)

    override var currentTime: Float = 0.0f

    override fun getFrame(deltaTime: Float): Bitmap {
        if(hasRecycled) throw IllegalAccessException("Image has been destroyed.")
        //currentTime += deltaTime
        //if((currentTime * 1000) >= apng.durationMillis) currentTime = 0.0f
        //apng.loopLimit = 0
        ////apng.seekTo((currentTime * 1000).toLong())
        apng.draw(composer)
        return compBitmap
    }

    override fun recycle() {
        try{
            pHasRecycled = true
            compBitmap.recycle()
        }catch (e:Exception){}
    }
}