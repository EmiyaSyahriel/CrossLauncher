package id.psw.vshlauncher.types.items

import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.types.XmbItem

class XmbSettingsItem(
    private val vsh: Vsh,
    override val id : String,
    private val r_title : Int,
    private val r_desc : Int,
    private val r_icon : Int,
    private val value_get : () -> String,
    private val on_launch : () -> Unit
) : XmbItem(vsh) {
    override val displayName: String get() = vsh.getString(r_title)
    override val description: String get() = vsh.getString(r_desc)
    override val value: String get() = value_get()
    override val hasValue: Boolean = true
    override val hasDescription: Boolean get() = description.isNotBlank()
    override var hasMenu: Boolean = false
    override var menuItems: ArrayList<XmbMenuItem>? = null
    override val hasIcon: Boolean = true
    override val isIconLoaded: Boolean = true
    override val icon = ResourcesCompat.getDrawable(vsh.resources, r_icon, null)?.toBitmap(256,256) ?: TRANSPARENT_BITMAP
    override val isHidden: Boolean get() = checkIsHidden()
    var checkIsHidden : () -> Boolean = {
        false
    }

    private fun callLaunch(x:XmbItem){
        on_launch()
    }

    override val onLaunch: (XmbItem) -> Unit
        get() = ::callLaunch
}