package id.psw.vshlauncher.types.items

import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.types.XMBItem

class XMBShortcutItem(vsh: VSH, c:String) : XMBItem(vsh) {

    override val onLaunch: (XMBItem) -> Unit
        get() = super.onLaunch
}