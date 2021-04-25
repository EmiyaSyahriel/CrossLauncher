package id.psw.vshlauncher.icontypes

import android.content.Context
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.views.VshView

class XMBMenuEntry (val id: String) {
    enum class MenuFlag {
        Default,
        Invisible,
        Unselectable
    }

    var onClick : Runnable = Runnable {  }

    var selectable : Boolean = true
    var name : String = ""
    fun onLaunch() = onClick.run()

}