package id.psw.vshlauncher

import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import java.io.File
import java.lang.StringBuilder

object VshBaseDirs {
    const val VSH_RESOURCES_DIR = "dev_flash/vsh/resource"
    const val FLASH_DATA_DIR = "dev_flash/data"
    const val USER_DIR = "dev_hdd0/home"
    const val APPS_DIR = "dev_hdd0/games"
    const val CACHE_DIR = "dev_hdd1/caches"
}

/**
 * @param base Base directory after the storage path, treat it like the root of a PS3 HDD
 * @param args Additional directories
 * @param createParentDir Create Parent Directory if is not exist
 * @param isUserSpecific Data in this directory is specific for the user when is located at an external/portable storage
 * e.g : Settings, Configurations, Preferences
 */
fun VSH.getAllPathsFor(base:String, vararg args:String, createParentDir:Boolean = false, isUserSpecific:Boolean = false, isCache:Boolean = false) : ArrayList<File> {
    val retval = ArrayList<File>()
    isCache.select(allCacheDirs, getExternalFilesDirs("")).forEach {
        val baseFile = if(isUserSpecific && !it.isOnInternalStorage){
            it.combine(base, getUserIdPath(), *args)
        }else{
            it.combine(base, *args)
        }
        if(createParentDir && (baseFile.parentFile?.exists() != true)) baseFile.parentFile?.mkdirs()
        retval.add(baseFile)
    }
    return retval
}

val ActivityInfo.uniqueActivityName get() = "${processName}_${name}"
val ResolveInfo.uniqueActivityName get() = activityInfo.uniqueActivityName

val VSH.allCacheDirs : Array<File> get() {
    return arrayOf(cacheDir, *externalCacheDirs)
}

fun String.removeSimilarPrefixes(b:String) : String{
    // Seek until it finds any differences
    var i = 0
    var isEqual = false
    while(i < kotlin.math.min(length, b.length) && !isEqual){
        isEqual = get(i) == b[i]
        i++
    }
    return if(i < length) substring(i) else ""
}

fun VSH.generateTitleId(pkg:String, act:String) : String {
    val sb = StringBuilder()
    for(i in 0 until kotlin.math.max(pkg.length, act.length)){
        if(i < pkg.length) sb.append(pkg[i])
        if(i < act.length) sb.append(act[i])
    }
    return sb.toString()
}

/** Basically Useless on Android 4.2+ when user is exclusively using the emulated internal storage,
 * since the base emulated storage path will always contains On-device User Index,
 * Hence the "/storage/emulated/{user_index}".
 * Unless user is using External SD Card or is using a device that still
 * mounts the emulated internal storage to "/mnt/media" (or something similar) instead of to
 * "/storage/emulated/{user_index}", this is useless.
 */
fun VSH.getUserIdPath() : String {
    return "00000000"
}

val File.isOnInternalStorage : Boolean get() = absolutePath.startsWith("/storage/emulated")