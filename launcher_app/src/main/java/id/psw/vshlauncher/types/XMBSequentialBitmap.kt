package id.psw.vshlauncher.types

import android.graphics.Bitmap
import kotlin.math.roundToInt

class XMBSequentialBitmap(var fps:Float = 0.0f, vararg vframes : Bitmap) {
    var CurrentTime : Float = 0.0f
    lateinit var Frames : Array<Bitmap>
    var hasRecycled = false

    init{
        Frames = Array<Bitmap>(vframes.size) { i -> vframes[i] }
    }

    fun getFrame(t : Float = 0.0f) : Bitmap{

        if(hasRecycled) throw IllegalAccessException("Sequential bitmap has been recycled...")
        CurrentTime += t

        // Resize Current Time
        if(CurrentTime >= 1000 * Frames.size){ CurrentTime = 0.0f }

        if(fps <= 0.9f) return Frames[0]

        val frameTime = 1.0f / fps
        val i = ((CurrentTime / frameTime) * Frames.size).roundToInt()

        return Frames[i]
    }

    fun recycle(){
        if(hasRecycled) return
        for(frame in Frames){
            frame.recycle()
        }
        hasRecycled = true
    }

}