package id.psw.vshlauncher.types.sequentialimages

import android.graphics.Bitmap
import android.graphics.Canvas
import com.github.penfeizhou.animation.gif.GifDrawable
import java.io.File

class XmbAnimGif(val file: File) : XmbFrameAnimation() {
    companion object {
    }

    private var gif : GifDrawable = GifDrawable.fromFile(file.absolutePath).apply {
        setAutoPlay(false)
        setLoopLimit(0)
        registerAnimationCallback(XMBAnimCallback())
        start()
    }
    private var pFps : Float = 0.0f
    private var hasDisposed = false
    override val frameCount get() = 0
    override val hasRecycled get() = hasDisposed
    private val cmpBitmap = Bitmap.createBitmap(gif.intrinsicWidth, gif.intrinsicHeight, Bitmap.Config.ARGB_8888)
    private val composer = Canvas(cmpBitmap)

    override fun getFrame(deltaTime: Float): Bitmap {
        if(hasRecycled) throw IllegalAccessException("Image has been destroyed.")
        //currentTime += deltaTime
        //val sNowTime = (currentTime * 1000).toInt() % gif.duration
        //val fDurs = gif.frameDurations
        //var frameNum = 0
        //for(i in fDurs.indices){
        //    val lowTime = (i == 0).select(0, fDurs[i-1])
        //    if(sNowTime > lowTime && sNowTime < fDurs[i]){
        //        frameNum = i
        //    }
        //}
        //frameNum = frameNum.coerceIn(0, gif.frameCount - 1)
        //gif.getFrame(frameNum).renderFrame(tmpBitmap.width, tmpBitmap.height, tmpBitmap)
        gif.draw(composer)
        return cmpBitmap
    }
    override fun recycle() {
        if(!hasDisposed){
            cmpBitmap.recycle()
        }
        hasDisposed = true
    }
}