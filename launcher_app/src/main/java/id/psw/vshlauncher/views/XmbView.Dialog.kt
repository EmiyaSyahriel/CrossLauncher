package id.psw.vshlauncher.views

import android.app.Dialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import androidx.core.graphics.withClip
import androidx.core.graphics.withTranslation
import id.psw.vshlauncher.FittingMode
import id.psw.vshlauncher.hasConcurrentLoading
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.typography.MultifontSpan
import id.psw.vshlauncher.vsh
import java.util.*

class VshViewDialogState {
    var activeDialog : XmbDialogSubview? = null
    var textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 25.0f
        color = Color.WHITE
        typeface = FontCollections.masterFont
    }
    var outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 3.0f
        style = Paint.Style.STROKE
        typeface = FontCollections.masterFont
    }
    var tmpBound = RectF()
    var dBackgroundAlpha = 0.0f
    var iconTmpBound = RectF()
}

fun XmbView.dlgStart(){
    with(state.dialog){
        activeDialog?.onStart()
        dBackgroundAlpha = 0.0f
    }
}

fun XmbView.showDialog(dialog:XmbDialogSubview){
    state.dialog.activeDialog?.onClose() // Close previous dialog in case it's available
    state.dialog.activeDialog = dialog
    switchPage(VshViewPage.Dialog)
}

fun XmbView.dlgRender(ctx: Canvas){
    val ctxState = ctx.save()
    with(state.dialog){
        if(dBackgroundAlpha < 1.0f){
            dBackgroundAlpha += time.deltaTime * 2.0f
        }

        ctx.drawARGB((dBackgroundAlpha * 128).toInt(), 0,0,0)
        val dlg = activeDialog
        if(dlg != null){
            tmpBound.set(scaling.target.left, scaling.target.top + 100f, scaling.target.right, scaling.target.bottom - 100f)
            ctx.drawLine(scaling.viewport.left - 10.0f, tmpBound.top,scaling.viewport.right-1.0f, tmpBound.top, outlinePaint)
            ctx.drawLine(scaling.viewport.left - 10.0f, tmpBound.bottom,scaling.viewport.right-1.0f, tmpBound.bottom, outlinePaint)

            // Draw title
            ctx.drawText(dlg.title, 125f, 65f, textPaint, 0.5f)
            iconTmpBound.set(50f, 65f - 25f, 100f, 65f + 25f)
            ctx.drawBitmap(dlg.icon, null, iconTmpBound, null, FittingMode.FIT, 0.5f, 0.5f)

            if(context.vsh.hasConcurrentLoading){
                drawClock(ctx, Calendar.getInstance(), 65f)
            }

            if(dlg.shouldClose){
                switchPage(dlg.closeDialogTo)
            }

            // Draw subview content
            ctx.withClip(tmpBound){
                ctx.withTranslation(tmpBound.left, tmpBound.top){
                    tmpBound.bottom -= tmpBound.top
                    tmpBound.top = 0.0f
                    dlg.onDraw(ctx, tmpBound)
                }
            }
        }else{
            switchPage(VshViewPage.MainMenu)
        }
    }
    ctx.restoreToCount(ctxState)
}

fun XmbView.dlgEnd(){
    with(state.dialog){
        activeDialog?.onClose()
        activeDialog = null
    }
}