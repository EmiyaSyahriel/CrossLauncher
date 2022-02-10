package id.psw.vshlauncher.types.items

import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.types.XMBItem

class XMBSettingsItem(
    private val vsh: VSH,
    override val id : String,
    private val r_title : Int,
    private val r_desc : Int,
    private val r_icon : Int,
    private val value_get : () -> String,
    private val on_launch : () -> Unit
) : XMBItem(vsh) {
    override val displayName: String get() = vsh.getString(r_title)
    override val description: String get() = vsh.getString(r_desc)
    override val value: String get() = value_get()
    override val hasValue: Boolean = true
    override val hasDescription: Boolean get() = description.isNotBlank()
    override val hasMenu: Boolean = false
    override val hasIcon: Boolean = true
    override val isIconLoaded: Boolean = true
    override val icon = ResourcesCompat.getDrawable(vsh.resources, r_icon, null)?.toBitmap(256,256) ?: TRANSPARENT_BITMAP

    private fun callLaunch(x:XMBItem){
        on_launch()
    }

    override val onLaunch: (XMBItem) -> Unit
        get() = ::callLaunch
}