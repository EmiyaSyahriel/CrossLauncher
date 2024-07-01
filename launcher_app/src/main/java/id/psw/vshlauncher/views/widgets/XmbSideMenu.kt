package id.psw.vshlauncher.views.widgets

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import androidx.core.graphics.minus
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import id.psw.vshlauncher.FColor
import id.psw.vshlauncher.PrefEntry
import id.psw.vshlauncher.makeTextPaint
import id.psw.vshlauncher.select
import id.psw.vshlauncher.submodules.PadKey
import id.psw.vshlauncher.submodules.SfxType
import id.psw.vshlauncher.toLerp
import id.psw.vshlauncher.types.items.XmbMenuItem
import id.psw.vshlauncher.views.XmbLayoutType
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.views.XmbWidget
import id.psw.vshlauncher.views.drawText
import kotlin.math.absoluteValue
import kotlin.math.sin

class XmbSideMenu(view: XmbView) : XmbWidget(view) {
    private val textPaint : Paint = vsh.makeTextPaint(10.0f)
    private val shapeOutline : Paint = vsh.makeTextPaint(style= Paint.Style.STROKE, color = Color.WHITE).apply {
        strokeWidth = 3.0f
    }
    private val shapeFill : Paint = vsh.makeTextPaint(style= Paint.Style.FILL, color = FColor.setAlpha(Color.BLACK, 0.5f))

    enum class TouchInteractMode
    {
        Tap,
        Gesture;

        fun toInt() = ordinal
        companion object
        {
            fun fromInt(value: Int) = entries[value]
        }
    }

    var showMenuDisplayFactor = 0.0f
    var isDisplayed = false
    var selectedIndex = 0
    var viewedIndex = Point(-5, 5)
    var viewRangeMinPlus = 5

    val itemMenuRectF = RectF()
    val items = arrayListOf<XmbMenuItem>()
    var disableMenuExec = false

    var interactionMode : TouchInteractMode
        get()
        {
            val defVal = TouchInteractMode.Tap.toInt()
            val saved = vsh.M.pref.get(PrefEntry.SIDEMENU_TOUCH_INTERACTION_MODE, defVal)
            return TouchInteractMode.fromInt(saved)
        }
        set(value)
        {
            vsh.M.pref.set(PrefEntry.SIDEMENU_TOUCH_INTERACTION_MODE, value.toInt())
        }

    fun show(items : ArrayList<XmbMenuItem>){
        this.items.clear()
        this.items.addAll(items)
        isDisplayed = true
    }

    fun show(){
        isDisplayed = true
    }

    fun hide(){
        this.items.clear()
        isDisplayed = false
    }

    private val allItems : ArrayList<XmbMenuItem>?  get(){
        val item = vsh.hoveredItem
        return if(view.activeScreen == view.screens.mainMenu && item?.hasMenu == true){
            item.menuItems
        }else{
            this.items
        }
    }

    fun moveCursor(isDown:Boolean){
        try{
            val menuItems = allItems
            if(menuItems != null){
                val sortedMenu = menuItems.sortedBy { it.displayOrder }
                val cIndex = sortedMenu.indexOfFirst { it.displayOrder == selectedIndex }
                val newIndex = cIndex + isDown.select(1, -1)
                if(cIndex >= 0 && newIndex < menuItems.size && newIndex >= 0){
                    selectedIndex = sortedMenu[newIndex].displayOrder
                }
                M.audio.playSfx(SfxType.Selection)
            }
        }catch(_:ArrayIndexOutOfBoundsException){

        }
    }

    private val iconRectF = RectF(-12.0f, -12.0f, 12.0f, 12.0f)

    private fun drawArrows(ctx:Canvas, menuRect: RectF)
    {
        if(!view.screens.mainMenu.arrowBitmapLoaded) return
        val bmp = view.screens.mainMenu.arrowBitmap

        val yPosOffset = sin((view.time.currentTime * 3.0f) % (Math.PI.toFloat() * 42.0f)) * 5.0f

        val xCenter = menuRect.left + 200.0f - 12.0f

        // Up arrow
        ctx.withTranslation(xCenter, menuRect.top + 100.0f + yPosOffset) {
            ctx.withRotation(90.0f) {
                ctx.drawBitmap(bmp, null,iconRectF,null)
            }
        }

        // Down arrow
        ctx.withTranslation(xCenter, menuRect.bottom - 100.0f - yPosOffset) {
            ctx.withRotation(-90.0f) {
                ctx.drawBitmap(bmp, null,iconRectF,null)
            }
        }
    }

