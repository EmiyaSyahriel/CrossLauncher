package id.psw.vshlauncher.views.widgets

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import id.psw.vshlauncher.FColor
import id.psw.vshlauncher.getBatteryLevel
import id.psw.vshlauncher.isBatteryCharging
import id.psw.vshlauncher.makeTextPaint
import id.psw.vshlauncher.select
import id.psw.vshlauncher.typography.parseEncapsulatedBracket
import id.psw.vshlauncher.views.XmbLayoutType
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.views.XmbWidget
import id.psw.vshlauncher.views.drawRoundRect
import id.psw.vshlauncher.views.drawText
import id.psw.vshlauncher.views.setColorAndSize
import id.psw.vshlauncher.vsh
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.min

class XmbStatusBar(view: XmbView) : XmbWidget(view) {
    var disabled = false

    val statusFillPaint : Paint = vsh.makeTextPaint(color = FColor.setAlpha(Color.WHITE, 0.5f)).apply {
        style = Paint.Style.FILL
        strokeWidth = 3.0f
    }
    val statusTextPaint : Paint = vsh.makeTextPaint(size = 10.0f, color = Color.WHITE).apply {
        style = Paint.Style.FILL
        strokeWidth = 3.0f
    }
    val iconPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {}
    val menuVerticalNamePaint : Paint = vsh.makeTextPaint(size = 20.0f, color = Color.WHITE).apply {
        textAlign = Paint.Align.LEFT
    }
    val menuVerticalDescPaint : Paint = vsh.makeTextPaint(size = 10.0f, color = Color.WHITE).apply {
        textAlign = Paint.Align.LEFT
    }

    val menuHorizontalNamePaint : Paint = vsh.makeTextPaint(size = 15.0f, color = Color.WHITE).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }
    val menuHorizontalIconPaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        alpha = 255
    }
    val statusOutlinePaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.0f
        color = Color.WHITE
    }

    private var baseDefRect = RectF()
    private var tmpRectF = RectF()
    private var tmpPointFA = PointF()
    private var tmpPointFB = PointF()
    private val tmpPath = Path()
    var showAnalogClock = true
    var pspPadStatusBar = true
    var dateTimeFormat = "{operator} {sdf:dd/M HH:mm a}"
    var pspShowBattery = true
    var showMobileOperator = false

    /**
     * Encapsulated text format
     * Values:
     * - `operator` - Network Name
     * - `sdf:(sdf_format)` - Simple Date Format, for reference see [SimpleDateFormat](https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html)
     */
    fun format(src:String) : String {
        val sb = StringBuilder()
        val cl = Calendar.getInstance()
        src.parseEncapsulatedBracket().forEachIndexed { i, s ->
            if(i % 2 == 0){
                sb.append(s)
            }else{
                sb.append(when(s){
                    // Operator
                    "operator" -> {
                        if(showMobileOperator)
                            M.network.operatorName
                        else ""
                    }

                    "battery" -> {
                        (vsh.getBatteryLevel() * 100).toInt().toString()
                    }
                    "charging" -> {
                        vsh.isBatteryCharging().select("⚡","")
                    }
                    else -> {
                        when {
                            s.startsWith("sdf:") -> {
                                SimpleDateFormat(s.substring(4), Locale.getDefault()).format(cl.time)
                            }
                            s.startsWith("battery_f:") -> {
                                "%.${s.substring(10)}f".format(context.vsh.getBatteryLevel() * 100)
                            }
                            s =="battery_f" -> {
                                "%f".format(vsh.getBatteryLevel() * 100)
                            }
                            else -> {
                                "{$s}"
                            }
                        }
                    }
                })
            }
        }
        return sb.toString()
    }

    private fun drawStatusBar(ctx: Canvas){
        if(disabled) return
        when(view.screens.mainMenu.layoutMode){
            XmbLayoutType.PS3 -> drawStatusBarPS3(ctx)
            XmbLayoutType.PSP -> drawStatusBarPSP(ctx)
            else -> {}
        }
    }

    private fun drawStatusBarPS3(ctx: Canvas){

        val top = scaling.target.top + (scaling.target.height() * 0.1f)
        val hSize = 40.0f / 2.0f
        val leftRound = (scaling.screen.width() > scaling.screen.height()).select(0.5f, 0.75f)
        baseDefRect.set(
                scaling.target.right - (scaling.target.width() * leftRound),
                top - hSize,
                scaling.viewport.right + 120.0f,
                top + hSize,
        )
        statusOutlinePaint.strokeWidth = 1.5f
        statusOutlinePaint.color = Color.WHITE
        statusFillPaint.setColorAndSize(FColor.argb(0.25f, 0.0f,0f,0f), 25.0f, android.graphics.Paint.Align.RIGHT)
        ctx.drawRoundRect(baseDefRect, 10.0f, statusFillPaint)
        ctx.drawRoundRect(baseDefRect, 10.0f, statusOutlinePaint)

        statusTextPaint.setColorAndSize(Color.WHITE, 20.0f, android.graphics.Paint.Align.RIGHT)

        val status = StringBuilder()

        status.append(format(dateTimeFormat))

        ctx.drawText(
                status.toString()
                , scaling.target.right - showAnalogClock.select(120.0f, 70.0f), top, statusTextPaint, 0.5f)

        if(showAnalogClock){
            view.widgets.analogClock.render(ctx)
        }
    }

    private fun drawStatusBarPSP(ctx: Canvas){

        val topBar = pspPadStatusBar.select(48f, 10f)

        // statusTextPaint.setShadowLayer(10.0f, 2.0f, 2.0f, Color.BLACK)
        statusTextPaint.setColorAndSize(Color.WHITE, 40.0f, Paint.Align.RIGHT)
        val statusText = StringBuilder()
        statusText.append(format(dateTimeFormat))

        ctx.drawText(statusText.toString(), scaling.target.right - 90f,  topBar, statusTextPaint, 1.0f)

        // region Battery Icon
        // Draw Battery Rect
        if(pspShowBattery){
            val charging = context.vsh.isBatteryCharging()
            val battery = context.vsh.getBatteryLevel()
            statusFillPaint.color = charging.select(Color.YELLOW,  Color.WHITE)
            statusOutlinePaint.color = charging.select(Color.YELLOW,  Color.WHITE)
            statusOutlinePaint.strokeWidth = 3.0f

            tmpRectF.set(
                    scaling.target.right - 70f,
                    scaling.target.top + topBar + 5f,
                    scaling.target.right - 10f,
                    scaling.target.top + topBar + 30f
            )
            ctx.drawRoundRect(tmpRectF, 3.0f, statusOutlinePaint)
            statusOutlinePaint.strokeWidth = 5.0f
            ctx.drawLine(
                    tmpRectF.left - 3.0f,
                    tmpRectF.centerY() - 7.0f,
                    tmpRectF.left - 3.0f,
                    tmpRectF.centerY() + 7.0f,
                    statusOutlinePaint
            )

            val batteryBlocks = kotlin.math.floor (battery * 4).toInt()
            tmpPath.reset()
            val sWidth = 16.0f
            for(i in 0 .. min(batteryBlocks - 1, 2)){
                val left = tmpRectF.right - 2.0f - ((i + 1) * (sWidth + 2.0f))
                tmpPath.addRect(left, tmpRectF.top + 3.0f, left+ sWidth, tmpRectF.bottom - 3.0f, Path.Direction.CW)
            }
            ctx.drawPath(tmpPath, statusFillPaint)
        }
        // endregion
    }


}