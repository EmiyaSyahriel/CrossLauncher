package id.psw.vshlauncher.views

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import id.psw.vshlauncher.FColor
import id.psw.vshlauncher.select
import id.psw.vshlauncher.toLerp
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.vsh
import kotlin.math.abs
import kotlin.math.sin

class ItemMenuState {
    val menuContextMenuTextPaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 25.0f
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
}

val itemMenuRectF = RectF()

fun XmbView.menuMoveItemMenuCursor(isDown:Boolean){
    with(state.itemMenu){
        try{
            val item = context.vsh.hoveredItem
            if(item != null){
                if(item.hasMenu){
                    val menuItems = item.menuItems
                    if(menuItems != null){
                        val sortedMenu = menuItems.sortedBy { it.displayOrder }
                        val cIndex = menuItems.indexOfFirst { it.displayOrder == selectedIndex }
                        if(isDown && cIndex + 1 < menuItems.size){
                            selectedIndex = sortedMenu[cIndex + 1].displayOrder
                        }
                        if(!isDown && cIndex - 1 >= 0){
                            selectedIndex = sortedMenu[cIndex - 1].displayOrder
                        }
                    }
                }
            }
        }catch(e:ArrayIndexOutOfBoundsException){

        }
    }
}

fun XmbView.menuStartItemMenu(){
    with(state.itemMenu){
        try{
            val item = context.vsh.hoveredItem
            if(item != null){
                if(item.hasMenu){
                    item.menuItems?.find {it.displayOrder == selectedIndex}?.onLaunch?.invoke()
                }
            }
        }catch(e:ArrayIndexOutOfBoundsException){

        }
    }
}

fun XmbView.menuRenderItemMenu(ctx: Canvas){
    val isPSP = state.crossMenu.layoutMode == XMBLayoutType.PSP
    with(state.itemMenu){
        val item = context.vsh.hoveredItem
        if(item != null){
            if(item.hasMenu){

                menuContextMenuTextPaint.textSize = isPSP.select(30.0f, 25.0f)

                showMenuDisplayFactor = (time.deltaTime * 10.0f).toLerp(showMenuDisplayFactor, isDisplayed.select(1.0f, 0.0f))
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
                        menuContextMenuTextPaint.setShadowLayer(abs(sin(time.currentTime)) * 10.0f, 0f,0f, Color.WHITE)



                    } else {
                        menuContextMenuTextPaint.setShadowLayer(0.0f, 0f,0f, Color.TRANSPARENT)
                    }

                    ctx.drawText(it.displayName, textLeft, zeroIdx + (it.displayOrder * textSize), menuContextMenuTextPaint, 0.5f, false)
                }
            }
        }
    }
}