    override fun render(ctx: Canvas) {
        showMenuDisplayFactor = (time.deltaTime * 10.0f).toLerp(showMenuDisplayFactor, isDisplayed.select(1.0f, 0.0f)).coerceIn(0.0f, 1.0f)

        if(showMenuDisplayFactor < 0.1f) return

        val isPSP = view.screens.mainMenu.layoutMode == XmbLayoutType.PSP
        textPaint.textSize = isPSP.select(30.0f, 20.0f)

        val menuLeft = showMenuDisplayFactor.toLerp(scaling.viewport.right + 10.0f, scaling.target.right - 400f)

        val items = allItems
        itemMenuRectF.set(
            menuLeft,
            scaling.viewport.top- 10.0f,
            scaling.viewport.right + 20.0f,
            scaling.viewport.bottom + 10.0f)

        ctx.drawRect(itemMenuRectF, shapeFill)
        ctx.drawRect(itemMenuRectF, shapeOutline)

        drawArrows(ctx, itemMenuRectF)

        val zeroIdx = itemMenuRectF.centerY()
        val textSize = textPaint.textSize * isPSP.select(1.5f, 1.25f)
        val textLeft = isPSP.select(0.0f, textSize) + menuLeft + 20.0f

        if(items != null && items.size > 0)
        {
            synchronized(items){
                items.forEach {
                    textPaint.color = it.isDisabled.select(Color.GRAY, Color.WHITE)

                    if (it.displayOrder == selectedIndex) {
                        if (isPSP) {
                            val yOff = zeroIdx + (it.displayOrder * textSize)
                            itemMenuRectF.set(
                                textLeft - 5.0f, yOff - textSize,
                                scaling.viewport.right - 5.0f, yOff
                            )
                            if (showMenuDisplayFactor > 0.1f) {
                                ctx.drawRoundRect(
                                    itemMenuRectF,
                                    5.0f, 5.0f, shapeFill
                                )
                                ctx.drawRoundRect(
                                    itemMenuRectF,
                                    5.0f, 5.0f, shapeOutline
                                )
                            }
                        } else {
                            if (view.screens.mainMenu.arrowBitmapLoaded) {
                                val bitmap = view.screens.mainMenu.arrowBitmap
                                val xOff = textLeft - 20.0f
                                val yOff = zeroIdx + ((it.displayOrder - 0.75f) * textSize)
                                ctx.withTranslation(xOff, yOff) {
                                    ctx.withRotation(180.0f) {
                                        itemMenuRectF.set(-12.0f, -12.0f, 12.0f, 12.0f)
                                        ctx.drawBitmap(bitmap, null, itemMenuRectF, null)
                                    }
                                }
                            }
                        }
                    }

                    ctx.drawText(
                        it.displayName,
                        textLeft,
                        zeroIdx + (it.displayOrder * textSize),
                        textPaint,
                        -0.5f,
                        false
                    )
                }
            }
        }
        else
        {
            isDisplayed = false
        }

    }

    fun executeSelected(){
        try{
            val items = allItems
            if(items != null && items.size > 0){
                items.find {it.displayOrder == selectedIndex}?.onLaunch?.invoke()
                M.audio.playSfx(SfxType.Confirm)
            }
        }catch(_:ArrayIndexOutOfBoundsException){

        }
    }

    fun onGamepadInput(key: PadKey, isDown: Boolean) : Boolean
    {
        if(isDown){
            when(key){
                PadKey.PadU -> {
                    moveCursor(false)
                    return true
                }
                PadKey.PadD -> {
                    view.widgets.sideMenu.moveCursor(true)
                    return true
                }
                PadKey.Triangle -> {
                    widgets.sideMenu.isDisplayed = false
                    return true
                }
                PadKey.Confirm, PadKey.StaticConfirm -> {
                    if(!disableMenuExec){
                        view.widgets.sideMenu.executeSelected()
                        widgets.sideMenu.isDisplayed = false
                    }
                    return true
                }
                PadKey.StaticCancel, PadKey.Cancel -> {
                    widgets.sideMenu.isDisplayed = false
                    return true
                }
                else -> { }
            }
        }else{
            when(key){
                PadKey.Confirm, PadKey.StaticConfirm -> {
                    disableMenuExec = false
                    return true
                }
                else -> { }
            }
        }
        return false
    }

    private var _hasCursorMoved = false
    fun onTouchScreen(start: PointF, current: PointF, action: Int)
    {
        when(action){
            MotionEvent.ACTION_DOWN ->{
                if(interactionMode == TouchInteractMode.Tap)
                {
                    // Is Up
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
                start.set(current)
                _hasCursorMoved = false
            }
            MotionEvent.ACTION_MOVE ->
            {
                if(interactionMode == TouchInteractMode.Gesture)
                {
                    val yDiff = current.y - start.y
                    val absDiff = yDiff.absoluteValue
                    if(absDiff > 50.0f)
                    {
                        view.widgets.sideMenu.moveCursor(yDiff > 0.0f)
                        _hasCursorMoved = true
                        start.set(current)
                    }
                }
            }
            MotionEvent.ACTION_UP ->
            {
                if(interactionMode == TouchInteractMode.Gesture)
                {
                    val len = (current - start).length()
                    if(!_hasCursorMoved && len < 5.0f)
                    {
                        if(current.x < scaling.target.right - 400.0f)
                        {
                            widgets.sideMenu.isDisplayed = false
                        }
                        else
                        {
                            view.widgets.sideMenu.executeSelected()
                        }
                    }
                }
            }
        }
    }
}