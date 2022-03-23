package id.psw.vshlauncher.views.dialogviews

import android.app.PendingIntent
import android.content.Intent
import android.graphics.*
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.R
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.select
import id.psw.vshlauncher.submodules.GamepadSubmodule
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.views.VshViewPage
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.xmb
import kotlin.system.exitProcess

class TestDialogView(private val vsh: VSH) : XmbDialogSubview(vsh) {
    override val icon: Bitmap
        get() = ResourcesCompat.getDrawable(vsh.resources, R.drawable.category_games, null)?.toBitmap(50,50) ?: XMBItem.TRANSPARENT_BITMAP
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

    override fun onDraw(ctx: Canvas, drawBound: RectF) {
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
            finish(VshViewPage.MainMenu)
        }
        super.onDialogButton(isPositive)
    }
}