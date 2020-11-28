package id.psw.vshlauncher

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.text.Editable
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.widget.EditText
import android.widget.Toast
import id.psw.vshlauncher.customtypes.HexInputFilter
import id.psw.vshlauncher.views.VshView

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

/** TODO: Extend to use XMB-like dialog instead */
fun VSH.showBackgroundColorDialog(){
    val textView = EditText(this)
    val currentColor = VshView.menuBackgroundColor.toString(16)
    textView.inputType= InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    textView.filters = arrayOf(HexInputFilter())
    textView.text.clear()
    textView.text.append("#$currentColor")
    AlertDialog.Builder(this)
        .setView(textView)
        .setPositiveButton("Set"){ dialog,_ ->
            try{
                VshView.menuBackgroundColor = textView.text.toString().hexColorToInt()
                prefs.edit().putInt(VSH.PREF_BACKGROUND_COLOR, VshView.menuBackgroundColor).apply()
                dialog.dismiss()
            }catch(e:Exception){
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
        }
        .setNegativeButton("Cancel"){ dialog,_ ->
            dialog.dismiss()
        }
        .setTitle("Set Menu Background Color")
        .create()
        .show()
}