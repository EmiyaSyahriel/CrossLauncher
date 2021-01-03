package id.psw.vshlauncher

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import com.linecorp.apng.ApngDrawable
import id.psw.vshlauncher.filesystem.fileOpenOrCreate
import java.io.File
import java.lang.Exception
import java.nio.file.Paths


@SuppressLint("UseCompatLoadingForDrawables")
fun VSH.loadBitmap(bitmapIcon: Int): Bitmap {
    var retval = VSH.transparentIcon
    try{
        val drw = resources.getDrawable(bitmapIcon)
        retval = drw.toBitmap(drw.intrinsicWidth, drw.intrinsicHeight, Bitmap.Config.ARGB_8888)
    }catch (e:Exception) { e.printStackTrace() }
    return retval
}

fun VSH.getVshCustomRsrcDir() : File {
    return fileOpenOrCreate("vsh/resource/")
}

/**
 * Try load "app_dir/files/vsh/resource/[category]/[iconName]" as file
 * when an icon is hovered
 */
fun VSH.loadCustomIcon(category:String, iconName:String, defaultIcon: Int) : Bitmap{
    val TAG = "custman.sprx"
    var retval = loadBitmap(defaultIcon)
    try{
        val catDir = fileOpenOrCreate("vsh/resource/icons/${category}")
        Log.d(TAG, "Exploring ${catDir.path}");
        catDir.listFiles()?.forEach {
            Log.d(TAG, "-> [${if(it.isDirectory) "Dirc" else "File"}] ${it.name}");
        }
        val finalFile = File(catDir, "$iconName.png")
        Log.d(TAG, "Trying to load custom icon from \"${finalFile.path}\"")
        retval = if(finalFile.exists()){
            if(retval != VSH.transparentIcon) retval.recycle()
            Log.d(TAG, "-> Loaded")
            BitmapFactory.decodeFile(finalFile.path)
        }else{
            Log.d(TAG, "-> Failed, loading from app resource with id $defaultIcon")
            BitmapFactory.decodeResource(resources, defaultIcon)
        }
    }catch (e:Exception) { e.printStackTrace() }
    return retval
}

fun VSH.getAppCustomFiles(packageName: String, fileName:String) : File?{
    var retval : File? = null
    try{
        val filesDir = getExternalFilesDir(null)
        if(filesDir != null){
            val packageDir= File(filesDir, pathCombine("game", packageName))
            if(packageDir.exists()){
                val finalFile = File(packageDir, fileName)
                if(finalFile.exists()){
                    retval = finalFile
                }
            }else{
                packageDir.mkdirs()
            }
        }
    }finally { }
    return retval
}

/**
 * Try load "app_dir/files/game/[packageName]/ICON1.APNG" as file
 * where it was the animated version of the app icon
 */
fun VSH.getAppCustomAnimIconFile(packageName: String) : File?{
    var retval : File? = null
    val animIconFile = getAppCustomFiles(packageName, "ICON1.APNG")
    if(animIconFile != null){
        if(ApngDrawable.isApng(animIconFile)){
            retval = animIconFile
        }
    }
    return retval
}

/**
 * Try load "app_dir/files/game/[packageName]/SND0.AAC" as file
 * Which matched PS3 Content Information File for audio file
 * when an icon is hovered
 */
fun VSH.getAppCustomBackSoundFile(packageName:String) : File? {
    return getAppCustomFiles(packageName, "SND0.AAC")
}
