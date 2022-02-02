package id.psw.vshlauncher.types.sequentialimages

import android.graphics.Bitmap
import com.facebook.animated.gif.GifImage
import id.psw.vshlauncher.select
import java.io.File

class XMBAnimGIF(val file: File) : XMBFrameAnimation() {
    companion object {
    }

    private var gif : GifImage = GifImage.createFromByteArray(file.readBytes())
    private var pFps : Float = fps
    private var hasDisposed = false
    override val frameCount get() = gif.frameCount
    override val hasRecycled get() = hasDisposed
    private val tmpBitmap = Bitmap.createBitmap(gif.width, gif.height, Bitmap.Config.ARGB_8888)

    init {
        pFps = (gif.frameDurations.sumOf { it } / gif.duration) / 1000.0f
    }

    override fun getFrame(deltaTime: Float): Bitmap {
        if(hasRecycled) throw IllegalAccessException("Image has been destroyed.")
        currentTime += deltaTime
        val sNowTime = (currentTime * 1000).toInt() % gif.duration
        val fDurs = gif.frameDurations
        var frameNum = 0
        for(i in 0 until fDurs.size){
            val lowTime = (i == 0).select(0, fDurs[i-1])
            if(sNowTime > lowTime && sNowTime < fDurs[i]){
                frameNum = i
            }
        }
        frameNum = frameNum.coerceIn(0, gif.frameCount - 1)
        gif.getFrame(frameNum).renderFrame(tmpBitmap.width, tmpBitmap.height, tmpBitmap)
        return tmpBitmap
    }

    override fun recycle() {
        if(!hasDisposed){
            tmpBitmap.recycle()
            gif.dispose()
        }
        hasDisposed = true
    }
}