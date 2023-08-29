package id.psw.vshlauncher.views.dialogviews

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import id.psw.vshlauncher.R
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.makeTextPaint
import id.psw.vshlauncher.views.VshViewPage
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.views.wrapText

class ConfirmDialogView(val vsh: VSH, override val title: String, val iconId:Int = 0, val text:String, private val onChange : (Boolean) -> Unit) : XmbDialogSubview(vsh) {

    override val hasPositiveButton: Boolean
        get() = true

    override val hasNegativeButton: Boolean
        get() = true

    override val positiveButton: String
        get() = vsh.getString(R.string.dlg_confirm_confirm)

    override val negativeButton: String
        get() = vsh.getString(R.string.dlg_confirm_cancel)

    override fun onDialogButton(isPositive: Boolean) {
        onChange(isPositive)
        finish(VshViewPage.MainMenu)
    }

    private lateinit var paint : Paint

    override fun onStart() {
        paint = vsh.makeTextPaint(20.0f, align = Paint.Align.CENTER)
        super.onStart()
    }

    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime: Float) {
        val txtLines = paint.wrapText(text, 800.0f).lines()
        val top = drawBound.centerY() - (txtLines.size /2) * paint.textSize
        txtLines.forEachIndexed { i, line ->
            ctx.drawText(line, drawBound.centerX(), top + (i * paint.textSize), paint)
        }
        super.onDraw(ctx, drawBound, deltaTime)
    }
}