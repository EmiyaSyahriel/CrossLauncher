package id.psw.crosslauncher.xlib

import android.content.Context

open class XMBPlugin internal constructor(private var _pluginType: PluginType) {
    companion object {
        const val API_VERSION = "0.1"
        const val CATEGORY_PLUGIN = "id.psw.crosslauncher.xlib.CATEGORY_PLUGIN"
    }

    open val pluginId : String = ""
    var pluginType : PluginType
        get() = _pluginType
        internal set(v) { _pluginType = v }

    open fun onStart(){ }
    open fun onUpdate() { }
    open fun onDestroy() { }
    open fun onMemoryLow() { }
}