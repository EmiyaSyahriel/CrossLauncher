package id.psw.vshlauncher.views.dialogviews

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import id.psw.vshlauncher.R
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.views.XmbView

class SystemUpdateDialogView(v: XmbView) : XmbDialogSubview(v) {

    override val icon: Bitmap
        get() = super.icon

    override val title: String
        get() = vsh.getString(R.string.settings_system_update_name)

    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime: Float) {
        super.onDraw(ctx, drawBound, deltaTime)
    }

}