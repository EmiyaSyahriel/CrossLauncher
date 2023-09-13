package id.psw.vshlauncher.views

import android.content.Context
import android.graphics.Canvas
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.submodules.SubmoduleManager
import id.psw.vshlauncher.vsh

open class XmbWidget(view: XmbView) : XmbSubview(view) {
    open fun render(ctx: Canvas) { }
}