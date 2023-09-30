package id.psw.vshlauncher.views.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.contains
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import id.psw.vshlauncher.Consts
import id.psw.vshlauncher.FColor
import id.psw.vshlauncher.FittingMode
import id.psw.vshlauncher.PrefEntry
import id.psw.vshlauncher.R
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.lerpFactor
import id.psw.vshlauncher.livewallpaper.NativeGL
import id.psw.vshlauncher.livewallpaper.XMBWaveSurfaceView
import id.psw.vshlauncher.makeTextPaint
import id.psw.vshlauncher.select
import id.psw.vshlauncher.submodules.PadKey
import id.psw.vshlauncher.submodules.SfxType
import id.psw.vshlauncher.toLerp
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.types.items.XMBItemCategory
import id.psw.vshlauncher.views.DirectionLock
import id.psw.vshlauncher.views.DrawExtension
import id.psw.vshlauncher.views.XmbLayoutType
import id.psw.vshlauncher.views.XmbScreen
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.views.drawBitmap
import id.psw.vshlauncher.views.drawText
import id.psw.vshlauncher.views.filterBySearch
import id.psw.vshlauncher.views.nativedlg.NativeEditTextDialog
import id.psw.vshlauncher.visibleItems
import id.psw.vshlauncher.vsh
import id.psw.vshlauncher.xmb
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

