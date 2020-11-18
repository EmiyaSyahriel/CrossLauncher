package id.psw.vshlauncher.icontypes

import android.content.Context
import android.graphics.Bitmap
import id.psw.vshlauncher.R
import id.psw.vshlauncher.VshY

class VshSettingIcon(
    itemID: Int,
    private var context: Context,
    name: String,
    iconId: String,
    private var onClick: () -> Unit,
    valueStr: () -> String
) : VshY(itemID) {

    companion object
    {
        const val DEVICE_ORIENTATION = "device_orientation"
        const val ICON_ANDROID = "icon_android"
        const val ICON_STAR = "icon_star"
        const val ICON_REFRESH = "icon_refresh"
        const val ICON_START = "icon_start"
        var iconIds : Map<String, Int> = mapOf(
            Pair(DEVICE_ORIENTATION, R.drawable.icon_orientation),
            Pair(ICON_ANDROID, R.drawable.icon_android),
            Pair(ICON_STAR, R.drawable.icon_dynamic_theme_effect),
            Pair(ICON_REFRESH, R.drawable.icon_refresh),
            Pair(ICON_START, R.drawable.icon_start)
        )
    }

    private var valueString : () -> String = valueStr
    private var iconName : String = name
    private var iconDrawableId = iconId
    private var iconSelected : Bitmap = transparentBitmap
    private var iconUnselected : Bitmap = transparentBitmap
    override val selectedIcon: Bitmap get() = iconUnselected

    override val hasDescription: Boolean
        get() = this.valueString.invoke().isNotEmpty()

    override val name: String
        get() = this.iconName

    override val description: String
        get() = this.valueString.invoke()

    override val onLaunch: Runnable
        get() = Runnable { onClick.invoke() }

}