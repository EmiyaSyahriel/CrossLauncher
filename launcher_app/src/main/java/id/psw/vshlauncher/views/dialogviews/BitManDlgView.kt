package id.psw.vshlauncher.views.dialogviews

import android.graphics.*
import id.psw.vshlauncher.R
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.submodules.BitmapManager
import id.psw.vshlauncher.submodules.BitmapRef
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.views.asBytes


class BitManDlgView(v: XmbView) : XmbDialogSubview(v) {
    override val hasNegativeButton: Boolean = true
    override val hasPositiveButton: Boolean = false
    override val negativeButton: String = "Close"
    override val useRefIcon: Boolean = true

    override val title: String
        get() = "Bitmap Cache Info"
    override val reficon: BitmapRef = BitmapRef("ic_log_view", { vsh.loadTexture(R.drawable.ic_close) }, BitmapRef.FallbackColor.Transparent)

    private var lines = arrayListOf<Pair<String, String>>()
    private var tPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply{
        color = Color.WHITE
        typeface = FontCollections.masterFont
        textSize = 20.0f
    }

    override fun onStart() {


    }

    private val cPt = PointF()
    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime: Float) {

        lines.clear()
        lines.add(
            "Bitmap Count" to "${BitmapManager.instance.bitmapCount}"
        )
        lines.add(
            "Cache Memory Usage" to BitmapManager.instance.totalCacheSize.asBytes()
        )
        lines.add(
            "Bitmap Load Queue" to "${BitmapManager.instance.queueCount}"
        )

        val h = (lines.size * tPaint.textSize)
        var y = drawBound.centerY() - (h /2.0f)
        lines.forEach {
            cPt.set(drawBound.centerX(), y)
            tPaint.textAlign = Paint.Align.RIGHT
            ctx.drawText(it.first, cPt.x - 20.0f, cPt.y, tPaint)
            tPaint.textAlign = Paint.Align.LEFT
            ctx.drawText(it.second, cPt.x + 20.0f, cPt.y, tPaint)
            y += tPaint.textSize
        }
    }

    override fun onDialogButton(isPositive: Boolean) {
        if(!isPositive){
            finish(view.screens.mainMenu)
        }
    }

    override fun onClose() {
        reficon.release()
    }
}