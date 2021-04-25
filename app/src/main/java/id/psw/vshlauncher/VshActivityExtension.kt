package id.psw.vshlauncher

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.text.Editable
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.widget.EditText
import android.widget.Toast
import id.psw.vshlauncher.customtypes.HexInputFilter
import id.psw.vshlauncher.icontypes.XMBIcon
import id.psw.vshlauncher.icontypes.createMenu
import id.psw.vshlauncher.mediaplayer.XMBAudioPlayerService
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

fun VSH.setHiddenClock(boolean: Boolean){ VshView.hideClock = boolean; prefs.edit().putBoolean(VSH.PREF_HIDE_CLOCK, boolean).apply() }
fun VSH.setHiddenClock(){ setHiddenClock(!VshView.hideClock) }

fun VSH.setSeparatorLine(boolean: Boolean){ VshView.descriptionSeparator = boolean; prefs.edit().putBoolean(VSH.PREF_SHOW_SEPARATOR, boolean).apply() }
fun VSH.setSeparatorLine(){ setSeparatorLine( !VshView.descriptionSeparator) }

fun VSH.setBackgroundColor(a:Int,r:Int,g:Int,b:Int){
    val fColor = Color.argb(a,r,g,b)
    VshView.menuBackgroundColor = fColor
    prefs.edit().putInt(VSH.PREF_BACKGROUND_COLOR, VshView.menuBackgroundColor).apply()
}

fun VSH.preMadeColors(icon:XMBIcon) : XMBIcon.MenuEntryBuilder {

    return icon.createMenu()
        .add("Clear"){          setBackgroundColor(   0, 255, 255, 255)}
        .add("50% Red"){        setBackgroundColor( 128, 255,   0,   0)}
        .add("50% Green"){      setBackgroundColor( 128,   0, 255,   0)}
        .add("50% Blue"){       setBackgroundColor( 128,   0,   0, 255)}
        .add("50% Black"){      setBackgroundColor( 128,   0,   0,   0)}
        .add("50% White"){      setBackgroundColor( 128, 255, 255, 255)}
        .add("Opaque Red"){     setBackgroundColor( 255, 255,   0,   0)}
        .add("Opaque Green"){   setBackgroundColor( 255,   0, 255,   0)}
        .add("Opaque Blue"){    setBackgroundColor( 255,   0,   0, 255)}
        .add("Opaque Black"){   setBackgroundColor( 255,   0,   0,   0)}
        .add("Opaque White"){   setBackgroundColor( 255, 255, 255, 255)}
}

fun VSH.tryConnectToMusicPlayerService(){
    bindService(Intent(this, XMBAudioPlayerService::class.java), audioPlayerConnector, Context.BIND_AUTO_CREATE)
}