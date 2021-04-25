package id.psw.vshlauncher.icontypes

import android.graphics.Bitmap
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.VshY
import id.psw.vshlauncher.customtypes.Icon
import id.psw.vshlauncher.views.VshView

class VshSettingCategory (
    itemID:String,
    override val name:String,
    override val description: String,
    override var icon : Icon
) : XMBIcon (itemID){

}