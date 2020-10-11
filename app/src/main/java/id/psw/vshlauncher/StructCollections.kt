package id.psw.vshlauncher

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.graphics.drawable.toBitmap
import java.io.File


data class ControlRenderable(val id :Int, var enabled:Boolean, var nameStrId:Int, var runnable:Runnable)

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

/**
 * Used to determine whether this icon have a custom background / icon / BGM when hovered
 *
 * @param ICON0 Main custom icon, named ICON0.PNG when loaded from directory (recommended resolution: 320x176)
 * @param ICON1 Animated icon, named ICON1.GIF when loaded from directory (recommended resolution: 320x176)
 * @param SND0 Background music when the icon is hovered, named SND0.AAC when loaded from directory
 * @param PIC0 top-layer background image when the icon is hovered, named PIC0.PNG when loaded from directory (recommended resolution: 1000x560)
 * @param PIC1 bottom-layer Background image when the icon is hovered, named PIC1.PNG when loaded from directory (recommended resolution: 1920x1080)
 */
data class ContentInfo(var ICON0:Bitmap? = null, var ICON1:File? = null, var SND0:File? = null, var PIC0:Bitmap? = null, var PIC1:Bitmap? = null){
    companion object{
        val transparent = ColorDrawable(Color.TRANSPARENT).toBitmap(1,1, Bitmap.Config.ARGB_8888)
        const val ICON0FILENAME = "ICON0.PNG"
        const val ICON1FILENAME = "ICON1.PNG"
        const val SND0FILENAME = "SND0.AAC"
        const val PIC0FILENAME = "ICON0.PNG"
        const val PIC1FILENAME = "ICON0.PNG"
    }

    constructor(directory: File?):this(){
        directory.whenNotNull { contentDir ->
            val files = contentDir.listFiles().whenNotNull { files ->
                files.find { it.name == ICON0FILENAME }.whenNotNull { ICON0 = BitmapFactory.decodeFile(it.absolutePath) }
                files.find { it.name == ICON1FILENAME }.whenNotNull { ICON1 = it }
                files.find { it.name == SND0FILENAME }.whenNotNull { SND0 = it }
                files.find { it.name == PIC0FILENAME }.whenNotNull { PIC0 = BitmapFactory.decodeFile(it.absolutePath) }
                files.find { it.name == PIC1FILENAME }.whenNotNull { PIC1 = BitmapFactory.decodeFile(it.absolutePath) }
            }
        }
    }
}