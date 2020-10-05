package id.psw.vshlauncher

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.PointF


data class ControlRenderable(val id :Int, var enabled:Boolean, var pos: PointF, var name:String, var icon:Bitmap, var runnable:Runnable){
    fun setNameFromID(resId: Int, res:Resources){ name = res.getString(resId) }
}

enum class ControlStyle{
    PlayStation_ASIA, // Sony PlayStation
    PlayStation_US,   // Sony PlayStation (US)
    Xbox,             // Microsoft Xbox
    AndroidTV,        // Generic Android TV Remote
    JoyCon,           // Nintendo Switch JoyCon
    Keyboard,         // Generic Keyboard
    Touch             // Touch Instruction
}

enum class MediaPlayerState {
    Uninitialized,
    Prepared,
    Paused,
    Playing,
    Completed,
    Error
}

data class BoolPackOffset(val byteOffset:Int, val bitindex : Int)

data class VideoSubtitleText(val startTime: Int, val endTime : Int, val text : String)

data class VideoSubtitle(val fileName: String, val texts : List<VideoSubtitleText>){
    private fun getTextInTime(time:Int) : String{
        var retval = ""
        val found = texts.findLast { it.startTime < time && it.endTime > time }
        if(found != null) retval = found.text
        return retval
    }
}