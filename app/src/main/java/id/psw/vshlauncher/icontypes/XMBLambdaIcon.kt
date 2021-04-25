package id.psw.vshlauncher.icontypes

import android.graphics.Bitmap

class XMBLambdaIcon(id: String) : XMBIcon(id) {

    var nameImpl : () -> String = { "" }
    var onClick = Runnable {  }
    var activeIconImpl : () -> Bitmap? = { null }
    var inactiveIconImpl : () -> Bitmap? = { null }
    var descriptionImpl : () -> String = {""}

    override val name: String get() = nameImpl.invoke()
    override val description: String get() = descriptionImpl.invoke()
    override var activeIcon: Bitmap
        get() = activeIconImpl.invoke() ?: blankBmp
        set(value) {}
    override var inactiveIcon: Bitmap
        get() = inactiveIconImpl.invoke() ?: blankBmp
        set(value) {}

    override fun onLaunch() { onClick.run() }
}