class XmbMainMenu(view : XmbView) : XmbScreen(view)  {
    var sortHeaderDisplay: Float = 0.0f
    var layoutMode : XmbLayoutType = XmbLayoutType.PS3
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
            var showBackdrop : Boolean = true,
            var nameTextXOffset : Float = 0.0f,
            var descTextXOffset : Float = 0.0f
    )

    // val dateTimeFormat = "dd/M HH:mm a"
    val verticalMenu = VerticalMenu()
    private var directionLock : DirectionLock = DirectionLock.None
    private val backgroundPaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val statusOutlinePaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.0f
        color = Color.WHITE
    }
    private val statusFillPaint : Paint = vsh.makeTextPaint(color = FColor.setAlpha(Color.WHITE, 0.5f)).apply {
        style = Paint.Style.FILL
        strokeWidth = 3.0f
    }
    private val statusTextPaint : Paint = vsh.makeTextPaint(size = 10.0f, color = Color.WHITE).apply {
        style = Paint.Style.FILL
        strokeWidth = 3.0f
    }
    private val iconPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {}
    private val menuVerticalNamePaint : Paint = vsh.makeTextPaint(size = 20.0f, color = Color.WHITE).apply {
        textAlign = Paint.Align.LEFT
    }
    private val menuVerticalDescPaint : Paint = vsh.makeTextPaint(size = 10.0f, color = Color.WHITE).apply {
        textAlign = Paint.Align.LEFT
    }

    private val menuHorizontalNamePaint : Paint = vsh.makeTextPaint(size = 15.0f, color = Color.WHITE).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }
    private val menuHorizontalIconPaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        alpha = 255
    }
    
    private var baseDefRect = RectF()
    private var tmpRectF = RectF()
    private var tmpPointFA = PointF()
    private var tmpPointFB = PointF()
    private val tmpPath = Path()

    /** Is **Open Menu Hold** enabled. */
    var isOpenMenuOnHold = true
    var isOpenMenuHeld = false
    var isOpenMenuDisableMenuExec = true

    override fun start() {
        currentTime = 0.0f
        menuScaleTime = 1.0f

        if(!arrowBitmapLoaded){
            arrowBitmap =
                    ResourcesCompat.getDrawable(view.resources, R.drawable.miptex_arrow, null)
                            ?.toBitmap(128,128)
                            ?: XMBItem.TRANSPARENT_BITMAP
            arrowBitmapLoaded = true
        }

        if(loadingIconBitmap == null){
            loadingIconBitmap = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_sync_loading,null)?.toBitmap(256,256)
        }

        widgets.statusBar.disabled = M.pref.get(PrefEntry.DISPLAY_DISABLE_STATUS_BAR, 0) == 1
        widgets.analogClock.showSecondHand = M.pref.get(PrefEntry.DISPLAY_SHOW_CLOCK_SECOND, 0) == 1
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

    private val verticalRectF = RectF()
    private val textNameRectF = RectF()

    private fun drawBackground(ctx:Canvas){

        // statusFillPaint.color = FColor.setAlpha(Color.BLACK, 0.25f)
        // ctx.drawRect(scaling.target, statusFillPaint)
        if(verticalMenu.showBackdrop){
            try{
                val activeItem = vsh.items?.visibleItems?.find{it.id == context.vsh.selectedItemId}
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
                cme.printStackTrace()
            }
        }

        val focusAlpha = widgets.sideMenu.showMenuDisplayFactor.toLerp(0f, 128f).toInt()
        ctx.drawARGB(focusAlpha, 0,0,0)

    }

    private fun drawHorizontalMenu(ctx:Canvas) {
        val isPSP = layoutMode == XmbLayoutType.PSP
        val center = isPSP.select(pspMenuIconCenter, ps3MenuIconCenter)
        val xPos = (scaling.target.width() * center.x) + context.vsh.isInRoot.select(0f, isPSP.select(
                pspSelectedIconSize, ps3SelectedIconSize).x * -0.75f)
        val yPos = scaling.target.height() * center.y
        val notHidden = context.vsh.categories.visibleItems
        val separation = (layoutMode == XmbLayoutType.PSP).select(pspIconSeparation, ps3IconSeparation).x
        val cursorX = context.vsh.itemCursorX
        for(wx in notHidden.indices){
            val item = notHidden[wx]
            val ix = wx - cursorX
            val selected = ix == 0

            val targetSize =
                    when(layoutMode){
                        XmbLayoutType.PSP -> selected.select(pspSelectedCategoryIconSize, pspUnselectedCategoryIconSize)
                        else -> selected.select(ps3SelectedCategoryIconSize, ps3UnselectedCategoryIconSize)
                    }
            var size = targetSize

            if(selected){
                val sizeTransition = abs(context.vsh.itemOffsetX)
                val previousSize =
                        when(layoutMode){
                            XmbLayoutType.PSP -> selected.select(pspUnselectedCategoryIconSize, pspSelectedCategoryIconSize)
                            else -> selected.select(ps3UnselectedCategoryIconSize, ps3SelectedCategoryIconSize)
                        }
                tmpPointFA.x = sizeTransition.toLerp(targetSize.x, previousSize.x)
                tmpPointFA.y = sizeTransition.toLerp(targetSize.y, previousSize.y)
                size = tmpPointFA

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
                    val iconCtrToText = (layoutMode == XmbLayoutType.PSP).select(pspSelectedCategoryIconSize, ps3SelectedCategoryIconSize).y
                    menuHorizontalNamePaint.textSize = (layoutMode == XmbLayoutType.PSP).select(25f, 15f)
                    ctx.drawText(item.displayName, centerX, yPos + (iconCtrToText / 2.0f), menuHorizontalNamePaint, 1.0f)
                }
            }
        }
    }

    private fun drawVerticalMenu(ctx:Canvas){
        val items = vsh.items?.visibleItems?.filterBySearch(context.vsh)

        val loadIcon = loadingIconBitmap
        val isPSP = layoutMode == XmbLayoutType.PSP
        val menuDispT = widgets.sideMenu.showMenuDisplayFactor
        val isLand = view.width > view.height

        val hSeparation = (isPSP).select(pspIconSeparation, ps3IconSeparation).x
        val separation = (isPSP).select(pspIconSeparation, ps3IconSeparation).y
        val center = (isPSP).select(pspMenuIconCenter, ps3MenuIconCenter)
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
            }catch(_:IndexOutOfBoundsException){}
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
                        // menuVerticalNamePaint.setShadowLayer(radius, 0f, 0f, Color.WHITE)
                        // menuVerticalDescPaint.setShadowLayer(radius, 0f, 0f, Color.WHITE)
                        // iconPaint.setShadowLayer(radius, 0f, 0f, Color.WHITE)
                    } else {
                        // menuVerticalNamePaint.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
                        // menuVerticalDescPaint.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
                        // iconPaint.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
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
                            if (item.hasBackSound && item.isBackSoundLoaded) M.audio.setAudioSource(item.backSound)
                            else M.audio.removeAudioSource()
                        }

                        val textLeft = verticalRectF.centerX() + iconCenterToText

                        if (isPSP) {
                            menuVerticalNamePaint.textSize = 35.0f
                            menuVerticalDescPaint.textSize = 25.0f
                        } else {
                            menuVerticalNamePaint.textSize = 25.0f
                            menuVerticalDescPaint.textSize = 15.0f
                        }

                        var dispNameYOffset = 0.5f

                        var dNameEnd = scaling.viewport.right - 20.0f

                        if(isLand){
                            if(item.hasDescription){
                                //ctx.drawText(item.description, textLeft, centerY, menuVerticalDescPaint, 1.1f)
                                DrawExtension.scrollText(
                                        ctx,
                                        item.description,
                                        textLeft,
                                        scaling.viewport.right - 20.0f,
                                        centerY, menuVerticalDescPaint,
                                        1.1f,
                                        time.currentTime,
                                        24.0f
                                )
                                dispNameYOffset= -0.25f
                            }
                            if(item.hasValue && !widgets.sideMenu.isDisplayed){
                                DrawExtension.scrollText(
                                        ctx,
                                        item.value,
                                        scaling.viewport.right - 400.0f,
                                        scaling.viewport.right - 20.0f,
                                        centerY, menuVerticalDescPaint,
                                        -0.25f,
                                        time.currentTime,
                                        24.0f
                                )
                                dNameEnd = scaling.viewport.right - 450.0f
                                //ctx.drawText(item.value, scaling.viewport.right - 400.0f, centerY, menuVerticalDescPaint, -0.25f)
                            }
                        }else{

                            dNameEnd = scaling.viewport.right - 20.0f
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
                                DrawExtension.scrollText(
                                        ctx,
                                        itemDesc,
                                        textLeft,
                                        scaling.viewport.right - 20.0f,
                                        centerY, menuVerticalDescPaint,
                                        1.1f,
                                        time.currentTime,
                                        5.0f
                                )
                                //ctx.drawText(itemDesc, textLeft, centerY, menuVerticalDescPaint, 1.1f)
                            }
                        }
                        DrawExtension.scrollText(
                                ctx,
                                item.displayName,
                                textLeft,
                                dNameEnd,
                                centerY, menuVerticalNamePaint,
                                dispNameYOffset,
                                time.currentTime,
                                5.0f
                        )
                        //ctx.drawText(item.displayName, textLeft, centerY, menuVerticalNamePaint, dispNameYOffset)

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
                M.audio.removeAudioSource()
            }
        }
    }

    private val touchTestRectF = RectF()
    private val touchTestSearchRectF = RectF()

    override fun end() {
    }

    private fun openSearchQuery(){
        val item = vsh.activeParent
        if(item != null){
            val q = item.getProperty(Consts.XMB_ACTIVE_SEARCH_QUERY, "")
            NativeEditTextDialog(context.vsh)
                    .setOnFinish {  item.setProperty(Consts.XMB_ACTIVE_SEARCH_QUERY, it) }
                    .setTitle(context.getString(R.string.dlg_set_search_query))
                    .setValue(q)
                    .show()
        }
    }

    private fun updateColdbootWaveAnim(){
        val speed = M.pref.get(XMBWaveSurfaceView.KEY_SPEED, 1.0f)
        NativeGL.setSpeed( screens.coldBoot.transition.toLerp(speed, 25.0f) )
        NativeGL.setVerticalScale( screens.coldBoot.transition.toLerp(1.0f, 1.25f) )

        screens.coldBoot.transition -= time.deltaTime * 2.0f
        if(screens.coldBoot.transition < 0.0f){
            context.vsh.waveShouldReReadPreferences = true
            NativeGL.setVerticalScale( 1.0f )
            NativeGL.setSpeed( 1.0f )
        }
    }

    override fun onGamepadInput(key: PadKey, isDown: Boolean) : Boolean {
        var retval = false
        val vsh = context.vsh
        val inMenu = widgets.sideMenu.isDisplayed

        if(isDown){
            when(key){
                PadKey.PadL -> {
                    if(!inMenu){
                        if(vsh.isInRoot){
                            vsh.moveCursorX(false)
                            M.audio.playSfx(SfxType.Selection)
                        }else{
                            vsh.backStep()
                            M.audio.playSfx(SfxType.Cancel)
                        }
                        retval = true
                    }
                }
                PadKey.PadR -> {
                    if(vsh.isInRoot){
                        if(!inMenu){
                            context.vsh.moveCursorX(true)
                        }
                        retval = true
                    }
                }
                PadKey.PadU -> {
                    if(inMenu){
                        view.widgets.sideMenu.moveCursor(false)
                    }else{
                        context.vsh.moveCursorY(false)
                    }
                    retval = true
                }
                PadKey.PadD -> {
                    if(inMenu){
                        view.widgets.sideMenu.moveCursor(true)
                    }else{
                        context.vsh.moveCursorY(true)
                    }
                    retval = true
                }
                PadKey.Triangle -> {
                    val item = vsh.hoveredItem
                    if(inMenu){
                        widgets.sideMenu.isDisplayed = false
                    }else{
                        if(item != null){
                            if(item.hasMenu){
                                widgets.sideMenu.isDisplayed = true
                            }
                        }
                    }
                    retval = true
                }
                PadKey.Select -> {
                    openSearchQuery()
                }
                PadKey.Square -> {
                    if(vsh.isInRoot){
                        vsh.doCategorySorting()
                        sortHeaderDisplay = 5.0f
                        retval = true
                    }
                }
                PadKey.Confirm, PadKey.StaticConfirm -> {
                    if(inMenu) {
                        if(isOpenMenuDisableMenuExec){
                            view.widgets.sideMenu.executeSelected()
                            widgets.sideMenu.isDisplayed = false
                        }
                    }else{
                        // Start Timer
                        if(isOpenMenuOnHold){
                            vsh.mainHandle.postDelayed(::openMenuOnHoldAction, 500L)
                            isOpenMenuHeld = true
                            isOpenMenuDisableMenuExec = true
                        }else{
                            vsh.launchActiveItem()
                        }
                    }
                    retval = true
                }
                PadKey.Cancel, PadKey.StaticCancel -> {
                    if(inMenu){
                        widgets.sideMenu.isDisplayed = false
                    }else{
                        vsh.backStep()
                    }
                    retval = true
                }
                else -> {  }
            }
        }else{
            when(key){
                PadKey.Confirm, PadKey.StaticConfirm -> {
                    if(isOpenMenuOnHold){
                        if(isOpenMenuHeld){
                            vsh.mainHandle.removeCallbacks(::openMenuOnHoldAction)
                            vsh.launchActiveItem()
                            isOpenMenuHeld = false
                            retval = true
                        }else{
                            retval = false
                        }
                        isOpenMenuDisableMenuExec = false
                    }else{
                        retval = false
                    }
                }
                else -> {}
            }
        }
        return retval
    }

    private fun openMenuOnHoldAction(){
        val vsh = context.vsh
        val item = vsh.hoveredItem

        if(!isOpenMenuHeld) return

        if(item != null){
            if(item.hasMenu){
                widgets.sideMenu.isDisplayed = true
            }
        }
        isOpenMenuHeld = false
    }

    override fun onTouchScreen(start: PointF, current: PointF, action: Int) {
        if(widgets.sideMenu.isDisplayed){
            when(action){
                MotionEvent.ACTION_DOWN ->{
                    run { // Is Up
                        if(current.x < scaling.target.right - 400.0f){
                            widgets.sideMenu.isDisplayed = false
                        }else{
                            if(current.y < 200.0f){
                                view.widgets.sideMenu.moveCursor(false)
                            }else if(current.y > scaling.target.bottom - 200.0f){
                                view.widgets.sideMenu.moveCursor(true)
                            }else{
                                view.widgets.sideMenu.executeSelected()
                            }
                        }
                    }
                }
            }
        }else {
            val isPSP = layoutMode == XmbLayoutType.PSP


            when (action) {
                MotionEvent.ACTION_UP -> {

                    run { // Run Active Icon
                        if(directionLock == DirectionLock.None){
                            val iconSize = isPSP.select(pspSelectedIconSize, ps3SelectedIconSize)
                            val hSeparation = (isPSP).select(pspIconSeparation, ps3IconSeparation).x
                            val separation = (isPSP).select(pspIconSeparation, ps3IconSeparation).y
                            val center = (isPSP).select(pspMenuIconCenter, ps3MenuIconCenter)
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

                            if (touchTestRectF.contains(start)) {
                                context.vsh.launchActiveItem()
                            }else if(touchTestSearchRectF.contains(start)){
                                openSearchQuery()
                            }
                            else if(current.x < 200.0f){
                                context.vsh.backStep()
                            }
                        }
                    }

                    directionLock = DirectionLock.None
                }
                MotionEvent.ACTION_MOVE -> {
                    val isMenu = start.x > scaling.target.right - 200.0f
                    run{
                        if(isMenu){
                            val menuTol = (view.width > view.height).select(400.0f, 100.0f)
                            if(current.x <  scaling.target.right - menuTol){
                                val item = context.vsh.hoveredItem
                                if(item?.hasMenu == true){
                                    widgets.sideMenu.isDisplayed = true
                                }
                            }
                        }
                    }
                    if(!isMenu) {
                        run { //
                            val iconSize =
                                    (isPSP).select(pspUnselectedIconSize, ps3UnselectedIconSize)
                            if (directionLock != DirectionLock.Horizontal) { // Vertical
                                val yLen = current.y - start.y
                                if (abs(yLen) > iconSize.y) {
                                    context.vsh.moveCursorY(yLen < 0.0f)
                                    context.xmb.touchStartPointF.set(current)
                                    directionLock = DirectionLock.Vertical
                                }
                            }

                            if (directionLock != DirectionLock.Vertical) { // Horizontal
                                val xLen = current.x - start.x
                                if (abs(xLen) > iconSize.x) {
                                    if (context.vsh.isInRoot) {
                                        context.vsh.moveCursorX(xLen < 0.0f)
                                    } else {
                                        if (xLen > 0.0f) {
                                            context.vsh.backStep()
                                        }
                                    }
                                    context.xmb.touchStartPointF.set(current)
                                    directionLock = DirectionLock.Horizontal
                                }
                            }
                        }
                    }


                }
            }
        }
    }

    private fun updateColdBootWaveAnimation(){
        val speed = M.pref.get(XMBWaveSurfaceView.KEY_SPEED, 1.0f)
        NativeGL.setSpeed( coldBootTransition.toLerp(speed, 25.0f) )
        NativeGL.setVerticalScale( coldBootTransition.toLerp(1.0f, 1.25f) )

        coldBootTransition -= time.deltaTime * 2.0f
        if(coldBootTransition < 0.0f){
            context.vsh.waveShouldReReadPreferences = true
            NativeGL.setVerticalScale( 1.0f )
            NativeGL.setSpeed( 1.0f )
        }
    }

    private fun drawSortHeaderDisplay(ctx:Canvas){
        if(sortHeaderDisplay > 0.0f){
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

    override fun render(ctx: Canvas) {
        currentTime += time.deltaTime
        menuScaleTime = (time.deltaTime * 10.0f).toLerp(menuScaleTime, 0.0f).coerceIn(0f,1f)
        val menuScale = menuScaleTime.toLerp(1.0f, 2.0f).coerceIn(1.0f, 2.0f)

        if(coldBootTransition > 0.0f){
            updateColdBootWaveAnimation()
        }

        drawBackground(ctx)
        ctx.withScale(menuScale, menuScale, scaling.target.centerX(), scaling.target.centerY()){
            try{
                drawVerticalMenu(ctx)
                drawHorizontalMenu(ctx)
                widgets.searchQuery.render(ctx)
                widgets.statusBar.render(ctx)
                drawSortHeaderDisplay(ctx)
            }catch(_:ConcurrentModificationException){}
        }
    }
}