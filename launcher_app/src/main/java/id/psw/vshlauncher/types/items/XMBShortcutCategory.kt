package id.psw.vshlauncher.types.items

import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.types.XMBItem

class XMBShortcutCategory(vsh: VSH) : XMBItem(vsh) {
    private val items = arrayListOf<XMBItem>()

    override val content: ArrayList<XMBItem> get() = items
    override val hasContent: Boolean  get() = items.isNotEmpty()
    override val isHidden: Boolean get() = !hasContent
    override val hasIcon: Boolean get() = true

}