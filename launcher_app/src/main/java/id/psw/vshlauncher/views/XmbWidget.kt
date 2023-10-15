package id.psw.vshlauncher.views

import android.graphics.Canvas

open class XmbWidget(view: XmbView) : XmbSubview(view) {
    open fun render(ctx: Canvas) { }
}