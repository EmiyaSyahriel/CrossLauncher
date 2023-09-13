package id.psw.vshlauncher.views

import android.content.Context
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.submodules.SubmoduleManager
import id.psw.vshlauncher.vsh

open class XmbSubview(val view: XmbView) {
    protected val vsh : VSH get() = view.context.vsh
    protected val context: Context get() = view.context
    protected val M : SubmoduleManager get() = vsh.M
    protected val scaling : XmbView.ScaleInfo get() = view.scaling
    protected val time : TimeData get() = view.time
    protected val screens get() = view.screens
    protected val widgets get() = view.widgets
}