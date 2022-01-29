package id.psw.vshlauncher.pluginservices

import android.content.ComponentName
import id.psw.vshlauncher.IXMBIconListProvider

data class IconPluginServiceHandle (
    var className : ComponentName = ComponentName("_","_"),
    var provider : IXMBIconListProvider? = null,
    var name : String = "",
    var description : String = "",
    var version : String = "",
    var disabled : Boolean = true)