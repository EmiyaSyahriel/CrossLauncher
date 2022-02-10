package id.psw.vshlauncher.views.dialogviews

import android.graphics.*
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.R
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.submodules.GamepadSubmodule
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.views.VshViewPage
import id.psw.vshlauncher.views.XmbDialogSubview

class TestDialogView(private val vsh: VSH) : XmbDialogSubview(vsh) {
    override val icon: Bitmap
        get() = ResourcesCompat.getDrawable(vsh.resources, R.drawable.category_games, null)?.toBitmap(50,50) ?: XMBItem.TRANSPARENT_BITMAP
    private val tPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 20.0f
        color = Color.WHITE
    }

    override fun onDraw(ctx: Canvas, drawBound: RectF) {
        ctx.drawARGB(0x66, 0x00,0x99,0xFF)
        ctx.drawText("Test Dialog, press Cancel to back to reboot", drawBound.centerX(), drawBound.centerY(),tPaint )
        if(VSH.Gamepad.getKeyDown(GamepadSubmodule.Key.Cancel)){
            finish(VshViewPage.ColdBoot)
        }
    }

    override fun onClose() {
        icon.recycle()
    }
}