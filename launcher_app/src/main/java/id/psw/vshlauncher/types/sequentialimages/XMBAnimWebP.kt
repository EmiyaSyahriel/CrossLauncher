package id.psw.vshlauncher.types.sequentialimages
import android.graphics.Bitmap
import com.facebook.animated.webp.WebPImage
import com.facebook.imagepipeline.common.ImageDecodeOptions
import id.psw.vshlauncher.select
import java.io.File
import kotlin.math.roundToInt

class XMBAnimWebP(val file:File) : XMBFrameAnimation() {
    companion object {
        private val decodeOption = ImageDecodeOptions.newBuilder().apply {

        }.build()
    }

    private var webp : WebPImage = WebPImage.createFromByteArray(file.readBytes(), decodeOption)
    private var pFps : Float = fps
    private var hasDisposed = false
    override val frameCount get() = webp.frameCount
    override val hasRecycled get() = hasDisposed
    private val tmpBitmap = Bitmap.createBitmap(webp.width, webp.height, Bitmap.Config.ARGB_8888)

    init {
        pFps = (webp.frameDurations.sumOf { it } / webp.duration) / 1000.0f
    }

    override fun getFrame(deltaTime: Float): Bitmap {
        if(hasRecycled) throw IllegalAccessException("Image has been destroyed.")
        currentTime += deltaTime
        var frameCTime = 0
        val sNowTime = (currentTime * 1000).toInt() % webp.duration
        val fDurs = webp.frameDurations
        var frameNum = 0
        for(i in 0 until webp.duration){
            val lowTime = (i == 0).select(0, fDurs[i-1])
            if(sNowTime > lowTime && sNowTime < fDurs[i]){
                frameNum = i
            }
        }
        frameNum = frameNum.coerceIn(0, webp.frameCount - 1)
        webp.getFrame(frameNum).renderFrame(tmpBitmap.width, tmpBitmap.height, tmpBitmap)
        return tmpBitmap
    }

    override fun recycle() {
        if(!hasDisposed){
            tmpBitmap.recycle()
            webp.dispose()
        }
        hasDisposed = true
    }
}