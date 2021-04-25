package id.psw.vshlauncher.icontypes

import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.ContextCompat.startActivity
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.VshY
import id.psw.vshlauncher.views.VshView

class AndroidSettingIcon(var vsh: VSH, vshView: VshView, itemID: String, val settingAction: String) : XMBIcon (itemID){
    override fun onLaunch() { vsh.startActivity(Intent(settingAction)) }
}