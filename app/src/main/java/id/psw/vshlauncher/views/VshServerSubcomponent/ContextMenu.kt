package id.psw.vshlauncher.views.VshServerSubcomponent

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import androidx.core.graphics.withSave
import id.psw.vshlauncher.icontypes.XMBIcon
import id.psw.vshlauncher.views.VshServer
import id.psw.vshlauncher.views.VshServerSubcomponent.xMath
import kotlin.math.absoluteValue

object ContextMenu{
    var visible = false
    var xOffset = 0.0f
    var lerpData = 0.0f
    const val contextMenuWidth = 350.0f

    val activeItem : XMBIcon? get()= VshServer.getActiveItem()
    val isItemHasMenu : Boolean get()= activeItem?.hasMenu ?: false && (activeItem?.menuCount ?: 0) > 0

    fun switchVisibility(){
        if(!VshServer.showDesktop){
            visible = if(isItemHasMenu) !visible else false

            if(!isItemHasMenu){
                Log.w("VSH::CtxMenu", "Item has no menu, switching will no be fulfilled")
            }
        }
    }

    fun lContextMenu(canvas: Canvas){
        lerpData = xMath.lerp(lerpData, if(visible) 1.0f else 0.0f, Time.deltaTime * 10.0f)

        if(visible && !isItemHasMenu) visible = false

        xOffset = xMath.lerp(0.0f, contextMenuWidth, lerpData)

        var leftSide = VshServer.orientWidth - xOffset

        val menuHeight = VshServer.orientHeight

        Paints.menuBackground.color = Color.argb(xMath.lerp(0f, 128f, lerpData).toInt(),0,0,0)
        canvas.drawPaint(Paints.menuBackground)

        canvas.withSave {
            canvas.clipRect(leftSide.toInt(), (-menuHeight).toInt(), VshServer.orientWidth.toInt(), (menuHeight * 2).toInt())

            // draw menu background
            Paints.menuBackground.strokeWidth = 1.0f
            Paints.menuBackground.color = Color.argb(128,255,255,255)
            Paints.menuBackground.style = Paint.Style.FILL
            canvas.drawRect(RectF(leftSide, -menuHeight, VshServer.orientWidth, menuHeight * 2), Paints.menuBackground)
            Paints.menuBackground.color = Color.WHITE
            Paints.menuBackground.style = Paint.Style.STROKE
            Paints.menuBackground.strokeWidth = 3.0f
            canvas.drawRect(RectF(leftSide, -menuHeight, VshServer.orientWidth, menuHeight * 2), Paints.menuBackground)

            if(isItemHasMenu){
                leftSide += xOffset * 0.1f

                val item = activeItem

                if(item != null){
                    item.menu.forEachIndexed { i, menu ->
                        val paint = if(i == item.menuIndex && menu.selectable) Paints.itemTitleSelected else Paints.itemTitleUnselected
                        val textSize =  paint.textSize
                        val textTopSide = (VshServer.orientHeight / 2) - ((item.menuCount * textSize) / 2f) + (i * textSize)

                        canvas.drawText(menu.name, leftSide, textTopSide, paint)
                    }
                }
            }else{
                val paint = Paints.itemTitleUnselected
                val textTopSide = (VshServer._refHeight / 2) - ((2 * paint.textSize) / 2f)
                canvas.drawText("Menu is empty", leftSide, textTopSide, paint)
            }
        }

    }

    fun setSelection(y: Int) {
        val item = activeItem
        if(item != null){
            item.menuIndex = (item.menuIndex + y).coerceIn(0, (item.menu.size - 1).coerceAtLeast(0))
        }
    }

    fun launchMenu(){
        val item = activeItem
        if(item != null){
            if(item.menuCount > item.menuIndex){
                val activeMenu = item.menu[item.menuIndex]
                if(activeMenu.selectable){
                    activeMenu.onClick.run()
                }
            }
        }
    }
}
