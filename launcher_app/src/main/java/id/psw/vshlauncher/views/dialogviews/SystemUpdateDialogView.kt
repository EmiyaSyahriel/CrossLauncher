package id.psw.vshlauncher.views.dialogviews

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import id.psw.vshlauncher.R
import id.psw.vshlauncher.makeTextPaint
import id.psw.vshlauncher.submodules.BitmapRef
import id.psw.vshlauncher.views.DrawExtension
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.views.asBytes
import kotlin.math.roundToInt

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
        val u = vsh.M.updater
        when {
            u.isChecking -> {
                ctx.drawText("Checking ...", drawBound.centerX(), drawBound.centerY(), textPaint)
            }
            u.isDownloading -> {
                val al =textPaint.textAlign
                textPaint.textAlign = Paint.Align.LEFT
                val lines = "Preparing to update ...\nDo not close the launcher.\nAfter preparation has completed, Please install the update package".lines()
                lines.forEachIndexed { i, s ->
                    val y = lines.size - i
                    ctx.drawText(s, drawBound.centerX() - 300.0f, drawBound.centerY() - (y * textPaint.textSize), textPaint)
                }
                DrawExtension.progressBar(ctx,
                    0.0f,
                    1.0f,
                    u.downloadProgressF,
                    drawBound.centerX() - 300.0f, drawBound.centerY(),
                    600.0f
                )
                textPaint.textAlign = Paint.Align.CENTER
                synchronized(u.locker){
                    val f = u.downloadProgressCurrent.asBytes()
                    val n = u.downloadProgressMax.asBytes()
                    val p = (u.downloadProgressF * 1000).roundToInt() / 10.0f
                    ctx.drawText("$f / $n ($p%)", drawBound.centerX(), drawBound.centerY() + 50.0f, textPaint)
                }
                textPaint.textAlign = al
            }
            u.hasUpdate -> {
                val lines = arrayOf(
                    "A system software update is found.",
                    "${u.updateSize} is going to be downloaded.",
                    "Do you want to update now?"
                )
                val al = textPaint.textAlign
                textPaint.textAlign = Paint.Align.LEFT
                lines.forEachIndexed { i, s ->
                    ctx.drawText(s, drawBound.centerX() - 200.0f, drawBound.centerY() + (i * textPaint.textSize), textPaint)
                }
                textPaint.textAlign = al
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
        val u = vsh.M.updater
        when {
            u.hasUpdate -> {
                if(isPositive){
                    u.beginDownload()
                }else{
                    close()
                }
            }
            else -> {
                if(!isPositive){
                    close()
                }else{
                    u.beginCheck()
                }
            }
        }
    }

    override fun onClose() {
        super.onClose()
        reficon.release()
    }

}