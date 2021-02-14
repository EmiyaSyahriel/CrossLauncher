package id.psw.vshlauncher.icontypes

import android.content.Context
import id.psw.vshlauncher.*
import id.psw.vshlauncher.customtypes.Icon
import id.psw.vshlauncher.views.VshView
import java.util.*

class XMBRootIcon(context: VSH, vsh:VshView) : XMBIcon(context, vsh, Calendar.getInstance().time.toLocaleString()) {

    private val mIcon = Icon.fromBitmap(XMBIcon.TransparentBitmap)

    override val name: String
        get() = "<ROOT>"
    override val description: String
        get() = context.getString(R.string.xmb_icon_desc_root)

    override fun onLaunch() {

    }
}