package id.psw.vshlauncher.views

import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.os.BatteryManager
import android.view.MotionEvent
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.contains
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import id.psw.vshlauncher.*
import id.psw.vshlauncher.activities.XMB
import id.psw.vshlauncher.livewallpaper.NativeGL
import id.psw.vshlauncher.livewallpaper.XMBWaveRenderer
import id.psw.vshlauncher.livewallpaper.XMBWaveSurfaceView
import id.psw.vshlauncher.submodules.GamepadSubmodule
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.types.items.XMBItemCategory
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.typography.parseEncapsulatedBracket
import id.psw.vshlauncher.views.nativedlg.NativeEditTextDialog
import junit.runner.Version.id
import java.text.SimpleDateFormat
import java.util.*
import kotlin.ConcurrentModificationException
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

enum class DirectionLock {
    None,
    Vertical,
    Horizontal
}



class VshViewMainMenuState {
    var sortHeaderDisplay: Float = 0.0f
    var currentTime : Float = 0.0f
    var layoutMode : XMBLayoutType = XMBLayoutType.PS3
    lateinit var arrowBitmap : Bitmap
    var arrowBitmapLoaded = false
    var bgOverlayColorA : Int = 0x88FF0000u.toInt()
    var bgOverlayColorB : Int = 0x8800FF00u.toInt()
    var isFocused : Boolean = false
    var clockLoadTransition : Float = 1.0f
    var menuScaleTime : Float = 0.0f
    var loadingIconBitmap : Bitmap? = null
    var playVideoIcon = true
    var coldBootTransition = 0.0f
    var dimOpacity = 0

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

    class Formatter{
        var shortMonthName : SimpleDateFormat = SimpleDateFormat("MMM", Locale.getDefault())
        var fullMonthName : SimpleDateFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        var shortDayName : SimpleDateFormat = SimpleDateFormat("EEE", Locale.getDefault())
        var fullDayName : SimpleDateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
    }

    data class SearchQuery(
        var searchIcon : Bitmap? = null
    )

    // val dateTimeFormat = "dd/M HH:mm a"
    var dateTimeFormat = "{operator} {sdf:dd/M HH:mm a}"
    var formatter = Formatter()
    val statusBar = StatusBarSetting()
    val verticalMenu = VerticalMenu()
    val searchQuery = SearchQuery()
    var directionLock : DirectionLock = DirectionLock.None
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
        textSize = 10f
        typeface = FontCollections.masterFont
    }
    val iconPaint: Paint= Paint(Paint.ANTI_ALIAS_FLAG).apply {

    }
    val menuVerticalNamePaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.LEFT
        textSize = 20.0f
        typeface = FontCollections.masterFont
    }
    val menuVerticalDescPaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.LEFT
        textSize = 10.0f
        typeface = FontCollections.masterFont
    }

    val menuHorizontalNamePaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
        textSize = 15.0f
        textAlign = Paint.Align.CENTER
        typeface = FontCollections.masterFont
    }
    val menuHorizontalIconPaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        alpha = 255
    }
}

fun XmbView.menuStart(){
    state.crossMenu.currentTime = 0.0f
    state.crossMenu.menuScaleTime = 1.0f

    if(!state.crossMenu.arrowBitmapLoaded){
        state.crossMenu.arrowBitmap =
            ResourcesCompat.getDrawable(resources, R.drawable.miptex_arrow, null)
                ?.toBitmap(128,128)
                ?: XMBItem.TRANSPARENT_BITMAP
        state.crossMenu.arrowBitmapLoaded = true
    }

    if(state.crossMenu.loadingIconBitmap == null){
        state.crossMenu.loadingIconBitmap = ResourcesCompat.getDrawable(context.resources,R.drawable.ic_sync_loading,null)?.toBitmap(256,256)
    }
}

private var baseDefRect = RectF()
private var tmpRectF = RectF()
private var tmpPointFA = PointF()
private var tmpPointFB = PointF()

