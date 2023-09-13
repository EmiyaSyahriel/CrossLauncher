package id.psw.vshlauncher.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.submodules.PadKey
import id.psw.vshlauncher.submodules.SubmoduleManager
import id.psw.vshlauncher.vsh

open class XmbScreen(view : XmbView) : XmbSubview(view){
    var currentTime = 0.0f

    open fun start(){}
    open fun end(){}
    open fun render(ctx: Canvas){ }
    open fun onTouchScreen(start: PointF, current: PointF, action:Int){}
    open fun onGamepadInput(key: PadKey, isDown : Boolean) : Boolean { return false }
}