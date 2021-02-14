package id.psw.vshlauncher.icontypes

import android.content.Context
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.views.VshView

class XMBMenuEntry (val ctx: Context, val vsh: VshView, val id: String) {
    enum class MenuFlag {
        Default,
        Invisible,
        Unselectable
    }

    var onClick : Runnable = Runnable {  }
    var onSelectionChanged : (Boolean, XMBMenuEntry) -> Unit = { _, _ -> }

    var selectable : Boolean = true
    var name : String = ""
    var description : String = ""
    fun onLaunch() = onClick.run()

    var isSelected : Boolean = false
        set(value){
            if(field != value){
                onSelectionChanged(value, this)
            }
            field = value
        }
}