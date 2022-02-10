package id.psw.vshlauncher.views

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import id.psw.vshlauncher.R
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.submodules.GamepadSubmodule
import id.psw.vshlauncher.types.XMBItem

open class XmbDialogSubview(private val vsh: VSH) {
    open val icon : Bitmap = XMBItem.TRANSPARENT_BITMAP
    open val title : String = vsh.getString(R.string.default_dialog_title)
    open val negativeButton = vsh.getString(android.R.string.cancel)
    open val positiveButton = vsh.getString(android.R.string.ok)
    open val neutralButton = vsh.getString(android.R.string.untitled)
    open val hasNegativeButton = false
    open val hasPositiveButton = false
    open val hasNeutralButton = false
    private var pCloseDialogTo : VshViewPage = VshViewPage.MainMenu
    private var pShouldClose = false
    val closeDialogTo get() = pCloseDialogTo
    val shouldClose get() = pShouldClose

    protected fun finish(dialogTo:VshViewPage){
        pCloseDialogTo = dialogTo
        pShouldClose = true
    }

    open fun onCreate(){

    }

    open fun onStart(){

    }

    open fun onDraw(ctx: Canvas, drawBound: RectF){
    }

    open fun onClose(){

    }
}