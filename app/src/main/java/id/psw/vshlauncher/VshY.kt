package id.psw.vshlauncher

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.customtypes.XMBStack
import id.psw.vshlauncher.icontypes.XMBIcon
import id.psw.vshlauncher.views.VshView

open class VshY(context: VSH, vsh:VshView, itemID: String) : XMBIcon(context, vsh, itemID) {


    /** Tell the view to render the icon in 20:11 AR instead of 1:1 icon */
    open val matchPSContentIcon : Boolean = false

    open val hasOptions : Boolean = false


    /** Called when this icon is shown into screen after being hidden */
    open fun onScreen(){}

    /** Called when this icon is hidden into screen after being shown */
    open fun onHidden(){}

    /** Called when is currently selected */
    open fun onSelected(){}

    /** Called when is no longer selected */
    open fun onUnselected(){}

    override var contentIndex: Int
        get() = TODO("Not yet implemented")
        set(value) {}

}
