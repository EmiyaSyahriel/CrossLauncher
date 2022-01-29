package id.psw.vshlauncher

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import com.linecorp.apng.ApngDrawable
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.views.VshView
import java.io.File
import java.lang.Exception

object VshDirs {
    /**
     * 0 : Device files dir
     */
    const val SYSTEM_DIR = "{0}/dev_flash/vsh/resource/"
    /**
     * 0 : Device files dir
     *
     * 1 : Package name
     */
    const val USER_DIR = "{0}/dev_hdd0/home/{1}/"
    /**
     * 0 : Device files dir
     *
     * 1 : App package name
     */
    const val APPS_DIR = "{0}/dev_hdd0/games/{1}/"
    /**
     * 0 : Device cache directory
     *
     * 1 : Whatever name it is
     */
    const val CACHE_DIR = "{0}/dev_hdd1/caches/{1}/"

}

fun VSH.formatDirPathAndCreate(source: String, vararg args : String) : File {
    var retval = source
    for(i in args.indices){
        retval = retval.replace("{$i}", args[i], true)
    }
    val fileRetval = File(retval)
    if(!fileRetval.exists()){
        fileRetval.mkdirs()
    }
    return fileRetval
}

fun VSH.getFilesPath() : String = getExternalFilesDir(null)?.absolutePath ?: "/storage/emulated/0/Android/$packageName/files/"
fun VSH.getCachePath() : String = externalCacheDir?.absolutePath ?: "/storage/emulated/0/Android/$packageName/cache/"


fun VSH.formatFilePathAndCreate(source: String, fileName: String, vararg args : String) : File{
    var retval = source
    for(i in args.indices){
        retval = retval.replace("{$i}", args[i], true)
    }
    val fileRetval = File(retval)
    if(!fileRetval.exists()){
        fileRetval.mkdirs()
    }
    return File(fileRetval, fileName)
}

@SuppressLint("UseCompatLoadingForDrawables")
fun VSH.loadBitmap(bitmapIcon: Int): Bitmap {
    var retval = XMBItem.TRANSPARENT_BITMAP
    try{
        val drw = resources.getDrawable(bitmapIcon)
        retval = drw.toBitmap(drw.intrinsicWidth, drw.intrinsicHeight, Bitmap.Config.ARGB_8888)
    }catch (e:Exception) { e.printStackTrace() }
    return retval
}

/**
 * Try load "app_dir/files/vsh/resource/[category]/[iconName]" as file
 * when an icon is hovered
 */
fun VSH.loadLauncherCustomIcon(iconName:String, defaultIcon: Int) : Bitmap{
    val TAG = "custman.sprx"
    var retval = loadBitmap(defaultIcon)
    try{
        val iconFile = formatFilePathAndCreate(VshDirs.SYSTEM_DIR, iconName, getFilesPath())
        if(iconFile.exists()){
            val bitmapTemp = BitmapFactory.decodeFile(iconFile.absolutePath)
            if(bitmapTemp != null) retval = bitmapTemp
        }
    }catch (e:Exception) { e.printStackTrace() }
    return retval
}

fun VSH.getAppCustomFiles(packageName: String, fileName:String) : File = formatFilePathAndCreate(VshDirs.APPS_DIR, fileName, getFilesPath(), packageName)

/**
 * Try load "app_dir/files/game/[packageName]/ICON1.APNG" as file
 * where it was the animated version of the app icon
 */
fun VSH.getAppCustomAnimIconFile(packageName: String) : ApngDrawable?{
    var retval : ApngDrawable? = null
    try{
        val file = formatFilePathAndCreate(VshDirs.SYSTEM_DIR, "ICON1.APNG", getFilesPath(), packageName)
        if(file.exists()) retval = ApngDrawable.Companion.decode(file)
    }catch (e:Exception){}
    return retval
}
/**
 * Try load "app_dir/files/game/[packageName]/ICON0.PNG" as file
 * where it was the animated version of the app icon
 */
fun VSH.getAppCustomIconFile(packageName: String) : Bitmap?{
    var retval : Bitmap? = null
    try{
        val file = formatFilePathAndCreate(VshDirs.SYSTEM_DIR, "ICON0.PNG", getFilesPath(), packageName)
        if(file.exists()) retval = BitmapFactory.decodeFile(file.absolutePath)
    }catch (e:Exception){}
    return retval
}

/**
 * Try load "app_dir/files/game/[packageName]/PIC0.PNG" as file
 * where it was the animated version of the app icon
 */
fun VSH.getAppCustomBackdrop(packageName: String) : Bitmap?{
    var retval : Bitmap? = null
    try{
        val file = formatFilePathAndCreate(VshDirs.SYSTEM_DIR, "PIC0.PNG", getFilesPath(), packageName)
        if(file.exists()) retval = BitmapFactory.decodeFile(file.absolutePath)
    }catch (e:Exception){}
    return retval
}

/**
 * Try load "app_dir/files/game/[packageName]/SND0.AAC" as file
 * Which matched PS3 Content Information File for audio file
 * when an icon is hovered
 */
fun VSH.getAppCustomBackSoundFile(packageName:String) : File? {
    return formatFilePathAndCreate(VshDirs.SYSTEM_DIR, "SND0.AAC", getFilesPath(), packageName)
}