fun XmbView.drawClock(ctx:Canvas, calendar:Calendar, top:Float){
    with(state.crossMenu){
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
        if(statusBar.secondOnAnalog){
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

private fun Int.leadZero(count:Int = 2) : String{
    return this.toString().padStart(count, '0')
}

/**
 * Encapsulated text format
 * Values:
 * - `operator` - Network Name
 * - `sdf:(sdf_format)` - Simple Date Format, for reference see [SimpleDateFormat](https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html)
 */
fun XmbView.formatStatusBar(src:String) : String {
    val sb = StringBuilder()
    val cl = Calendar.getInstance()
    src.parseEncapsulatedBracket().forEachIndexed { i, s ->
        if(i % 2 == 0){
            sb.append(s)
        }else{
            sb.append(when(s){
                // Operator
                "operator" -> context.vsh.network.operatorName
                "battery" -> {
                    (context.vsh.getBatteryLevel() * 100).toInt().toString()
                }
                "charging" -> {
                    context.vsh.isBatteryCharging().select("⚡","")
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
                            "%f".format(context.vsh.getBatteryLevel() * 100)
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

fun XmbView.menu3StatusBar(ctx:Canvas){
    with(state.crossMenu){
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
        statusFillPaint.setColorAndSize(FColor.argb(0.25f, 0.0f,0f,0f), 25.0f, Paint.Align.RIGHT)
        ctx.drawRoundRect(baseDefRect, 10.0f, statusFillPaint)
        ctx.drawRoundRect(baseDefRect, 10.0f, statusOutlinePaint)

        statusTextPaint.setColorAndSize(Color.WHITE, 20.0f, Paint.Align.RIGHT)

        val calendar = Calendar.getInstance()

        val status = StringBuilder()

        status.append(formatStatusBar(dateTimeFormat))

        ctx.drawText(
            status.toString()
            , scaling.target.right - statusBar.showAnalogClock.select(120.0f, 70.0f), top, statusTextPaint, 0.5f)

        if(statusBar.showAnalogClock){
            drawClock(ctx, calendar, top)
        }
    }
}

private val tmpPath = Path()

fun XmbView.menuPStatusBar(ctx:Canvas){
    with(state.crossMenu){
        val topBar = statusBar.padPSPStatusBar.select(48f, 10f)

        statusTextPaint.setShadowLayer(10.0f, 2.0f, 2.0f, Color.BLACK)
        statusTextPaint.setColorAndSize(Color.WHITE, 40.0f, Paint.Align.RIGHT)
        val statusText = StringBuilder()
        statusText.append(formatStatusBar(dateTimeFormat))

        ctx.drawText(statusText.toString(), scaling.target.right - 90f,  topBar, statusTextPaint, 1.0f)

        // region Battery Icon
        // Draw Battery Rect
        if(statusBar.showBattery){
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
            statusTextPaint.removeShadowLayer()
        }
        // endregion
    }
}

fun XmbView.menuRenderSearchQuery(ctx:Canvas){
    with(state.crossMenu){
        if(statusBar.disabled) return
        when(layoutMode){
            XMBLayoutType.PS3 -> menu3SearchQuery(ctx)
            XMBLayoutType.PSP -> menuPSearchQuery(ctx)
            else -> menuPSearchQuery(ctx)
        }
    }
}

fun XmbView.menu3SearchQuery(ctx:Canvas){
    val vsh = this.context.vsh
    with(state.crossMenu){
        val cat = vsh.activeParent
        if(cat != null){
            val y = scaling.target.top + (scaling.target.height() * 0.1f)
            val x = scaling.target.left + 50.0f
            val q = cat.getProperty(Consts.XMB_ACTIVE_SEARCH_QUERY, "")
            if(q.isNotEmpty()){
                if(searchQuery.searchIcon == null){
                    searchQuery.searchIcon = vsh.loadTexture(R.drawable.ic_search, 32, 32, true)
                }
                val icon = searchQuery.searchIcon ?: XMBItem.WHITE_BITMAP
                val hSize = 40.0f / 2.0f

                ctx.drawBitmap(icon,
                    null,
                    RectF(x, y - hSize, x + 40.0f, y + hSize),
                    iconPaint
                )
                val align = statusTextPaint.textAlign
                statusTextPaint.textAlign = Paint.Align.LEFT
                ctx.drawText(q, x + 50.0f, y, statusTextPaint, 0.5f)
                statusTextPaint.textAlign = align
            }
        }
    }
}

fun XmbView.menuPSearchQuery(ctx:Canvas){
    val vsh = this.context.vsh
    with(state.crossMenu){
        val cat = vsh.activeParent
        if(cat != null){
            val y = scaling.target.top + statusBar.padPSPStatusBar.select(48.0f, 10.0f)
            val x = scaling.target.left + 10.0f
            val q = cat.getProperty(Consts.XMB_ACTIVE_SEARCH_QUERY, "")
            if(q.isNotEmpty()){
                if(searchQuery.searchIcon == null){
                    searchQuery.searchIcon = vsh.loadTexture(R.drawable.ic_search, 32, 32, true)
                }
                val icon = searchQuery.searchIcon ?: XMBItem.WHITE_BITMAP

                ctx.drawBitmap(icon,
                    null,
                    RectF(x, y, x + 30.0f, y + 30.0f),
                    iconPaint
                )
                val align = statusTextPaint.textAlign
                statusTextPaint.textAlign = Paint.Align.LEFT
                ctx.drawText(q, x + 35.0f, top + 15.0f, statusTextPaint, 0.5f)
                statusTextPaint.textAlign = align
            }
        }
    }
}


fun XmbView.menuXStatusBar(ctx:Canvas){

}

fun XmbView.menuRenderStatusBar(ctx:Canvas){
    with(state.crossMenu){
        if(statusBar.disabled) return
        when(layoutMode){
            XMBLayoutType.PS3 -> menu3StatusBar(ctx)
            XMBLayoutType.PSP -> menuPStatusBar(ctx)
            else -> menuXStatusBar(ctx)
        }
    }
}

fun XmbView.menuDrawBackground(ctx:Canvas) {
    with(state.crossMenu){
        // statusFillPaint.color = FColor.setAlpha(Color.BLACK, 0.25f)
        // ctx.drawRect(scaling.target, statusFillPaint)
        if(verticalMenu.showBackdrop){
            try{
                val activeItem = context.vsh.items?.visibleItems?.find{it.id == context.vsh.selectedItemId}
                if(activeItem != null){
                    context.vsh.itemBackdropAlphaTime =context.vsh.itemBackdropAlphaTime.coerceIn(0f, 1f)
                    backgroundPaint.alpha = (context.vsh.itemBackdropAlphaTime * 255).roundToInt().coerceIn(0, 255)
                    val opa = dimOpacity / 10.0f
                    if((scaling.screen.height() > scaling.screen.width()) && activeItem.hasPortraitBackdrop){
                        if(activeItem.isPortraitBackdropLoaded){
                            ctx.drawBitmap(
                                activeItem.portraitBackdrop,
                                null,
                                scaling.viewport,
                                backgroundPaint,
                                FittingMode.FILL, 0.5f, 0.5f)
                            ctx.drawARGB((context.vsh.itemBackdropAlphaTime * opa * 255).toInt(), 0,0,0)
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
                            ctx.drawARGB((context.vsh.itemBackdropAlphaTime * opa * 255).toInt(), 0,0,0)
                            if(context.vsh.itemBackdropAlphaTime < 1.0f) context.vsh.itemBackdropAlphaTime += (time.deltaTime) * 2.0f
                        }
                    }
                }
            }catch(cme:ConcurrentModificationException){

            }
        }

        val focusAlpha = state.itemMenu.showMenuDisplayFactor.toLerp(0f, 128f).toInt()
        ctx.drawARGB(focusAlpha, 0,0,0)

    }
}

private val ps3MenuIconCenter = PointF(0.30f, 0.25f)
private val pspMenuIconCenter = PointF(0.20f, 0.25f)
private val ps3SelectedCategoryIconSize = PointF(75.0f, 75.0f)
private val ps3UnselectedCategoryIconSize = PointF(60.0f, 60.0f)
private val pspSelectedCategoryIconSize = PointF(150.0f, 150.0f)
private val pspUnselectedCategoryIconSize = PointF(100.0f, 100.0f)

// PS3 Base Icon Aspect Ratio = 20:11 (320x176)
private val ps3SelectedIconSize = PointF(200.0f, 110.0f)
private val ps3UnselectedIconSize = PointF(120.0f, 66.0f)

// PSP Base Icon Aspect Ratio = 18:10 (144x80)
private val pspSelectedIconSize = PointF(270.0f, 150.0f)
private val pspUnselectedIconSize = PointF(180.0f, 100.0f)

private val pspIconSeparation = PointF(200.0f, 105.0f)
private val ps3IconSeparation = PointF(150.0f, 70.0f)
private val horizontalRectF = RectF()

fun XmbView.menu3HorizontalMenu(ctx:Canvas){
    with(state.crossMenu){
        val isPSP = layoutMode == XMBLayoutType.PSP
        val center = isPSP.select(pspMenuIconCenter, ps3MenuIconCenter)
        val xPos = (scaling.target.width() * center.x) + context.vsh.isInRoot.select(0f, isPSP.select(
            pspSelectedIconSize, ps3SelectedIconSize).x * -0.75f)
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

                val radius = abs((kotlin.math.sin(currentTime / 3.0f)) * 10f)
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
                menuHorizontalIconPaint.alpha = selected.select(255, context.vsh.isInRoot.select(200, 0))
                ctx.drawBitmap(item.icon, null, horizontalRectF, menuHorizontalIconPaint, FittingMode.FIT, 0.5f, 1.0f)
                if(selected && context.vsh.isInRoot){
                    val iconCtrToText = (layoutMode == XMBLayoutType.PSP).select(pspSelectedCategoryIconSize, ps3SelectedCategoryIconSize).y
                    menuHorizontalNamePaint.textSize = (layoutMode == XMBLayoutType.PSP).select(25f, 15f)
                    ctx.drawText(item.displayName, centerX, yPos + (iconCtrToText / 2.0f), menuHorizontalNamePaint, 1.0f)
                }
            }
        }
    }
}

private val verticalRectF = RectF()

fun XmbView.menuRenderVerticalMenu(ctx:Canvas){
    val items = context.vsh.items?.visibleItems?.filterBySearch(context.vsh)

    val loadIcon = state.crossMenu.loadingIconBitmap
    val isPSP = state.crossMenu.layoutMode == XMBLayoutType.PSP
    val menuDispT = state.itemMenu.showMenuDisplayFactor
    val isLand = width > height
    with(state.crossMenu){

        val hSeparation = (isPSP).select(pspIconSeparation, ps3IconSeparation).x
        val separation = (isPSP).select(pspIconSeparation, ps3IconSeparation).y
        val center = (isPSP).select(pspMenuIconCenter,ps3MenuIconCenter)
        val xPos =
            ((scaling.target.width() * center.x) + (context.vsh.itemOffsetX * hSeparation))
        val yPos = (scaling.target.height() * center.y) + (separation * 2.0f)
        val iconCenterToText = (isPSP).select(pspSelectedIconSize, ps3SelectedIconSize).x * menuDispT.toLerp(0.60f, 0.75f)
        val cursorY = context.vsh.itemCursorY

        if(!context.vsh.isInRoot){
            val vsh = context.vsh
            val rootItem = vsh.categories.visibleItems.find { it.id == vsh.selectedCategoryId }
            var i = 0
            val iconSize = (isPSP).select(pspUnselectedIconSize, ps3UnselectedIconSize)
            val hSizeY = iconSize.y / 2.0f
            val hSizeX = iconSize.x / 2.0f

            iconPaint.alpha = 255

            val dxOffArr = xPos - (ps3IconSeparation.x * 0.625f)
            ctx.withTranslation(dxOffArr, yPos){
                ctx.withRotation(180f){
                    tmpRectF.set(-16f, -16f, 16f, 16f)
                    ctx.drawBitmap(arrowBitmap, null, tmpRectF, iconPaint, FittingMode.FIT, 0.5f, 0.5f)
                }
            }

            iconPaint.alpha = 192

            try {
                if (i < vsh.selectStack.size) {
                    var sItem = rootItem?.content?.find { it.id == vsh.selectStack[i] }
                    while (sItem != null && i < vsh.selectStack.size) {
                        val dxOff = xPos - ((ps3IconSeparation.x * 1.25f) * (i + 1))
                        tmpRectF.set(dxOff - hSizeX, yPos - hSizeY, dxOff + hSizeY, yPos + hSizeY)
                        if (sItem.hasIcon) {
                            ctx.drawBitmap(
                                sItem.icon,
                                null,
                                tmpRectF,
                                iconPaint,
                                FittingMode.FIT,
                                0.5f
                            )
                        }
                        sItem = sItem.content?.find { it.id == vsh.selectStack[i] }
                        i++
                    }
                }
            }catch(e:IndexOutOfBoundsException){}
        }

        if(items != null){
            if(items.isNotEmpty()) {
                for (wx in items.indices) {
                    val iy = wx - cursorY
                    val selected = iy == 0
                    val item = items[wx]

                    val textAlpha = selected.select(255, menuDispT.toLerp(128f, 0f).toInt())
                    menuVerticalDescPaint.alpha = textAlpha
                    menuVerticalNamePaint.alpha = textAlpha

                    if (selected) {
                        val radius = abs((kotlin.math.sin(currentTime / 3.0f)) * 10f)
                        menuVerticalNamePaint.setShadowLayer(radius, 0f, 0f, Color.WHITE)
                        menuVerticalDescPaint.setShadowLayer(radius, 0f, 0f, Color.WHITE)
                        iconPaint.setShadowLayer(radius, 0f, 0f, Color.WHITE)
                    } else {
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

                    if (selected) {
                        val sizeTransition = abs(context.vsh.itemOffsetY)
                        val previousSize =
                            isPSP.select(
                                selected.select(pspUnselectedIconSize, pspSelectedIconSize),
                                selected.select(ps3UnselectedIconSize, ps3SelectedIconSize)
                            )
                        tmpPointFA.x = sizeTransition.toLerp(targetSize.x, previousSize.x)
                        tmpPointFA.y = sizeTransition.toLerp(targetSize.y, previousSize.y)
                        size = tmpPointFA
                    }

                    var hSizeX = size.x / 2.0f
                    var hSizeY = (size.y / 2.0f)
                    var centerY = yPos + ((iy + context.vsh.itemOffsetY) * separation)

                    iconPaint.alpha = 255
                    if(!selected){
                        hSizeX *= menuDispT.toLerp(1.0f, 0.75f)
                        hSizeY *= menuDispT.toLerp(1.0f, 0.75f)
                        iconPaint.alpha = (menuDispT.toLerp(255f, 128f)).toInt()
                    }else{
                        hSizeX *= menuDispT.toLerp(1.0f, 1.25f)
                        hSizeY *= menuDispT.toLerp(1.0f, 1.25f)
                    }

                    if (iy < 0) centerY -= size.y * context.vsh.isInRoot.select(3.0f, 0.5f)
                    if (iy > 0) centerY += size.y * 0.5f

                    val isInViewport =
                        (centerY + hSizeY) > scaling.viewport.top && (centerY - hSizeY) < scaling.viewport.bottom

                    item.screenVisibility = isInViewport

                    if (isInViewport) {
                        verticalRectF.set(xPos - hSizeX, centerY - hSizeY, xPos + hSizeX, centerY + hSizeY)
                        if (item.hasIcon) {
                            val iconAnchorX = (isPSP).select(0.5f, 0.5f)
                            if (item.hasAnimatedIcon && item.isAnimatedIconLoaded && playVideoIcon) {
                                val animIconBm = item.animatedIcon.getFrame(time.deltaTime)
                                ctx.drawBitmap(animIconBm, null, verticalRectF, iconPaint, FittingMode.FIT, iconAnchorX, 0.5f)
                            } else {
                                // ctx.drawRect(verticalRectF, menuVerticalDescPaint)
                                if (item.isIconLoaded) {
                                    ctx.drawBitmap(item.icon, null, verticalRectF, iconPaint, FittingMode.FIT, iconAnchorX, 0.5f)
                                } else {
                                    if (loadIcon != null) {
                                        ctx.withRotation(
                                            ((time.currentTime + (wx * 0.375f)) * -360.0f) % 360.0f, verticalRectF.centerX(), verticalRectF.centerY()) {
                                            ctx.drawBitmap(loadIcon, null, verticalRectF, iconPaint, FittingMode.FIT, iconAnchorX, 0.5f)
                                        }
                                    }
                                }
                            }
                        }

                        if (selected) {
                            if (item.hasBackSound && item.isBackSoundLoaded) context.vsh.setAudioSource(item.backSound)
                            else context.vsh.removeAudioSource()
                        }

                        val textLeft = verticalRectF.centerX() + (iconCenterToText)

                        if (isPSP) {
                            menuVerticalNamePaint.textSize = 35.0f
                            menuVerticalDescPaint.textSize = 25.0f
                        } else {
                            menuVerticalNamePaint.textSize = 25.0f
                            menuVerticalDescPaint.textSize = 15.0f
                        }

                        var dispNameYOffset = 0.5f

                        if(isLand){
                            if(item.hasDescription){
                                ctx.drawText(item.description, textLeft, centerY, menuVerticalDescPaint, 1.1f)
                                dispNameYOffset= -0.25f
                            }
                            if(item.hasValue && !state.itemMenu.isDisplayed){
                                ctx.drawText(item.value, scaling.viewport.right - 400.0f, centerY, menuVerticalDescPaint, -0.25f)
                            }
                        }else{
                            var itemDesc = ""
                            var hasBottomText = false
                            if(item.hasValue){
                                itemDesc = item.value
                                hasBottomText = itemDesc.isNotBlank()
                            }else if(item.hasDescription){
                                itemDesc = item.description
                                hasBottomText = itemDesc.isNotBlank()
                            }

                            if(hasBottomText){
                                dispNameYOffset= -0.25f
                                ctx.drawText(itemDesc, textLeft, centerY, menuVerticalDescPaint, 1.1f)
                            }
                        }
                        ctx.drawText(item.displayName, textLeft, centerY, menuVerticalNamePaint, dispNameYOffset)

                        if (isPSP && item.hasDescription) {
                            statusOutlinePaint.strokeWidth = 2.0f
                            statusOutlinePaint.color = Color.WHITE
                            statusOutlinePaint.alpha = selected.select(255, menuDispT.toLerp(255f,0f).toInt())
                            ctx.drawLine(textLeft, centerY,(scaling.viewport.right - 20.0f), centerY, statusOutlinePaint)
                        }
                    }
                }
            } else {
                val eString = context.getString(R.string.category_is_empty)
                val xxPos = xPos + (ps3SelectedIconSize.x / 2.0f)
                menuVerticalNamePaint.color = Color.WHITE
                ctx.drawText(eString, xxPos, yPos, menuVerticalNamePaint)
                context.vsh.removeAudioSource()
            }
        }
    }
}

fun List<XMBItem>.filterBySearch(vsh: VSH): List<XMBItem> {
    val q = vsh.activeParent?.getProperty(Consts.XMB_ACTIVE_SEARCH_QUERY, "") ?: ""
    return this.filter { it.displayName.contains(q, true) }
}

fun XmbView.menuRenderHorizontalMenu(ctx:Canvas){
    menu3HorizontalMenu(ctx)
}

private val touchTestRectF = RectF()
private val touchTestSearchRectF = RectF()

fun XmbView.menuOnTouchScreen(a:PointF, b:PointF, act:Int){
    with(state.crossMenu){
        if(state.itemMenu.isDisplayed){
            when(act){
                MotionEvent.ACTION_DOWN ->{
                    run { // Is Up
                        if(b.x < scaling.target.right - 400.0f){
                            state.itemMenu.isDisplayed = false
                        }else{
                            if(b.y < 200.0f){
                                menuMoveItemMenuCursor(false)
                            }else if(b.y > scaling.target.bottom - 200.0f){
                                menuMoveItemMenuCursor(true)
                            }else{
                                menuStartItemMenu()
                            }
                        }
                    }
                }
            }
        }else {
            val isPSP = layoutMode == XMBLayoutType.PSP


            when (act) {
                MotionEvent.ACTION_UP -> {

                    run { // Run Active Icon
                        if(directionLock == DirectionLock.None){
                            val iconSize = isPSP.select(pspSelectedIconSize, ps3SelectedIconSize)
                            val hSeparation = (isPSP).select(pspIconSeparation, ps3IconSeparation).x
                            val separation = (isPSP).select(pspIconSeparation, ps3IconSeparation).y
                            val center = (isPSP).select(pspMenuIconCenter,ps3MenuIconCenter)
                            val xPos =
                                ((scaling.target.width() * center.x) + (context.vsh.itemOffsetX * hSeparation))
                            val yPos = (scaling.target.height() * center.y) + (separation * 2.0f)
                            val hSizeX = iconSize.x * 0.5f
                            val hSizeY = iconSize.y * 0.5f
                            touchTestRectF.set(
                                xPos - hSizeX,
                                yPos - hSizeY,
                                xPos + hSizeX,
                                yPos + hSizeY
                            )

                            if(context.vsh.isInRoot){
                                touchTestSearchRectF.set(
                                    xPos - hSizeX,
                                    yPos - hSizeY - iconSize.y,
                                    xPos + hSizeX,
                                    yPos - hSizeY,
                                )
                            }else{
                                touchTestSearchRectF.set(
                                    xPos - hSizeX - iconSize.x,
                                    yPos - hSizeY - iconSize.y,
                                    xPos - hSizeX,
                                    yPos - hSizeY,
                                )

                            }

                            if (touchTestRectF.contains(a)) {
                                context.vsh.launchActiveItem()
                            }else if(touchTestSearchRectF.contains(a)){
                                menuOpenSearchQuery()
                            }
                            else if(b.x < 200.0f){
                                context.vsh.backStep()
                            }
                        }
                    }

                    directionLock = DirectionLock.None
                }
                MotionEvent.ACTION_MOVE -> {
                    val isMenu = a.x > scaling.target.right - 200.0f
                    run{
                        if(isMenu){
                            if(b.x <  scaling.target.right - 400.0f){
                                val item = context.vsh.hoveredItem
                                if(item?.hasMenu == true){
                                    state.itemMenu.isDisplayed = true
                                }
                            }
                        }
                    }
                    if(!isMenu) {
                        run { //
                            val iconSize =
                                (isPSP).select(pspUnselectedIconSize, ps3UnselectedIconSize)
                            if (directionLock != DirectionLock.Horizontal) { // Vertical
                                val yLen = b.y - a.y
                                if (abs(yLen) > iconSize.y) {
                                    context.vsh.moveCursorY(yLen < 0.0f)
                                    context.xmb.touchStartPointF.set(b)
                                    directionLock = DirectionLock.Vertical
                                }
                            }

                            if (directionLock != DirectionLock.Vertical) { // Horizontal
                                val xLen = b.x - a.x
                                if (abs(xLen) > iconSize.x) {
                                    if (context.vsh.isInRoot) {
                                        context.vsh.moveCursorX(xLen < 0.0f)
                                    } else {
                                        if (xLen > 0.0f) {
                                            context.vsh.backStep()
                                        }
                                    }
                                    context.xmb.touchStartPointF.set(b)
                                    directionLock = DirectionLock.Horizontal
                                }
                            }
                        }
                    }


                }
            }
        }
    }
}

fun XmbView.menuRenderSortHeaderDisplay(ctx: Canvas) {
    with(state.crossMenu){
        if(sortHeaderDisplay > 0.0f){
            val vsh = context.vsh
            val selCat = vsh.categories.visibleItems.find {it.id == vsh.selectedCategoryId}
            if(selCat is XMBItemCategory){
                if(selCat.sortable){
                    val t = (
                            if(sortHeaderDisplay > 3.0f) sortHeaderDisplay.lerpFactor(5.0f, 4.75f)
                            else sortHeaderDisplay.lerpFactor(0.0f, 0.25f)
                            ).coerceIn(0.0f, 1.0f)
                    val textName = selCat.sortModeName
                    val scale = t.toLerp(2.0f, 1.0f)
                    ctx.withScale(scale, scale,  scaling.target.width() / 2.0f, scaling.target.height() / 2.0f){
                        statusFillPaint.alpha = (t * t).toLerp(0.0f,128.0f).toInt()
                        statusOutlinePaint.alpha = (t * t).toLerp(0.0f,255.0f).toInt()
                        statusTextPaint.alpha = (t * t).toLerp(0.0f,255.0f).toInt()
                        statusTextPaint.textAlign = Paint.Align.LEFT

                        tmpRectF.set(scaling.viewport.left - 10.0f, scaling.viewport.top - 10.0f,
                            scaling.viewport.right + 10.0f, 150.0f)
                        ctx.drawRect(tmpRectF, statusFillPaint)
                        ctx.drawRect(tmpRectF, statusOutlinePaint)

                        ctx.drawText(textName, 75f, tmpRectF.bottom - 50.0f, statusTextPaint, 0.5f)
                    }
                }
            }
            sortHeaderDisplay -= time.deltaTime
        }
    }
}

fun XmbView.updateColdBootWaveAnimation(){
    val speed = context.vsh.pref.getFloat(XMBWaveSurfaceView.KEY_SPEED, 1.0f)
    NativeGL.setSpeed( state.crossMenu.coldBootTransition.toLerp(speed, 25.0f) )
    NativeGL.setVerticalScale( state.crossMenu.coldBootTransition.toLerp(1.0f, 1.25f) )

    state.crossMenu.coldBootTransition -= time.deltaTime * 2.0f
    if(state.crossMenu.coldBootTransition < 0.0f){
        context.vsh.waveShouldReReadPreferences = true
        NativeGL.setVerticalScale( 1.0f )
        NativeGL.setSpeed( 1.0f )
    }
}

fun XmbView.menuRender(ctx: Canvas){
    state.crossMenu.currentTime += time.deltaTime
    state.crossMenu.menuScaleTime = (time.deltaTime * 10.0f).toLerp(state.crossMenu.menuScaleTime, 0.0f).coerceIn(0f,1f)
    val menuScale = state.crossMenu.menuScaleTime.toLerp(1.0f, 2.0f).coerceIn(1.0f, 2.0f)

    if(state.crossMenu.coldBootTransition > 0.0f){
        updateColdBootWaveAnimation()
    }

    menuDrawBackground(ctx)
    ctx.withScale(menuScale, menuScale, scaling.target.centerX(), scaling.target.centerY()){
        try{
            menuRenderVerticalMenu(ctx)
            menuRenderHorizontalMenu(ctx)
            menuRenderSearchQuery(ctx)
            menuRenderStatusBar(ctx)
            menuRenderItemMenu(ctx)
            menuRenderSortHeaderDisplay(ctx)
        }catch(cme:ConcurrentModificationException){}
    }
}

fun XmbView.menuEnd(){

}

fun XmbView.menuOpenSearchQuery(){
    val item = context.vsh.activeParent
    if(item != null){
        val q = item.getProperty(Consts.XMB_ACTIVE_SEARCH_QUERY, "")
        NativeEditTextDialog(context.vsh)
            .setOnFinish {  item.setProperty(Consts.XMB_ACTIVE_SEARCH_QUERY, it) }
            .setTitle(context.getString(R.string.dlg_set_search_query))
            .setValue(q)
            .show()
    }
}

fun XmbView.menuOnGamepad(key: GamepadSubmodule.Key, isPressing: Boolean) : Boolean {
    var retval = false
    val vsh = context.vsh
    val inMenu = state.itemMenu.isDisplayed

    if(isPressing){
        when(key){
            GamepadSubmodule.Key.PadL -> {
                if(!inMenu){
                    if(vsh.isInRoot){
                        vsh.moveCursorX(false)
                        vsh.playSfx(SFXType.Selection)
                    }else{
                        vsh.backStep()
                        vsh.playSfx(SFXType.Cancel)
                    }
                    retval = true
                }
            }
            GamepadSubmodule.Key.PadR -> {
                if(vsh.isInRoot){
                    if(!inMenu){
                        context.vsh.moveCursorX(true)
                    }
                    retval = true
                }
            }
            GamepadSubmodule.Key.PadU -> {
                if(inMenu){
                    menuMoveItemMenuCursor(false)
                }else{
                    context.vsh.moveCursorY(false)
                }
                retval = true
            }
            GamepadSubmodule.Key.PadD -> {
                if(inMenu){
                    menuMoveItemMenuCursor(true)
                }else{
                    context.vsh.moveCursorY(true)
                }
                retval = true
            }
            GamepadSubmodule.Key.Triangle -> {
                val item = vsh.hoveredItem
                if(inMenu){
                    state.itemMenu.isDisplayed = false
                }else{
                    if(item != null){
                        if(item.hasMenu){
                            state.itemMenu.isDisplayed = true
                        }
                    }
                }
                retval = true
            }
            GamepadSubmodule.Key.Select -> {
                menuOpenSearchQuery()
            }
            GamepadSubmodule.Key.Square -> {
                if(vsh.isInRoot){
                    vsh.doCategorySorting()
                    state.crossMenu.sortHeaderDisplay = 5.0f
                    retval = true
                }
            }
            GamepadSubmodule.Key.Confirm, GamepadSubmodule.Key.StaticConfirm -> {
                if(inMenu) {
                    menuStartItemMenu()
                    state.itemMenu.isDisplayed = false
                }else{
                    vsh.launchActiveItem()
                }
                retval = true
            }
            GamepadSubmodule.Key.Cancel, GamepadSubmodule.Key.StaticCancel -> {
                if(inMenu){
                    state.itemMenu.isDisplayed = false
                }else{
                    vsh.backStep()
                }
                retval = true
            }
            else -> {  }
        }
    }
    return retval
}