package id.psw.vshlauncher.types

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.Consts

open class XMBItem {
    companion object {
        val WHITE_BITMAP : Bitmap = ColorDrawable(Color.WHITE).toBitmap(1,1)
        val TRANSPARENT_BITMAP : Bitmap = ColorDrawable(Color.TRANSPARENT).toBitmap(1,1)
        val EmptyLaunchImpl : (XMBItem) -> Unit = { }
    }

    // Metadata
    open val HasDescription : Boolean get() = false
    open val HasIcon : Boolean get() = false
    open val HasMenu : Boolean get() = (MenuItems?.size ?: 0) > 0
    open val HasContent : Boolean get() = (Content?.size ?: 0) > 0

    open val ID: String get() = Consts.XMB_DEFAULT_ITEM_ID

    // Displayed Item
    open val Description : String get() = Consts.XMB_DEFAULT_ITEM_DESCRIPTION
    open val DisplayName : String get() = Consts.XMB_DEFAULT_ITEM_DISPLAY_NAME
    open val Icon : Bitmap get() = XMBItem.TRANSPARENT_BITMAP

    open val MenuItems : ArrayList<XMBMenuItem>? = null
    open val Content : ArrayList<XMBItem>? = null

    open val OnLaunch : (XMBItem) -> Unit = EmptyLaunchImpl

    open val MenuItemCount get() = MenuItems?.size ?: 0
    open val ContentCount get() = Content?.size ?: 0

    fun Launch(){
        OnLaunch(this)
    }
}