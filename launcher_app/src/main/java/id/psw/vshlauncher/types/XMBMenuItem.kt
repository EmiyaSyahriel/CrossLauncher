package id.psw.vshlauncher.types

open class XMBMenuItem {
    companion object{
        val EmptyLaunchImpl : (XMBMenuItem, XMBItem) -> Unit = { _,_ -> }
    }

    lateinit var item : XMBItem
    open val onLaunch : (XMBMenuItem, XMBItem) -> Unit = EmptyLaunchImpl
    open val isSeparator : Boolean get() = true
    open val isDisabled : Boolean get() = true

    fun launch() = onLaunch(this, item)
}