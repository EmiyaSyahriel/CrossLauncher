package id.psw.vshlauncher

import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import androidx.annotation.ColorInt
import id.psw.vshlauncher.typography.FontCollections
import java.io.File

object VshBaseDirs {
    const val VSH_RESOURCES_DIR = "dev_flash/vsh/resource"
    const val FLASH_DATA_DIR = "dev_flash/data"
    const val USER_DIR = "dev_hdd0/home"
    const val LOGS_DIR = "dev_hdd1/logs"
    const val APPS_DIR = "dev_hdd0/game"
    const val PLUGINS_DIR = "dev_hdd0/plugins"
    const val SHORTCUTS_DIR = "dev_hdd0/shortcut"
    const val CACHE_DIR = "dev_hdd1/caches"
}

object VshResName {
    const val COLDBOOT = "coldboot"
    const val GAMEBOOT = "gameboot"
    const val APP_ICON = "ICON0"
    const val APP_ANIM_ICON = "ICON1"
}
object VshResTypes {
    val IMAGES = arrayOf("jpg","png","webp","JPG","PNG","WEBP")
    val ICONS = arrayOf("png","webp","PNG","WEBP")
    val ANIMATED_ICONS = arrayOf("webp","apng","mp4","gif", "WEBP","APNG","MP4", "GIF")
    val SOUNDS = arrayOf("AAC","OGG","MP3","WAV","MID","MIDI","aac","ogg","mp3","wav","mid","midi")
    val INI = arrayOf("ini","INI")
}

val ActivityInfo.uniqueActivityName get() = "${processName}_${name.removeSimilarPrefixes(processName)}"
val ResolveInfo.uniqueActivityName get() = activityInfo.uniqueActivityName

val Vsh.allCacheDirs : Array<File> get() {
    return arrayOf(cacheDir, *externalCacheDirs)
}

fun Vsh.makeTextPaint(
    size: Float = 12.0f,
    align: Paint.Align = Paint.Align.LEFT,
    @ColorInt color : Int = Color.WHITE,
    style : Paint.Style = Paint.Style.FILL
) : TextPaint {
    return TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        textAlign = align
        textSize = size
        this.style = style
        typeface = FontCollections.masterFont
    }
}

fun String.removeSimilarPrefixes(b:String) : String{
    // Seek until it finds any differences
    var i = 0
    var isEqual = true
    while(i < kotlin.math.min(length, b.length) && isEqual){
        isEqual = get(i) == b[i]
        i++
    }
    // Seek until next dot
    var isDot = false
    while(i < length && !isDot){
        isDot = get(i) == '.'
        i++
    }
    val retval =if(i < length) substring(i) else b
    return retval
}

/** Basically Useless on Android 4.2+ when user is exclusively using the emulated internal storage,
 * since the base emulated storage path will always contains On-device User Index,
 * Hence the "/storage/emulated/{user_index}".
 * Unless user is using External SD Card or is using a device that still
 * mounts the emulated internal storage to "/mnt/media" (or something similar) instead of to
 * "/storage/emulated/{user_index}", this is useless.
 */
fun Vsh.getUserIdPath() : String {
    return "00000000"
}

val File.isOnInternalStorage : Boolean get() = absolutePath.startsWith("/storage/emulated")