package id.psw.vshlauncher.icontypes

import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.ContextCompat.startActivity
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.VshY

class AndroidSettingIcon(itemID:Int, private val vsh: VSH, private val itemName:String, private val settingAction:String, val icon:Bitmap) : VshY (itemID){

    override val onLaunch: Runnable
        get() = Runnable { vsh.startActivity(Intent(settingAction)) }
}