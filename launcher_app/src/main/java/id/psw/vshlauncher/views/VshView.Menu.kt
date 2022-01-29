package id.psw.vshlauncher.views

import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.os.BatteryManager
import id.psw.vshlauncher.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.min

class VshViewMainMenuState {
    var currentTime : Float = 0.0f
    var layoutMode : XMBLayoutType = XMBLayoutType.PS3
    var bgOverlayColorA : Int = 0x88FF0000u.toInt()
    var bgOverlayColorB : Int = 0x8800FF00u.toInt()
    var isFocused : Boolean = false
    var clockLoadTransition : Float = 1.0f
    val readPhoneStateAllowed : Boolean = false

    data class DateTimeFormatSetting(
        var showSecond : Boolean = false,
        var showYearFormat : Int = 0,
        var use12Format : Boolean = true,
        var showAMPM : Boolean = false,
        var secondOnAnalog : Boolean = false
    )

    data class StatusBarSetting(
        var disabled : Boolean = false,
        var showMobileOperator : Boolean = true,
        var showBattery : Boolean = true,
        var showBatteryPerc : Boolean = true,
        var showAnalogClock : Boolean = true,
        var padPSPStatusBar : Boolean = false
    );

    val dateTime = DateTimeFormatSetting()
    val statusBar = StatusBarSetting()

    val backgroundPaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val statusOutlinePaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.0f
        color = Color.WHITE
    }
    val statusFillPaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth = 3.0f
        color = FColor.setAlpha(Color.WHITE, 0.5f)
    }
    val statusTextPaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth = 3.0f
        color = Color.WHITE
        textSize = 20f
    }

    val menuHorizontalNamePaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
        textSize = 20.0f
        textAlign = Paint.Align.CENTER
    }
    val menuHorizontalIconPaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        alpha = 255
    }
}

fun VshView.menuStart(){
    state.menu.currentTime = 0.0f
}

private var baseDefRect = RectF()
private var tmpRectF = RectF()
private var tmpPointFA = PointF()
private var tmpPointFB = PointF()

fun VshView.getSettingDateTimeFormat(): String {
    with(state.menu.dateTime){
        val sb = StringBuilder()
        sb.append("d/M")
        if(showYearFormat == 2) sb.append("/yy")
        if(showYearFormat == 4) sb.append("/yyyy")
        sb.append(" ")
        sb.append(use12Format.select("K", "k"))
        sb.append(":m")
        if(showSecond) sb.append(":s")
        if(use12Format && showAMPM) sb.append(" a")

        return sb.toString()
    }
}

