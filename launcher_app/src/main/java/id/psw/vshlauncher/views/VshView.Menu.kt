package id.psw.vshlauncher.views

import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.os.BatteryManager
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import id.psw.vshlauncher.*
import java.lang.Exception
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.ConcurrentModificationException
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

class VshViewMainMenuState {
    var currentTime : Float = 0.0f
    var layoutMode : XMBLayoutType = XMBLayoutType.PS3
    var bgOverlayColorA : Int = 0x88FF0000u.toInt()
    var bgOverlayColorB : Int = 0x8800FF00u.toInt()
    var isFocused : Boolean = false
    var clockLoadTransition : Float = 1.0f
    var menuScaleTime : Float = 0.0f
    var loadingIconBitmap : Bitmap? = null

    data class StatusBarSetting(
        var disabled : Boolean = false,
        var showMobileOperator : Boolean = true,
        var showBattery : Boolean = true,
        var showBatteryPercentage : Boolean = true,
        var showAnalogClock : Boolean = true,
        var padPSPStatusBar : Boolean = false,
        var secondOnAnalog : Boolean = true
    )

    data class VerticalMenu(
        var playAnimatedIcon : Boolean = true,
        var playBackSound : Boolean = true,
        var showBackdrop : Boolean = true
    )

    val dateTimeFormat = "dd/M HH:mm a"
    val statusBar = StatusBarSetting()
    val verticalMenu = VerticalMenu()

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
    val iconPaint: Paint= Paint(Paint.ANTI_ALIAS_FLAG).apply {

    }
    val menuVerticalNamePaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.LEFT
        textSize = 30.0f
    }
    val menuVerticalDescPaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.LEFT
        textSize = 20.0f
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
    state.menu.menuScaleTime = 1.0f

    if(state.menu.loadingIconBitmap == null){
        state.menu.loadingIconBitmap = ResourcesCompat.getDrawable(context.resources,R.drawable.ic_sync_loading,null)?.toBitmap(256,256)
    }
}

private var baseDefRect = RectF()
private var tmpRectF = RectF()
private var tmpPointFA = PointF()
private var tmpPointFB = PointF()

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

        status.append(SimpleDateFormat(dateTimeFormat, Locale.getDefault()).format(calendar.time))

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

            val tFactor = context.vsh.hasConcurrentLoading.select(1.0f, 0.0f)
            //clockLoadTransition = (clockLoadTransition + (tFactor * time.deltaTime)).coerceIn(0.0f, 1.0f)
            clockLoadTransition = (time.deltaTime * 5.0f).toLerp(clockLoadTransition, tFactor)

            fun calcHand(deg:Float, len:Float) : PointF {
                val tDegree = clockLoadTransition.toLerp(deg, (currentTime % 2.0f) / 2.0f)
                tmpPointFA.x = tmpPointFB.x + (kotlin.math.sin((0.5f - tDegree).nrm2Rad) * len)
                tmpPointFA.y = tmpPointFB.y + (kotlin.math.cos((0.5f - tDegree).nrm2Rad) * len)
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
            var handPt : PointF = tmpPointFA
            if(statusBar.secondOnAnalog){
                statusOutlinePaint.color = Color.RED
                handPt = calcHand(fss, 20.0f)
                ctx.drawLine(tmpPointFB.x, tmpPointFB.y, handPt.x, handPt.y, statusOutlinePaint)
            }
            statusOutlinePaint.color = Color.WHITE
            handPt = calcHand(fmm, 20.0f)
            ctx.drawLine(tmpPointFB.x, tmpPointFB.y, handPt.x, handPt.y, statusOutlinePaint)
            handPt = calcHand(fhh, 10.0f)
            ctx.drawLine(tmpPointFB.x, tmpPointFB.y, handPt.x, handPt.y, statusOutlinePaint)
        }
    }
}

private val tmpPath = Path()

