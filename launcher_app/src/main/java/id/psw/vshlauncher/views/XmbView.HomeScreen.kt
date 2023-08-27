package id.psw.vshlauncher.views

import android.graphics.Canvas
import android.graphics.PointF
import android.view.MotionEvent
import id.psw.vshlauncher.submodules.GamepadSubmodule
import id.psw.vshlauncher.submodules.PadKey

class HomeScreenState {
    var lastTap = 0L
}

fun XmbView.homeRender(ctx: Canvas){
    menuRenderStatusBar(ctx)
}

fun XmbView.homeOnTouchScreen(a: PointF, b:PointF, act:Int){
    with(state.home){
        if(act == MotionEvent.ACTION_DOWN){
            val cTime = System.currentTimeMillis()
            if(cTime - lastTap < 300){
                switchPage(VshViewPage.MainMenu)
            }
            lastTap = cTime
        }
    }
}

fun XmbView.homeOnGamepad(k: PadKey, isDown:Boolean) : Boolean
{
    if(
        (PadKey.isConfirm(k) || k == PadKey.Start) && isDown
    ){
        switchPage(VshViewPage.MainMenu)
        return true
    }
    return false
}