fun VshView.menu3StatusBar(ctx:Canvas){
    with(state.menu){
        val top = scaling.target.top + (scaling.target.height() * 0.1f)
        val hSize = 50.0f /2.0f
        val leftRound = (scaling.screen.width() > scaling.screen.height()).select(0.5f, 0.75f)
        baseDefRect.set(
            scaling.target.right - (scaling.target.width() * leftRound),
            top - hSize,
            scaling.viewport.right + 120.0f,
            top + hSize,
        )
        statusOutlinePaint.strokeWidth = 1.5f
        statusOutlinePaint.color = Color.WHITE
        statusFillPaint.setColorAndSize(FColor.argb(0.25f, 0.0f,0f,0f), 25.0f, Paint.Align.RIGHT)
        ctx.drawRoundRect(baseDefRect, 10.0f, statusFillPaint)
        ctx.drawRoundRect(baseDefRect, 10.0f, statusOutlinePaint)

        statusTextPaint.setColorAndSize(Color.WHITE, 25.0f, Paint.Align.RIGHT)

        val calendar = Calendar.getInstance()

        val status = StringBuilder()

        if(statusBar.showMobileOperator){
            status.append(VSH.Network.operatorName).append("  ")
        }

        status.append(SimpleDateFormat(getSettingDateTimeFormat(), Locale.getDefault()).format(calendar.time))

        ctx.drawText(
            status.toString()
            , scaling.target.right - statusBar.showAnalogClock.select(120.0f, 70.0f), top, statusTextPaint, 0.5f)

        if(statusBar.showAnalogClock){
            val hh = calendar.get(Calendar.HOUR)
            val mm = calendar.get(Calendar.MINUTE)
            val ss = calendar.get(Calendar.SECOND)
            statusOutlinePaint.strokeWidth = 3.0f
            tmpPointFB.x = scaling.target.right - 85.0f
            tmpPointFB.y = top

            val tFactor = context.vsh.hasConcurrentLoading.select(1.0f, -1.0f)
            clockLoadTransition = (clockLoadTransition + (tFactor * time.deltaTime)).coerceIn(0.0f, 1.0f)

            fun calcHand(deg:Float, len:Float) : PointF {
                val tDegree = clockLoadTransition.toLerp(deg, (currentTime % 2.0f) / 2.0f)
                tmpPointFA.x = tmpPointFB.x + kotlin.math.sin((0.5f - tDegree) * (2.0f * Math.PI).toFloat()) * len
                tmpPointFA.y = tmpPointFB.y + kotlin.math.cos((0.5f - tDegree) * (2.0f * Math.PI).toFloat()) * len
                return tmpPointFA
            }

            statusFillPaint.color = FColor.argb(0.5f, 0f, 0f, 0f)
            ctx.drawCircle(tmpPointFB.x, tmpPointFB.y, 20.0f, statusFillPaint)
            ctx.drawCircle(tmpPointFB.x, tmpPointFB.y, 20.0f, statusOutlinePaint)

            if(context.vsh.hasConcurrentLoading){
                for(off in 0 until 2){
                    val radTime = (((currentTime + (off * 1.5f)) % 3.0f) / 3.0f) * 30.0f
                    val alphaTime = 1.0f - ((radTime - 20.0f) / 10.0f).coerceIn(0.0f, 1.0f)
                    statusOutlinePaint.color = FColor.setAlpha(Color.WHITE, alphaTime)
                    ctx.drawCircle(tmpPointFB.x, tmpPointFB.y,radTime, statusOutlinePaint)
                }
            }

            statusOutlinePaint.color = Color.WHITE
            val fss = ss / 60.0f
            val fmm = (mm + fss) / 60.0f
            val fhh = (hh + fmm) / 12.0f
            var handPt = calcHand(fmm, 20.0f)
            ctx.drawLine(tmpPointFB.x, tmpPointFB.y, handPt.x, handPt.y, statusOutlinePaint)
            handPt = calcHand(fhh, 10.0f)
            ctx.drawLine(tmpPointFB.x, tmpPointFB.y, handPt.x, handPt.y, statusOutlinePaint)
            if(dateTime.secondOnAnalog){
                statusOutlinePaint.color = Color.RED
                handPt = calcHand(fss, 20.0f)
                ctx.drawLine(tmpPointFB.x, tmpPointFB.y, handPt.x, handPt.y, statusOutlinePaint)
            }
        }
    }
}

private val tmpPath = Path()

fun VshView.menuPStatusBar(ctx:Canvas){
    with(state.menu){
        val calendar = Calendar.getInstance()

        val topBar = statusBar.padPSPStatusBar.select(48f, 10f)
        var battery = 0
        var charging = false

        run {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val bStat = context.vsh.registerReceiver(null, intentFilter)
            val level = bStat?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
            val scale = bStat?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
            val chargeState = bStat?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            charging = chargeState == BatteryManager.BATTERY_STATUS_CHARGING || chargeState == BatteryManager.BATTERY_STATUS_FULL
            battery = ((level.toFloat() / scale.toFloat()) * 100).toInt()
        }

        statusTextPaint.setColorAndSize(Color.WHITE, 40.0f, Paint.Align.RIGHT)
        val statusText = StringBuilder()

        statusText.append(SimpleDateFormat(getSettingDateTimeFormat(), Locale.getDefault()).format(calendar.time))

        if(statusBar.showBatteryPerc){
            statusText.append(" $battery%")
        }

        ctx.drawText(statusText.toString(), scaling.target.right - 90f,  topBar, statusTextPaint, 1.0f)

        // region Battery Icon
        if(statusBar.showBattery){
            // Draw Battery Rect

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

            val battBlock = kotlin.math.floor ((battery / 100.0f) * 4).toInt()
            tmpPath.reset()
            val sWidth = 16.0f
            for(i in 0 .. min(battBlock - 1, 2)){
                val left = tmpRectF.right - 2.0f - ((i + 1) * (sWidth + 2.0f))
                tmpPath.addRect(left, tmpRectF.top + 3.0f, left+ sWidth, tmpRectF.bottom - 3.0f, Path.Direction.CW)
            }
            ctx.drawPath(tmpPath, statusFillPaint)
        }
        // endregion
    }
}

