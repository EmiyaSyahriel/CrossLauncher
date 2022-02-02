package id.psw.vshlauncher.types.sequentialimages

import android.graphics.Bitmap
import kotlin.math.roundToInt

class XMBAnimBitmap(fps: Float = 0.0f, vararg vFrames: Bitmap) : XMBFrameAnimation() {
    private var frames : ArrayList<Bitmap> = arrayListOf()
    private var pFps : Float = fps
    private var pHasRecycled = false
    override val frameCount get() = frames.size
    override val hasRecycled get() = pHasRecycled

    override val fps get() = pFps

    init {
        frames.addAll(vFrames)
        pFps = 0.0f
    }

    override fun getFrame(deltaTime: Float): Bitmap {

        if(pHasRecycled) throw IllegalAccessException("Sequential bitmap has been recycled.")
        currentTime += deltaTime

        // Resize Current Time
        if(currentTime >= 1000 * frames.size){ currentTime = 0.0f }

        if(pFps <= 0.9f) return frames[0]

        val frameTime = 1.0f / pFps
        val i = ((currentTime / frameTime) * frames.size).roundToInt()
        return frames[i]
    }

    override fun recycle() {
        if(pHasRecycled) return
        for(frame in frames){
            frame.recycle()
        }
        pHasRecycled = true
    }

}