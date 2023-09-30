package id.psw.vshlauncher.views.widgets

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import id.psw.vshlauncher.Consts
import id.psw.vshlauncher.R
import id.psw.vshlauncher.makeTextPaint
import id.psw.vshlauncher.select
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.views.XmbLayoutType
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.views.XmbWidget
import id.psw.vshlauncher.views.drawText

class XmbSearchQuery(view: XmbView) : XmbWidget(view) {
    var searchIcon: Bitmap? = null
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val statusTextPaint = vsh.makeTextPaint(size = 10.0f, color = Color.WHITE).apply {
        style = Paint.Style.FILL
        strokeWidth = 3.0f
    }

    private fun drawSearchQueryPS3(ctx: Canvas){
        val cat = vsh.activeParent
        if(cat != null){
            val y = scaling.target.top + (scaling.target.height() * 0.1f)
            val x = scaling.target.left + 50.0f
            val q = cat.getProperty(Consts.XMB_ACTIVE_SEARCH_QUERY, "")
            if(q.isNotEmpty()){
                if(searchIcon == null){
                    searchIcon = vsh.loadTexture(R.drawable.ic_search, "icon_search", 32, 32, true)
                }
                val icon = searchIcon ?: XMBItem.WHITE_BITMAP
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

    private fun drawSearchQueryPSP(ctx: Canvas){
        val cat = vsh.activeParent
        if(cat != null){
            val y = scaling.target.top + widgets.statusBar.pspPadStatusBar.select(48.0f, 10.0f)
            val x = scaling.target.left + 10.0f
            val q = cat.getProperty(Consts.XMB_ACTIVE_SEARCH_QUERY, "")
            if(q.isNotEmpty()){
                if(searchIcon == null){
                    searchIcon = vsh.loadTexture(R.drawable.ic_search, "icon_search", 32, 32, true)
                }
                val icon = searchIcon ?: XMBItem.WHITE_BITMAP

                ctx.drawBitmap(icon,
                        null,
                        RectF(x, y, x + 30.0f, y + 30.0f),
                        iconPaint
                )
                val align = statusTextPaint.textAlign
                statusTextPaint.textAlign = Paint.Align.LEFT
                ctx.drawText(q, x + 35.0f, view.top + 15.0f, statusTextPaint, 0.5f)
                statusTextPaint.textAlign = align
            }
        }
    }

    override fun render(ctx: Canvas) {
        if(widgets.statusBar.disabled) return
        when(screens.mainMenu.layoutMode){
            XmbLayoutType.PS3 -> drawSearchQueryPS3(ctx)
            XmbLayoutType.PSP -> drawSearchQueryPSP(ctx)
            else -> drawSearchQueryPSP(ctx)
        }
    }
}