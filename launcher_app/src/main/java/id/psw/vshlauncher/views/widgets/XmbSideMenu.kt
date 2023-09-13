package id.psw.vshlauncher.views.widgets

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.RectF
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import id.psw.vshlauncher.FColor
import id.psw.vshlauncher.select
import id.psw.vshlauncher.submodules.SfxType
import id.psw.vshlauncher.toLerp
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.XMBLayoutType
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.views.XmbWidget
import id.psw.vshlauncher.views.drawText

class XmbSideMenu(view: XmbView) : XmbWidget(view) {
    val menuContextMenuTextPaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 10.0f
        textAlign = Paint.Align.LEFT
        typeface = FontCollections.masterFont
        color = Color.WHITE
    }
    val menuContextMenuOutline : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 3.0f
        style = Paint.Style.STROKE
        typeface = FontCollections.masterFont
        color = Color.WHITE
    }
    val menuContextMenuFill : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = FColor.setAlpha(Color.BLACK, 0.5f)
        typeface = FontCollections.masterFont
    }

    var showMenuDisplayFactor = 0.0f
    var isDisplayed = false
    var selectedIndex = 0
    var viewedIndex = Point(-5, 5)
    var viewRangeMinPlus = 5

    val itemMenuRectF = RectF()

    fun moveCursor(isDown:Boolean){
        try{
            val item = vsh.hoveredItem
            if(item != null){
                if(item.hasMenu){
                    val menuItems = item.menuItems
                    if(menuItems != null){
                        val sortedMenu = menuItems.sortedBy { it.displayOrder }
                        val cIndex = sortedMenu.indexOfFirst { it.displayOrder == selectedIndex }
                        val newIndex = cIndex + isDown.select(1, -1)
                        if(cIndex >= 0 && newIndex < menuItems.size && newIndex >= 0){
                            selectedIndex = sortedMenu[newIndex].displayOrder
                        }
                        M.audio.playSfx(SfxType.Selection)
                    }
                }
            }
        }catch(_:ArrayIndexOutOfBoundsException){

        }
    }

    override fun render(ctx: Canvas) {
        val isPSP = view.screens.mainMenu.layoutMode == XMBLayoutType.PSP

        val item = vsh.hoveredItem
        if(item != null){
            if(item.hasMenu){
                menuContextMenuTextPaint.textSize = isPSP.select(30.0f, 20.0f)

                showMenuDisplayFactor = (time.deltaTime * 10.0f).toLerp(showMenuDisplayFactor, isDisplayed.select(1.0f, 0.0f)).coerceIn(0.0f, 1.0f)
                val menuLeft = showMenuDisplayFactor.toLerp(scaling.viewport.right + 10.0f, scaling.target.right - 400f)

                itemMenuRectF.set(
                        menuLeft,
                        scaling.viewport.top- 10.0f,
                        scaling.viewport.right + 20.0f,
                        scaling.viewport.bottom + 10.0f)

                ctx.drawRect(itemMenuRectF, menuContextMenuFill)
                ctx.drawRect(itemMenuRectF, menuContextMenuOutline)

                val zeroIdx = itemMenuRectF.centerY()
                val textSize = menuContextMenuTextPaint.textSize * isPSP.select(1.5f, 1.25f)
                val textLeft = isPSP.select(0.0f, textSize) + menuLeft + 20.0f

                item.menuItems?.forEach {
                    menuContextMenuTextPaint.color = it.isDisabled .select(Color.GRAY, Color.WHITE)
                    if(it.displayOrder == selectedIndex){
                        // menuContextMenuTextPaint
                        //     .setShadowLayer(
                        //         abs(sin(time.currentTime)) * 10.0f,
                        //         0f,0f, Color.WHITE)
                    } else {
                        // menuContextMenuTextPaint
                        //     .setShadowLayer(0.0f, 0f,0f, Color.TRANSPARENT)
                    }

                    if(it.displayOrder == selectedIndex){
                        if(isPSP){
                            val yOff = zeroIdx + (it.displayOrder * textSize)
                            itemMenuRectF.set(textLeft - 5.0f, yOff - textSize,
                                    scaling.viewport.right - 5.0f, yOff)
                            if(showMenuDisplayFactor > 0.1f){
                                ctx.drawRoundRect(itemMenuRectF,
                                        5.0f, 5.0f, menuContextMenuFill)
                                ctx.drawRoundRect(itemMenuRectF,
                                        5.0f, 5.0f, menuContextMenuOutline)
                            }
                        }else{
                            if(view.screens.mainMenu.arrowBitmapLoaded){
                                val bitmap = view.screens.mainMenu.arrowBitmap
                                val xOff = textLeft - 20.0f
                                val yOff = zeroIdx + ((it.displayOrder - 0.75f) * textSize)
                                ctx.withTranslation(xOff, yOff) {
                                    ctx.withRotation(180.0f){
                                        itemMenuRectF.set(-12.0f, -12.0f, 12.0f, 12.0f)
                                        ctx.drawBitmap(bitmap, null, itemMenuRectF, null)
                                    }
                                }
                            }
                        }
                    }

                    ctx.drawText(it.displayName, textLeft, zeroIdx + (it.displayOrder * textSize), menuContextMenuTextPaint, -0.5f, false)
                }
            }else{
                isDisplayed = false
            }
        }
    }

    fun execute(){
        try{
            val item = vsh.hoveredItem
            if(item != null){
                if(item.hasMenu){
                    item.menuItems?.find {it.displayOrder == selectedIndex}?.onLaunch?.invoke()
                    M.audio.playSfx(SfxType.Confirm)
                }
            }
        }catch(_:ArrayIndexOutOfBoundsException){

        }
    }
}