package id.psw.crosslauncher.xlib

import android.content.Context

/**
 *
 */
abstract class XmbStatusTextExtension(host: Context, self:Context) : XmbExtension(ExtensionType.StatusText, host, self) {
    /** Formatted Keys that is supported by this extension  */
    abstract val keys : Array<out String>
    /** Get the string for this key */
    abstract fun getString(key:String) : String?
}