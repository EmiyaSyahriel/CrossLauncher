package id.psw.vshlauncher.views.widgets

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.view.MotionEvent
import id.psw.vshlauncher.activities.XMB
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.views.XmbWidget

class XmbDebugTouch(view: XmbView) : XmbWidget(view) {

    var dummyPaint = Paint()
    var touchCurrentPointF = PointF()
    var touchStartPointF = PointF()
    var lastTouchAction : Int = 0

    private fun drawDebugLocation(ctx: Canvas, xmb: XMB) {
        if(lastTouchAction == MotionEvent.ACTION_DOWN || lastTouchAction == MotionEvent.ACTION_MOVE){
            dummyPaint.style= Paint.Style.FILL
            dummyPaint.color = Color.argb(128,255,255,255)
            ctx.drawCircle(touchCurrentPointF.x, touchCurrentPointF.y, 10.0f, dummyPaint)
            ctx.drawCircle(touchStartPointF.x, touchStartPointF.y, 10.0f, dummyPaint)
        }
    }

}