package id.psw.vshlauncher.views.dialogviews

import android.graphics.*
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.R
import id.psw.vshlauncher.types.XmbItem
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.views.XmbView

class TestDialogView(v: XmbView) : XmbDialogSubview(v) {
    override val icon: Bitmap
        get() = ResourcesCompat.getDrawable(vsh.resources, R.drawable.category_games, null)?.toBitmap(50,50) ?: XmbItem.TRANSPARENT_BITMAP
    private val tPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 20.0f
        color = Color.WHITE
    }

    override val hasNegativeButton: Boolean = true
    override val negativeButton: String = "Back"
    override val hasPositiveButton: Boolean
        get() = true
    override val positiveButton: String
        get() = "Reboot"

    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime:Float) {
        ctx.drawARGB(0x66, 0x00,0x99,0xFF)
        ctx.drawText("Test Dialog", drawBound.centerX(), drawBound.centerY(),tPaint )
    }

    override fun onClose() {
        icon.recycle()
    }

    override fun onDialogButton(isPositive: Boolean) {
        if(isPositive){
            vsh.restart()
        }else{
            finish(view.screens.mainMenu)
        }
        super.onDialogButton(isPositive)
    }
}