package id.psw.vshlauncher.filesystem

import android.content.Context
import android.util.Log
import java.io.File

fun openOrCreateFile(ctx: Context, path:String) : File {
    val retval = File(ctx.getExternalFilesDir(null), path)
    var isNewCreate = false
    if(!retval.exists()) isNewCreate = retval.mkdirs()
    if(isNewCreate) Log.d("iosys.sprx", "Created new folder at $path")
    return retval
}

fun Context.fileOpenOrCreate(path : String) : File{
    val retval = File(getExternalFilesDir(null), path)
    var isNewCreate = false
    if(!retval.exists()) isNewCreate = retval.mkdirs()
    if(isNewCreate) Log.d("iosys.sprx", "Created new folder at $path")
    return retval
}