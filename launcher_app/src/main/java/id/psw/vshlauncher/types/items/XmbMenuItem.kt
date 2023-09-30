package id.psw.vshlauncher.types.items

import id.psw.vshlauncher.Consts

open class XmbMenuItem {
    companion object{
        val EmptyLaunchImpl : () -> Unit = { }
    }

    open val onLaunch : () -> Unit = EmptyLaunchImpl
    open val displayName : String get() = Consts.XMB_DEFAULT_ITEM_DISPLAY_NAME
    open val isDisabled : Boolean get() = true
    open val displayOrder : Int = 0

    override fun toString(): String = "[Menu] $displayName"

    class XmbMenuItemLambda(
        val gDisplayName: () -> String,
        val gDisabled: () -> Boolean,
        override val displayOrder: Int,
        override val onLaunch: () -> Unit,
    ) : XmbMenuItem() {
        override val displayName: String get() = gDisplayName()
        override val isDisabled: Boolean get() = gDisabled()
    }
}