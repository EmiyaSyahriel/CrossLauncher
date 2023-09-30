package id.psw.vshlauncher.types.sequentialimages
import android.graphics.*
// import com.facebook.animated.webp.WebPImage
// import com.facebook.imagepipeline.animated.base.AnimatedDrawableFrameInfo
// import com.facebook.imagepipeline.common.ImageDecodeOptions
import com.github.penfeizhou.animation.webp.WebPDrawable
import java.io.File

class XmbAnimWebP(val file:File) : XmbFrameAnimation() {
    private var webp : WebPDrawable = WebPDrawable.fromFile(file.absolutePath).apply {
        setAutoPlay(false)
        setLoopLimit(0)
        registerAnimationCallback(XMBAnimCallback())
        start()
    }
    private var hasDisposed = false
    override val frameCount get() = 0
    override val hasRecycled get() = hasDisposed
    // Composer bitmap
    private val cmpBitmap = Bitmap.createBitmap(webp.intrinsicWidth, webp.intrinsicHeight, Bitmap.Config.ARGB_8888)
    private val composer = Canvas(cmpBitmap)

    override fun getFrame(deltaTime: Float): Bitmap {
//        if(hasRecycled) throw IllegalAccessException("Image has been destroyed.")
//        currentTime += deltaTime
//        if(currentTime >= webp.duration * 1000) currentTime = 0.0f
//        var frameCTime = 0
//        val sNowTime = (currentTime * 1000.0f).toInt() % webp.duration
//        val fDurs = webp.frameDurations
//        var frameNum = 0
//        for(i in fDurs.indices){
//            val ccTime = fDurs[i]
//            if(sNowTime > frameCTime && sNowTime < frameCTime + ccTime){
//                frameNum = i
//                break
//            }
//            frameCTime += fDurs[i]
//        }
//
//        frameNum = frameNum.coerceIn(0, webp.frameCount - 1)
//        webp.getFrame(frameNum).renderFrame(tmpBitmap.width, tmpBitmap.height, tmpBitmap)
//        val imgInfo = webp.getFrameInfo(frameNum)
//        composePaint.xfermode = (imgInfo.blendOperation == AnimatedDrawableFrameInfo.BlendOperation.NO_BLEND).select(xferClear, xferDefault)
//        if(frameNum == 0){
//            composer.drawRect(0f, 0f, cmpBitmap.width.toFloat(), cmpBitmap.height.toFloat(), composePaint)
//        }else
//        {
//            tmpRectF.set(
//                imgInfo.xOffset * 1.0f,
//                imgInfo.yOffset * 1.0f,
//                imgInfo.width   * 1.0f,
//                imgInfo.height * 1.0f
//            )
//            composer.drawBitmap(tmpBitmap, null, tmpRectF, composePaint)
//        }
        webp.draw(composer)
        return cmpBitmap

    }

    override fun recycle() {
        if(!hasDisposed){
            cmpBitmap.recycle()
        }
        hasDisposed = true
    }
}