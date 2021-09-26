package id.psw.vshlauncher.views.VshServerSubcomponent

import android.graphics.*
import id.psw.vshlauncher.getRect
import id.psw.vshlauncher.toLerp
import id.psw.vshlauncher.views.VshServer
import id.psw.vshlauncher.views.VshServer.drawText

object CrossMenu {
    var xLoc = 0.3f
    var yLoc = 0.3f
    var yLerpOffset = 0.0f
    var xLerpOffset = 0.0f
    var selectedIconSize = 125f
    var unselectedIconSize = 100f

    fun lHorizontalMenu(ctx: Canvas)
    {
        updateLerps()
        Debug.debugPaint.color = Color.argb(64,0,0,255)
        if(VshServer.depth <= 1){
            val items = VshServer.root.content
            val sidx = VshServer.root.selectedIndex
            val sidxf = VshServer.root.selectedIndexF
            items.forEachIndexed { i, it ->
                val delta = i - sidx
                val deltaF = i - sidxf
                val isSelected = delta == 0

                val iconSize = if(isSelected) selectedIconSize else unselectedIconSize
                val iconHalf = iconSize/2f
                val posX = xLoc * VshServer.orientWidth - (iconSize * xLerpOffset)
                val posY = yLoc * VshServer.orientHeight - (iconSize * 0.0F )

                val itemX = (posX - iconHalf) + (deltaF * (iconSize + 50))
                val iconRect = RectF(itemX, posY - iconHalf, itemX + iconSize, posY +iconHalf)

                // ctx.drawRect(iconRect, Debug.debugPaint)

                //val icon = if(isSelected) it.activeIcon else it.inactiveIcon

                val icon = it.activeIcon
                val paint = if(isSelected) Paints.itemTitleSelected else Paints.itemTitleUnselected

                ctx.drawBitmap(icon, icon.getRect(), iconRect, paint)
                if(isSelected){
                    Paints.categoryTitleSelected .textAlign = Paint.Align.CENTER
                    ctx.drawText(it.name, iconRect.centerX(), iconRect.bottom + 10f, Paints.categoryTitleSelected , 1.0f)
                }
            }
        }else{
            val iconSize = 125f
            val iconHalf = iconSize/2f
            val posX = xLoc * VshServer.orientWidth - (iconSize * xLerpOffset)
            val posY = yLoc * VshServer.orientHeight - (iconSize * yLerpOffset)
            val iconRect =  RectF(posX - iconSize, posY - iconHalf, posX + iconHalf,posY + iconHalf)
            val parent = VshServer.getActiveVerticalParentMenu()
            ctx.drawBitmap(parent.inactiveIcon, Rect(0,0,75,75), iconRect, Paints.itemSubtitleUnselected)
        }
    }

    fun updateLerps()
    {
        xLerpOffset = 0.5f.toLerp(xLerpOffset, 0f)
        yLerpOffset = 0.5f.toLerp(yLerpOffset, 0f)
    }

    fun lVerticalMenu(ctx: Canvas)
    {
        val sidx = VshServer.getActiveVerticalParentMenu().selectedIndex
        val sidxf = VshServer.getActiveVerticalParentMenu().selectedIndexF
        val items = VshServer.verticalItems
        val posXName = (xLoc * VshServer.orientWidth) + 100
        Debug.debugPaint.color = Color.argb(64,0,255,0)
        items.forEachIndexed{ i, it ->
            val delta = i - sidx
            val deltaF = i - sidxf
            val isSelected = delta == 0

            val iconSize = if(isSelected) selectedIconSize else unselectedIconSize
            val iconHalf = iconSize/2f
            val posX = xLoc * VshServer.orientWidth - (iconSize * xLerpOffset)
            val posY = yLoc * VshServer.orientHeight - (iconSize * yLerpOffset)

            var itemY = (posY - iconHalf) + (deltaF * (iconSize + 20))
            if(delta < 0){
                itemY -= 1
            }else{
                itemY += iconSize + 50
            }
            if(delta >= 1) itemY += 20;

            val titlePaint = if(isSelected) Paints.itemTitleSelected else Paints.itemTitleUnselected
            val descPaint = if(isSelected) Paints.itemSubtitleSelected else Paints.itemSubtitleUnselected

            val iconRect = RectF(posX - iconHalf, itemY, posX + iconHalf, itemY + iconSize)
            //val icon = if(isSelected) it.activeIcon else it.inactiveIcon
            val icon = it.activeIcon
            ctx.drawBitmap(icon, icon.getRect(), iconRect, titlePaint)
            titlePaint.textAlign = Paint.Align.LEFT
            descPaint.textAlign = Paint.Align.LEFT

            // ctx.drawRect(iconRect, Debug.debugPaint)

            val textYOffset = if(it.hasDescription) -0.25f else 0.5f

            ctx.drawText(it.name, posXName, iconRect.centerY(), titlePaint, textYOffset)
            if(it.hasDescription) ctx.drawText(it.description, posXName, iconRect.centerY(), descPaint, 1.0f)
        }
    }
}
