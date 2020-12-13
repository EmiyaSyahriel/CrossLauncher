package id.psw.vshlauncher.icontypes

import android.graphics.Bitmap
import id.psw.vshlauncher.VshY

class VshSettingCategory (
    itemID:Int,
    override val name:String,
    override val description: String,
    val icon : Bitmap
) : VshY(itemID) {
    override var hasSubContent: Boolean = true
    override var subContent: ArrayList<VshY>? = arrayListOf()

    fun add(icon : VshY){ subContent?.add(icon) }

}