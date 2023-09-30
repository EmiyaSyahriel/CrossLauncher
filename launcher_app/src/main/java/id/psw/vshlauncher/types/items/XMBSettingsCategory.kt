package id.psw.vshlauncher.types.items

import android.graphics.Bitmap
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.types.XMBItem

class XMBSettingsCategory(
    private val vsh: Vsh,
    override val id: String,
    private val iconResId : Int,
    private val nameResId : Int,
    private val descResId : Int
) :
    XMBItem(vsh) {
    override val icon : Bitmap = ResourcesCompat.getDrawable(vsh.resources, iconResId, null)!!.toBitmap(100,100)
    override val displayName: String get() = vsh.getString(nameResId)
    override val description: String get() = vsh.getString(descResId)
    override val content: ArrayList<XMBItem> = arrayListOf()

    override val hasIcon: Boolean = true
    override val hasContent: Boolean = true
    override val hasDescription: Boolean = description.isNotBlank()
    override val hasMenu: Boolean = false
    override val isIconLoaded: Boolean = true
    override val hasAnimatedIcon: Boolean = false
    override val hasBackSound: Boolean = false
    override val hasBackOverlay: Boolean = false
    override val hasValue: Boolean = false
    override val hasBackdrop: Boolean = false
    override val hasPortraitBackdrop: Boolean = false
    override val hasPortraitBackdropOverlay: Boolean = false
    var isSettingHidden : () -> Boolean = { false }
    override val isHidden: Boolean get() = isSettingHidden()

}