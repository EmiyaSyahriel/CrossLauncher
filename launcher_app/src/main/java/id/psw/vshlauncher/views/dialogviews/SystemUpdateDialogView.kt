package id.psw.vshlauncher.views.dialogviews

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import id.psw.vshlauncher.R
import id.psw.vshlauncher.makeTextPaint
import id.psw.vshlauncher.submodules.BitmapRef
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.views.XmbView

class SystemUpdateDialogView(v: XmbView) : XmbDialogSubview(v) {

    override val useRefIcon: Boolean = true
    override val reficon: BitmapRef = BitmapRef("ic_system_update", { vsh.loadTexture(R.drawable.ic_sync_loading) }, BitmapRef.FallbackColor.Transparent)

    override val hasNegativeButton: Boolean
        get() = !(vsh.M.updater.isDownloading || vsh.M.updater.isChecking)
    override val negativeButton: String
        get() = vsh.getString(R.string.common_back)

    override val hasPositiveButton: Boolean
        get() = !(vsh.M.updater.isDownloading || vsh.M.updater.isChecking)
    override val positiveButton: String
        get() = if(vsh.M.updater.hasUpdate) vsh.getString(R.string.system_update_download)
            else vsh.getString(R.string.system_update_check)

    override val title: String
        get() = vsh.getString(R.string.settings_system_update_name)

    private val textPaint = vsh.makeTextPaint(20.0f).apply {
        textAlign = Paint.Align.CENTER
    }
    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime: Float) {
        when {
            vsh.M.updater.hasUpdate -> {
                ctx.drawText("Update found!", drawBound.centerX(), drawBound.centerY(), textPaint)
            }
            vsh.M.updater.isChecking -> {
                ctx.drawText("Checking ...", drawBound.centerX(), drawBound.centerY(), textPaint)
            }
            vsh.M.updater.isDownloading -> {
                ctx.drawText("Downloading ...", drawBound.centerX(), drawBound.centerY(), textPaint)
            }
            else -> {
                ctx.drawText("Launcher is up-to-date", drawBound.centerX(), drawBound.centerY(), textPaint)
                // No update, no checking
            }
        }
        super.onDraw(ctx, drawBound, deltaTime)
    }

    private fun close(){
        finish(view.screens.mainMenu)
    }

    override fun onDialogButton(isPositive: Boolean) {
        when {
            vsh.M.updater.hasUpdate -> {
                if(isPositive){
                    vsh.M.updater.beginDownload()
                }else{
                    close()
                }
            }
            else -> {
                if(!isPositive){
                    close()
                }else{
                    vsh.M.updater.beginCheck()
                }
            }
        }
    }

    override fun onClose() {
        super.onClose()
        reficon.release()
    }

}