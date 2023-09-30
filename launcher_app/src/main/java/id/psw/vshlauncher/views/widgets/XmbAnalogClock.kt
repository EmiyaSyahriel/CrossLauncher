package id.psw.vshlauncher.views.widgets

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import id.psw.vshlauncher.FColor
import id.psw.vshlauncher.hasConcurrentLoading
import id.psw.vshlauncher.makeTextPaint
import id.psw.vshlauncher.nrm2Rad
import id.psw.vshlauncher.select
import id.psw.vshlauncher.toLerp
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.views.XmbWidget
import id.psw.vshlauncher.vsh
import java.util.Calendar

class XmbAnalogClock(view: XmbView) : XmbWidget(view) {
    private val statusOutlinePaint = vsh.makeTextPaint(size = 20.0f, color = Color.WHITE)
    private val statusFillPaint = Paint()
    private val tmpPointFB = PointF()
    private val tmpPointFA = PointF()
    private val calendar = Calendar.getInstance()
    var showSecondHand = false
    private var clockLoadTransition = 0.0f

    override fun render(ctx: Canvas) {
        val hh = calendar.get(Calendar.HOUR)
        val mm = calendar.get(Calendar.MINUTE)
        val ss = calendar.get(Calendar.SECOND)
        statusOutlinePaint.strokeWidth = 3.0f
        tmpPointFB.x = scaling.target.right - 85.0f
        tmpPointFB.y = view.top.toFloat()

        val tFactor = vsh.hasConcurrentLoading.select(1.0f, 0.0f)
        //clockLoadTransition = (clockLoadTransition + (tFactor * time.deltaTime)).coerceIn(0.0f, 1.0f)
        clockLoadTransition = (time.deltaTime * 5.0f).toLerp(clockLoadTransition, tFactor)

        fun calcHand(deg:Float, len:Float) : PointF {
            val tDegree = clockLoadTransition.toLerp(deg, (time.currentTime % 2.0f) / 2.0f)
            tmpPointFA.x = tmpPointFB.x + (kotlin.math.sin((0.5f - tDegree).nrm2Rad) * len)
            tmpPointFA.y = tmpPointFB.y + (kotlin.math.cos((0.5f - tDegree).nrm2Rad) * len)
            return tmpPointFA
        }

        statusFillPaint.color = FColor.argb(0.5f, 0f, 0f, 0f)
        ctx.drawCircle(tmpPointFB.x, tmpPointFB.y, 15.0f, statusFillPaint)
        ctx.drawCircle(tmpPointFB.x, tmpPointFB.y, 15.0f, statusOutlinePaint)

        if(context.vsh.hasConcurrentLoading){
            for(off in 0 until 2){
                val radTime = (((time.currentTime + (off * 1.5f)) % 3.0f) / 3.0f) * 20.0f
                val alphaTime = 1.0f - ((radTime - 20.0f) / 10.0f).coerceIn(0.0f, 1.0f)
                statusOutlinePaint.color = FColor.setAlpha(Color.WHITE, alphaTime)
                ctx.drawCircle(tmpPointFB.x, tmpPointFB.y, radTime, statusOutlinePaint)
            }
        }

        statusOutlinePaint.color = Color.WHITE
        val fss = ss / 60.0f
        val fmm = (mm + fss) / 60.0f
        val fhh = (hh + fmm) / 12.0f
        var handPt : PointF = tmpPointFA
        if(showSecondHand){
            statusOutlinePaint.color = Color.RED
            handPt = calcHand(fss, 15.0f)
            ctx.drawLine(tmpPointFB.x, tmpPointFB.y, handPt.x, handPt.y, statusOutlinePaint)
        }
        statusOutlinePaint.color = Color.WHITE
        handPt = calcHand(fmm, 15.0f)
        ctx.drawLine(tmpPointFB.x, tmpPointFB.y, handPt.x, handPt.y, statusOutlinePaint)
        handPt = calcHand(fhh, 7.0f)
        ctx.drawLine(tmpPointFB.x, tmpPointFB.y, handPt.x, handPt.y, statusOutlinePaint)
    }
}