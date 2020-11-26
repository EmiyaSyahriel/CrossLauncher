package id.psw.vshlauncher

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast

/**
 * A file that contains extensions for the main VSH Activity
 * Due to the main class is too bloated for my PC to load
 */
val NOPE = 0

val UNINSTALL_REQ_CODE : Int get() = 29734

fun VSH.uninstallApp(packageName:String){
    try{
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.fromParts("package", packageName, null)
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
        startActivityForResult(intent, UNINSTALL_REQ_CODE)
    }catch(e:Exception){
        Toast.makeText(this, "Failed to uninstall app: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun VSH.runOnOtherThread(what : () -> Unit) : Thread {
    val thr = Thread {
        what.invoke()
    }

    thr.start()
    return thr
}

fun VSH.launchURL(url:String){
    try{
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }catch(e: PackageManager.NameNotFoundException){
        Toast.makeText(this, "No default browser found in device.", Toast.LENGTH_SHORT).show()
    }
}
