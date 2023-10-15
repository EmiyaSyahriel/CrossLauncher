package id.psw.vshlauncher.views.screens

import android.graphics.Canvas
import android.graphics.PointF
import android.view.MotionEvent
import id.psw.vshlauncher.submodules.PadKey
import id.psw.vshlauncher.views.XmbScreen
import id.psw.vshlauncher.views.XmbView

class XmbIdleScreen (view : XmbView) : XmbScreen(view) {
    private var lastTap = 0L
    override fun render(ctx: Canvas) {
        view.widgets.statusBar.render(ctx)
        super.render(ctx)
    }

    override fun onTouchScreen(start: PointF, current:PointF, action:Int){
        if(action == MotionEvent.ACTION_DOWN){
            val cTime = System.currentTimeMillis()
            if(cTime - lastTap < 300){
                view.switchScreen(view.screens.mainMenu)
            }
            lastTap = cTime
        }
    }

    override fun onGamepadInput(key: PadKey, isDown: Boolean): Boolean {
        if(
            (PadKey.isConfirm(key) || key == PadKey.Start) && isDown
        ){
            view.switchScreen(view.screens.mainMenu)
            return true
        }
        return false
    }
}