package id.psw.vshlauncher.views.VshServerSubcomponent

import android.annotation.SuppressLint
import android.graphics.*
import id.psw.vshlauncher.views.VshServer
import id.psw.vshlauncher.views.VshServer.drawText
import id.psw.vshlauncher.views.VshView
import java.text.SimpleDateFormat
import java.util.*

object StatusBar {
    var hide = false
    var showOperatorName = true
    var operatorName = "No Operator"
    var use24Format = false
    var clockExpandInfo = "Some info here --"
    val shouldClockExpanded : Boolean get() = clockExpandInfo.isNotBlank()
    var isLoading = true
    var animateLoadClock = true
    val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply{
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply{
        color = Color.argb(0x88,0xFF,0xFF,0xFF)
        style = Paint.Style.FILL
    }
    val clockBackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply{
        color = Color.argb(0x88,0x00,0x00,0x00)
        style = Paint.Style.FILL
    }

    private fun calculateHandRotation(c: PointF, t : Float, r : Float) : PointF {
        val clockHand = PointF(0f,0f)
        if(animateLoadClock && isLoading){
            // Do rotating the clock by frame instead of clock
            clockHand.y = Math.sin(((Time.currentTime % 1f) * 360) * VshView.Deg2Rad).toFloat() * r
            clockHand.x = Math.cos(((Time.currentTime % 1f) * 360) * VshView.Deg2Rad).toFloat() * r
        }else{
            clockHand.x = Math.cos(((t * 360) - 90) * VshView.Deg2Rad).toFloat() * r
            clockHand.y = Math.sin(((t * 360) - 90) * VshView.Deg2Rad).toFloat() * r
        }
        return PointF(c.x + clockHand.x, c.y + clockHand.y)
    }

    fun drawClock(ctx: Canvas, x:Float, y:Float, r:Float) {
        ctx.drawCircle(x,y,r, clockBackPaint)
        ctx.drawCircle(x,y,r, outlinePaint)
        val min = Calendar.getInstance().get(Calendar.MINUTE) / 60f
        val hrs = Calendar.getInstance().get(Calendar.HOUR) / 12f
        val minPos = calculateHandRotation(PointF(x,y), min, r * 0.8f)
        val hrsPos = calculateHandRotation(PointF(x,y), hrs, r * 0.5f)
        ctx.drawLine(x,y,minPos.x, minPos.y, outlinePaint)
        ctx.drawLine(x,y,hrsPos.x, hrsPos.y, outlinePaint)
        if(isLoading){
            ctx.drawCircle(x,y, r*(Time.currentTime % 1.0f), outlinePaint)
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getFormattedClock():String {
        val hh = if(use24Format) "HH" else "hh"
        val df = SimpleDateFormat("dd/MM $hh:mm")
        return df.format(Calendar.getInstance().time)
    }

    fun lStatusBar(ctx : Canvas){
        val y = VshServer.orientHeight * 0.1f
        val stHeight = if(shouldClockExpanded) 75 else 40
        val statusRect = RectF(VshServer.orientWidth -600,y, VshServer.scaledScreenWidth + 20f,y + stHeight)

        ctx.drawRoundRect(statusRect, 5f,5f, fillPaint)
        ctx.drawRoundRect(statusRect, 5f,5f, outlinePaint)
        Paints.statusPaint.textAlign = Paint.Align.RIGHT

        val sb = StringBuilder()
        if(showOperatorName) sb.append(operatorName).append("        ")
        sb.append(getFormattedClock())
        ctx.drawText(sb.toString(), VshServer.orientWidth -80, y, Paints.statusPaint, 1.2f)
        if(shouldClockExpanded){
            ctx.drawText(clockExpandInfo, VshServer.orientWidth -80, y, Paints.statusPaint, 2.2f)
        }

        drawClock(ctx, VshServer.orientWidth - 40f, statusRect.centerY(), 22f)
    }
}
