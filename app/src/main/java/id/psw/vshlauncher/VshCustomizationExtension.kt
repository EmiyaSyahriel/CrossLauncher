package id.psw.vshlauncher

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.drawable.toBitmap
import java.io.File
import java.nio.file.Paths


@SuppressLint("UseCompatLoadingForDrawables")
fun VSH.loadBitmap(bitmapIcon: Int): Bitmap {
    var retval = VSH.transparentIcon
    try{
        val drw = resources.getDrawable(bitmapIcon)
        retval = drw.toBitmap(drw.intrinsicWidth, drw.intrinsicHeight, Bitmap.Config.ARGB_8888)
    }finally {  }
    return retval
}

fun VSH.getVshCustomRsrcDir() : File? {
    var retval : File? = null
    try{
        val filesDir = getExternalFilesDir(null)
        if(filesDir != null){
            val resDir = File(filesDir, pathCombine("vsh","resource"))
            if(!resDir.exists()) resDir.mkdirs()
            return resDir
        }
    }finally { }
    return null
}

/**
 * Try load "app_dir/files/vsh/resource/[category]/[iconName]" as file
 * Which matched PS3 Content Information File for audio file
 * when an icon is hovered
 */
fun VSH.loadCustomIcon(category:String, iconName:String, defaultIcon: Int) : Bitmap{
    var retval = loadBitmap(defaultIcon)
    try{
        val resDir = getVshCustomRsrcDir()
        if(resDir != null){
            val catDir = File(resDir, category)
            if(!catDir.exists()) catDir.mkdirs()
            val finalFile = File(catDir, iconName)
            if(finalFile.exists()){
                if(retval != VSH.transparentIcon) retval.recycle()
                retval = BitmapFactory.decodeFile(finalFile.canonicalPath)
            }
        }
    }finally {

    }
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
    return getAppCustomFiles(packageName, "ICON1.APNG")
}

/**
 * Try load "app_dir/files/game/[packageName]/SND0.AAC" as file
 * Which matched PS3 Content Information File for audio file
 * when an icon is hovered
 */
fun VSH.getAppCustomBackSoundFile(packageName:String) : File? {
    return getAppCustomFiles(packageName, "SND0.AAC")
}
