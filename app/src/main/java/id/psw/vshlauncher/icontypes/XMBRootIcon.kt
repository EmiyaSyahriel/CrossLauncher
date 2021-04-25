package id.psw.vshlauncher.icontypes

import android.content.Context
import id.psw.vshlauncher.*
import id.psw.vshlauncher.customtypes.Icon
import id.psw.vshlauncher.views.VshView
import java.util.*

class XMBRootIcon() : XMBIcon(Calendar.getInstance().time.toLocaleString()) {

    private val mIcon = Icon.blankBitmap

    override val name: String
        get() = "<ROOT>"
    override val description: String
        get() = "<SHOULD NOT VISIBLE>"

    override fun onLaunch() {

    }
}