fun VshView.menuPStatusBar(ctx:Canvas){
    with(state.menu){
        val calendar = Calendar.getInstance()

        val topBar = statusBar.padPSPStatusBar.select(48f, 10f)

        // TODO : Old API, change to Listener one
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val bStat = context.vsh.registerReceiver(null, intentFilter)
        val level = bStat?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
        val scale = bStat?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val chargeState = bStat?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val charging = chargeState == BatteryManager.BATTERY_STATUS_CHARGING || chargeState == BatteryManager.BATTERY_STATUS_FULL
        val battery = ((level.toFloat() / scale.toFloat()) * 100).toInt()
        statusTextPaint.setShadowLayer(10.0f, 2.0f, 2.0f, Color.BLACK)
        statusTextPaint.setColorAndSize(Color.WHITE, 40.0f, Paint.Align.RIGHT)
        val statusText = StringBuilder()

        statusText.append(SimpleDateFormat(dateTimeFormat, Locale.getDefault()).format(calendar.time))

        if(statusBar.showBatteryPercentage){
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

            val batteryBlocks = kotlin.math.floor ((battery / 100.0f) * 4).toInt()
            tmpPath.reset()
            val sWidth = 16.0f
            for(i in 0 .. min(batteryBlocks - 1, 2)){
                val left = tmpRectF.right - 2.0f - ((i + 1) * (sWidth + 2.0f))
                tmpPath.addRect(left, tmpRectF.top + 3.0f, left+ sWidth, tmpRectF.bottom - 3.0f, Path.Direction.CW)
            }
            ctx.drawPath(tmpPath, statusFillPaint)
            statusTextPaint.removeShadowLayer()
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
        if(verticalMenu.showBackdrop){
            try{
                val activeItem = context.vsh.items?.visibleItems?.find{it.id == context.vsh.selectedItemId}
                if(activeItem != null){
                    context.vsh.itemBackdropAlphaTime =context.vsh.itemBackdropAlphaTime.coerceIn(0f, 1f)
                    backgroundPaint.alpha = (context.vsh.itemBackdropAlphaTime * 255).roundToInt().coerceIn(0, 255)
                    if((scaling.screen.height() > scaling.screen.width()) && activeItem.hasPortraitBackdrop){
                        if(activeItem.isPortraitBackdropLoaded){
                            ctx.drawBitmap(
                                activeItem.portraitBackdrop,
                                null,
                                scaling.viewport,
                                backgroundPaint,
                                FittingMode.FILL, 0.5f, 0.5f)
                            if(context.vsh.itemBackdropAlphaTime < 1.0f) context.vsh.itemBackdropAlphaTime += (time.deltaTime) * 2.0f
                        }
                    }else if(activeItem.hasBackdrop){
                        if(activeItem.isBackdropLoaded){
                            ctx.drawBitmap(
                                activeItem.backdrop,
                                null,
                                scaling.viewport,
                                backgroundPaint,
                                FittingMode.FILL, 0.5f, 0.5f)
                            if(context.vsh.itemBackdropAlphaTime < 1.0f) context.vsh.itemBackdropAlphaTime += (time.deltaTime) * 2.0f
                        }
                    }
                }
            }catch(cme:ConcurrentModificationException){

            }
        }
    }
}

private val ps3MenuIconCenter = PointF(0.30f, 0.25f)
private val pspMenuIconCenter = PointF(0.20f, 0.25f)
private val ps3SelectedCategoryIconSize = PointF(100.0f, 100.0f)
private val ps3UnselectedCategoryIconSize = PointF(80.0f, 80.0f)
private val pspSelectedCategoryIconSize = PointF(150.0f, 150.0f)
private val pspUnselectedCategoryIconSize = PointF(100.0f, 100.0f)

// PS3 Base Icon Aspect Ratio = 20:11 (320x176)
private val ps3SelectedIconSize = PointF(240.0f, 132.0f)
private val ps3UnselectedIconSize = PointF(160.0f, 88.0f)

// PSP Base Icon Aspect Ratio = 18:10 (144x80)
private val pspSelectedIconSize = PointF(270.0f, 150.0f)
private val pspUnselectedIconSize = PointF(180.0f, 100.0f)

private val pspIconSeparation = PointF(200.0f, 105.0f)
private val ps3IconSeparation = PointF(150.0f, 100.0f)
private val horizontalRectF = RectF()

fun VshView.menu3HorizontalMenu(ctx:Canvas){
    with(state.menu){
        val center = (layoutMode == XMBLayoutType.PSP).select(pspMenuIconCenter, ps3MenuIconCenter)
        val xPos = scaling.target.width() * center.x
        val yPos = scaling.target.height() * center.y
        val notHidden = context.vsh.categories.visibleItems
        val separation = (layoutMode == XMBLayoutType.PSP).select(pspIconSeparation, ps3IconSeparation).x
        val cursorX = context.vsh.itemCursorX
        for(wx in notHidden.indices){
            val item = notHidden[wx]
            val ix = wx - cursorX
            val selected = ix == 0
            val targetSize =
                when(layoutMode){
                    XMBLayoutType.PSP -> selected.select(pspSelectedCategoryIconSize, pspUnselectedCategoryIconSize)
                    else -> selected.select(ps3SelectedCategoryIconSize, ps3UnselectedCategoryIconSize)
                }
            var size = targetSize

            if(selected){
                val sizeTransition = abs(context.vsh.itemOffsetX)
                val previousSize =
                    when(layoutMode){
                        XMBLayoutType.PSP -> selected.select(pspUnselectedCategoryIconSize, pspSelectedCategoryIconSize)
                        else -> selected.select(ps3UnselectedCategoryIconSize, ps3SelectedCategoryIconSize)
                    }
                tmpPointFA.x = sizeTransition.toLerp(targetSize.x, previousSize.x)
                tmpPointFA.y = sizeTransition.toLerp(targetSize.y, previousSize.y)
                size = tmpPointFA


                val radius = kotlin.math.abs((kotlin.math.sin(currentTime / 3.0f)) * 10f)
                menuHorizontalNamePaint.setShadowLayer(radius, 0f, 0f, Color.WHITE)
                menuHorizontalIconPaint.setShadowLayer(radius, 0f, 0f, Color.BLACK)
            }else{
                menuHorizontalNamePaint.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
                menuHorizontalIconPaint.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
            }

            val hSizeX = size.x / 2.0f
            val hSizeY = (size.y / 2.0f)
            var centerX = xPos + ((ix + context.vsh.itemOffsetX) * separation)

            val isInViewport = centerX > scaling.viewport.left && centerX < scaling.viewport.right

            item.screenVisibility = isInViewport

            if(isInViewport){

                if(ix < 0) centerX -= 10.0f
                if(ix > 0) centerX += 10.0f
                horizontalRectF.set(centerX - hSizeX, yPos - hSizeY, centerX + hSizeX, yPos + hSizeY)
                menuHorizontalIconPaint.alpha = selected.select(255, 200)
                ctx.drawBitmap(item.icon, null, horizontalRectF, menuHorizontalIconPaint, FittingMode.FIT, 0.5f, 1.0f)
                if(selected){
                    val iconCtrToText = (layoutMode == XMBLayoutType.PSP).select(pspSelectedCategoryIconSize, ps3SelectedCategoryIconSize).y
                    menuHorizontalNamePaint.textSize = (layoutMode == XMBLayoutType.PSP).select(25f, 20f)
                    ctx.drawText(item.displayName, centerX, yPos + (iconCtrToText / 2.0f), menuHorizontalNamePaint, 1.0f)
                }
            }
        }
    }
}

private val verticalRectF = RectF()

fun VshView.menuRenderVerticalMenu(ctx:Canvas){
    val items = context.vsh.items?.visibleItems
    val loadIcon = state.menu.loadingIconBitmap
    val isPSP = state.menu.layoutMode == XMBLayoutType.PSP
    with(state.menu){
        if(items != null){
            val center = (isPSP).select(pspMenuIconCenter,ps3MenuIconCenter)
            val hSeparation = (isPSP).select(pspIconSeparation, ps3IconSeparation).x
            val separation = (isPSP).select(pspIconSeparation, ps3IconSeparation).y
            val xPos = (scaling.target.width() * center.x) + (context.vsh.itemOffsetX * hSeparation)
            val yPos = (scaling.target.height() * center.y) + (separation * 2.0f)
            val iconCenterToText = (isPSP).select(pspSelectedIconSize.x * 0.40f, ps3SelectedIconSize.x * 0.60f)
            val cursorY = context.vsh.itemCursorY
            for(wx in items.indices){
                val iy = wx - cursorY
                val selected = iy == 0
                val item = items[wx]

                val textAlpha = selected.select(255, 128)
                menuVerticalDescPaint.alpha = textAlpha
                menuVerticalNamePaint.alpha = textAlpha

                if(selected){
                    val radius = abs((kotlin.math.sin(currentTime / 3.0f)) * 10f)
                    menuVerticalNamePaint.setShadowLayer(radius, 0f, 0f, Color.WHITE)
                    menuVerticalDescPaint.setShadowLayer(radius, 0f, 0f, Color.WHITE)
                    iconPaint.setShadowLayer(radius, 0f, 0f, Color.WHITE)
                }else{
                    menuVerticalNamePaint.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
                    menuVerticalDescPaint.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
                    iconPaint.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
                }

                val targetSize =
                    isPSP.select(
                        selected.select(pspSelectedIconSize, pspUnselectedIconSize),
                        selected.select(ps3SelectedIconSize, ps3UnselectedIconSize)
                    )
                var size = targetSize

                if(selected){
                    val sizeTransition = abs(context.vsh.itemOffsetY)
                    val previousSize =
                        isPSP.select(
                            selected.select(pspUnselectedIconSize, pspSelectedIconSize),
                            selected.select(ps3UnselectedIconSize, ps3SelectedIconSize))
                    tmpPointFA.x = sizeTransition.toLerp(targetSize.x, previousSize.x)
                    tmpPointFA.y = sizeTransition.toLerp(targetSize.y, previousSize.y)
                    size = tmpPointFA
                }

                val hSizeX = size.x / 2.0f
                val hSizeY = (size.y / 2.0f)
                var centerY = yPos + ((iy + context.vsh.itemOffsetY) * separation)

                if(iy < 0) centerY -= size.y * 3.0f
                if(iy > 0) centerY += size.y * 0.5f

                val isInViewport = (centerY + hSizeY) > scaling.viewport.top && (centerY - hSizeY) < scaling.viewport.bottom

                item.screenVisibility = isInViewport

                if(isInViewport){
                    if(isPSP){
                        val pspCX = xPos - (pspUnselectedIconSize.x * 0.25f)
                        verticalRectF.set(pspCX - hSizeX, centerY - hSizeY, pspCX + hSizeX, centerY + hSizeY)
                    }else{
                        verticalRectF.set(xPos - hSizeX, centerY - hSizeY, xPos + hSizeX, centerY + hSizeY)
                    }
                    if(item.hasIcon){
                        val iconAnchorX = (isPSP).select(1.0f, 0.5f)
                        if(item.hasAnimatedIcon && item.isAnimatedIconLoaded){
                            ctx.drawBitmap(item.animatedIcon.getFrame(time.deltaTime), null, verticalRectF, iconPaint, FittingMode.FIT, iconAnchorX, 0.5f)
                        }else{
                            // ctx.drawRect(verticalRectF, menuVerticalDescPaint)
                            if(item.isIconLoaded){
                                ctx.drawBitmap(item.icon, null, verticalRectF, iconPaint, FittingMode.FIT, iconAnchorX, 0.5f)
                            }else{
                                if(loadIcon != null){
                                    ctx.withRotation(((time.currentTime + (wx * 0.375f)) * -360.0f) % 360.0f, verticalRectF.centerX(), verticalRectF.centerY()){
                                        ctx.drawBitmap(loadIcon, null, verticalRectF, iconPaint, FittingMode.FIT, iconAnchorX, 0.5f)
                                    }
                                }
                            }
                        }
                    }

                    if(selected){
                        if(item.hasBackSound && item.isBackSoundLoaded){
                            context.vsh.setAudioSource(item.backSound)
                        }else{
                            context.vsh.removeAudioSource()
                        }
                    }

                    if(item.hasDescription){
                        if(isPSP){
                            menuVerticalNamePaint.textSize = 35.0f
                            menuVerticalDescPaint.textSize = 25.0f
                        }else{
                            menuVerticalNamePaint.textSize = 30.0f
                            menuVerticalDescPaint.textSize = 20.0f
                        }
                        ctx.drawText(item.displayName, xPos + iconCenterToText, centerY, menuVerticalNamePaint, -0.25f)
                        if(item.hasValue){
                            menuVerticalDescPaint.textAlign = Paint.Align.RIGHT
                            ctx.drawText(item.value, scaling.target.right - 30f, centerY, menuVerticalDescPaint, -0.25f)
                            menuVerticalDescPaint.textAlign = Paint.Align.LEFT
                        }
                        ctx.drawText(item.description, xPos + iconCenterToText, centerY, menuVerticalDescPaint, 1.25f)
                        if(isPSP){
                            statusOutlinePaint.strokeWidth = 2.0f
                            statusOutlinePaint.color = Color.WHITE
                            ctx.drawLine(
                                xPos + iconCenterToText, centerY,
                                scaling.viewport.right - 20.0f, centerY,
                                statusOutlinePaint
                                )
                        }
                    }else{
                        ctx.drawText(item.displayName, xPos + iconCenterToText, centerY, menuVerticalNamePaint, 0.5f)
                        if(item.hasValue){
                            menuVerticalDescPaint.textAlign = Paint.Align.RIGHT
                            ctx.drawText(item.value, scaling.target.right - 30f, centerY, menuVerticalDescPaint, 0.5f)
                            menuVerticalDescPaint.textAlign = Paint.Align.LEFT
                        }
                    }
                }
            }
        }
    }
}

fun VshView.menuRenderHorizontalMenu(ctx:Canvas){
    menu3HorizontalMenu(ctx)
}

fun VshView.menuRender(ctx: Canvas){
    state.menu.currentTime += time.deltaTime
    state.menu.menuScaleTime = (time.deltaTime * 10.0f).toLerp(state.menu.menuScaleTime, 0.0f)
    val menuScale = state.menu.menuScaleTime.toLerp(1.0f, 2.0f)
    menuDrawBackground(ctx)
    ctx.withScale(menuScale, menuScale, scaling.target.centerX(), scaling.target.centerY()){
        try{
            menuRenderVerticalMenu(ctx)
            menuRenderHorizontalMenu(ctx)
            menuRenderStatusBar(ctx)
        }catch(cme:ConcurrentModificationException){}
    }
}

fun VshView.menuEnd(){

}