fun VshView.menuXStatusBar(ctx:Canvas){

}

fun VshView.menuRenderStatusBar(ctx:Canvas){
    with(state.menu){
        if(statusBar.disabled) return
        when(layoutMode){
            XMBLayoutType.PS3 -> menu3StatusBar(ctx)
            XMBLayoutType.PSP -> menuPStatusBar(ctx)
            else -> menuXStatusBar(ctx)
        }
    }
}

fun VshView.menuDrawBackground(ctx:Canvas) {
    with(state.menu){
        statusFillPaint.color = FColor.setAlpha(Color.BLACK, 0.25f)
        ctx.drawRect(scaling.target, statusFillPaint)
    }
}

private val ps3MenuIconCenter = PointF(0.30f, 0.25f)
private val pspMenuIconCenter = PointF(0.20f, 0.25f)
private val ps3SelectedCategoryIconSize = PointF(125.0f, 125.0f)
private val ps3UnselectedCategoryIconSize = PointF(80.0f, 80.0f)
private val horizontalRectF = RectF()

fun VshView.menu3HorizontalMenu(ctx:Canvas){
    with(state.menu){
        val xPos = scaling.target.width() * ps3MenuIconCenter.x
        val yPos = scaling.target.height() * ps3MenuIconCenter.y
        for(wx in context.vsh.categories.indices){
            val item = context.vsh.categories[wx]
            val ix = wx - context.vsh.itemCursorX
            val selected = ix == 0
            val targetSize = selected.select(ps3SelectedCategoryIconSize, ps3UnselectedCategoryIconSize)
            var size = targetSize

            if(selected){
                val sizeTransite = abs(context.vsh.itemOffsetX)
                val previoSize = selected.select(ps3UnselectedCategoryIconSize, ps3SelectedCategoryIconSize)
                tmpPointFA.x = sizeTransite.toLerp(targetSize.x, previoSize.x)
                tmpPointFA.y = sizeTransite.toLerp(targetSize.y, previoSize.y)
                size = tmpPointFA
            }

            val hSizeX = size.x / 2.0f
            val hSizeY = (size.y / 2.0f)
            var centerX = xPos + ((ix + context.vsh.itemOffsetX) * 150f)

            if(centerX > scaling.viewport.left && centerX < scaling.viewport.right){

                if(ix < 0) centerX -= 10.0f
                if(ix > 0) centerX += 10.0f
                horizontalRectF.set(centerX - hSizeX, yPos - hSizeY, centerX + hSizeX, yPos + hSizeY)
                menuHorizontalIconPaint.alpha = selected.select(255, 200)
                ctx.drawBitmap(item.icon, null, horizontalRectF, menuHorizontalIconPaint, FittingMode.FIT, 0.5f, 1.0f)
                if(selected){
                    ctx.drawText(item.displayName, centerX, yPos + (ps3SelectedCategoryIconSize.y / 2.0f), menuHorizontalNamePaint, 1.0f)
                }

            }
        }
    }
}

fun VshView.menuPHorizontalMenu(ctx:Canvas){

}

fun VshView.menuXHorizontalMenu(ctx:Canvas){

}

fun VshView.menuRenderHorizontalMenu(ctx:Canvas){
    if(!context.vsh.isInRoot) return
    with(state.menu){
        when(layoutMode){
            XMBLayoutType.PS3 -> menu3HorizontalMenu(ctx)
            XMBLayoutType.PSP -> menuPHorizontalMenu(ctx)
            else ->              menuXHorizontalMenu(ctx)
        }
    }
}

fun VshView.menuRender(ctx: Canvas){
    state.menu.currentTime += time.deltaTime
    menuDrawBackground(ctx)
    menuRenderHorizontalMenu(ctx)
    menuRenderStatusBar(ctx)
}

fun VshView.menuEnd(){

}