package id.psw.vshlauncher.icontypes

import android.graphics.Bitmap
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.VshY
import id.psw.vshlauncher.customtypes.Icon
import id.psw.vshlauncher.views.VshView

class VshSettingCategory (
    val ctx: VSH,
    val view : VshView,
    itemID:String,
    override val name:String,
    override val description: String,
    override val icon : Icon
) : XMBIcon (ctx, view, itemID){


    fun add(icon : VshY){ content.add(icon) }

}