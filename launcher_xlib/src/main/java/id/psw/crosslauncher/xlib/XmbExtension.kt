package id.psw.crosslauncher.xlib

import android.content.Context

/**
 * Base Extension Class, not to be directly extended from.
 *
 * @param hostContext - CrossLauncher application context
 * @param selfContext - Extension's own application context
 */
abstract class XmbExtension internal constructor(private var _pluginType: ExtensionType, private val hostContext : Context, private val selfContext : Context) {
    companion object {
        const val API_VERSION = "0.1"
        const val CATEGORY_PLUGIN = "id.psw.crosslauncher.xlib.CATEGORY_PLUGIN"
    }

    abstract val pluginId : String
    var pluginType : ExtensionType
        get() = _pluginType
        internal set(v) { _pluginType = v }

    open fun onStart(){}
    open fun onUpdate(){}
    open fun onDestroy(){}
    open fun onMemoryLow(){}
}