package id.psw.vshlauncher.views

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import id.psw.vshlauncher.R
import id.psw.vshlauncher.submodules.BitmapRef
import id.psw.vshlauncher.submodules.PadKey
import id.psw.vshlauncher.types.XmbItem
import id.psw.vshlauncher.vsh

open class XmbDialogSubview(var view : XmbView) {

    val vsh get() = view.context.vsh

    var isPSP: Boolean = false
    open val icon : Bitmap = XmbItem.TRANSPARENT_BITMAP
    open val useRefIcon : Boolean = false
    open val reficon : BitmapRef = BitmapRef("default_icon_dialog", {null}, BitmapRef.FallbackColor.Transparent)
    open val title : String = vsh.getString(R.string.default_dialog_title)
    open val negativeButton = vsh.getString(android.R.string.cancel)
    open val positiveButton = vsh.getString(android.R.string.ok)
    open val hasNegativeButton = false
    open val hasPositiveButton = false
    private var pCloseDialogTo : XmbScreen = vsh.xmbView?.screens?.mainMenu!!
    private var pShouldClose = false
    val closeDialogTo get() = pCloseDialogTo
    val shouldClose get() = pShouldClose

    fun finish(dialogTo:XmbScreen){
        pCloseDialogTo = dialogTo
        pShouldClose = true
    }

    open fun onStart(){

    }

    open fun onTouch(a:PointF, b:PointF, act:Int){

    }

    open fun onDialogButton(isPositive: Boolean){

    }

    open fun onGamepad(key: PadKey, isPress:Boolean) : Boolean {
        return false
    }

    open fun onCharInput(char:Char) : Boolean {
        return false
    }

    open fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime:Float){

    }

    open fun onClose(){

    }
}