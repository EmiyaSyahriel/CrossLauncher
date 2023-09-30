package id.psw.vshlauncher.views.screens

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.text.TextPaint
import androidx.core.graphics.contains
import androidx.core.graphics.withClip
import androidx.core.graphics.withTranslation
import id.psw.vshlauncher.FittingMode
import id.psw.vshlauncher.hasConcurrentLoading
import id.psw.vshlauncher.select
import id.psw.vshlauncher.submodules.PadKey
import id.psw.vshlauncher.toLerp
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.typography.drawText
import id.psw.vshlauncher.typography.toButtonSpan
import id.psw.vshlauncher.views.XmbLayoutType
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.views.XmbScreen
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.views.drawBitmap
import id.psw.vshlauncher.views.drawText
import id.psw.vshlauncher.vsh
import java.util.Calendar

class XmbDialog(view : XmbView) : XmbScreen(view) {
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
    var iconTmpBound = RectF()
    private val tmpPointA = PointF()
    private val tmpPointB = PointF()
    private val dTmpBound = RectF()


    override fun start() {
        activeDialog?.onStart()
    }

    fun showDialog(dialog:XmbDialogSubview){
        activeDialog?.onClose() // Close previous dialog in case it's available
        activeDialog = dialog
        activeDialog?.onStart()
        view.switchScreen(screens.dialog)
    }

    override fun render(ctx: Canvas) {
        val ctxState = ctx.save()
        val isPSP = screens.mainMenu.layoutMode == XmbLayoutType.PSP

        val dlg = activeDialog
        if(dlg != null){
            dlg.isPSP = isPSP

            textPaint.textSize = isPSP.select(30.0f, 25.0f)

            tmpBound.set(scaling.target.left, scaling.target.top + 100f, scaling.target.right, scaling.target.bottom - 100f)
            ctx.drawLine(scaling.viewport.left - 10.0f, tmpBound.top,scaling.viewport.right-1.0f, tmpBound.top, outlinePaint)
            ctx.drawLine(scaling.viewport.left - 10.0f, tmpBound.bottom,scaling.viewport.right-1.0f, tmpBound.bottom, outlinePaint)

            // Draw title
            ctx.drawText(dlg.title, 125f, 65f, textPaint, 0.5f)
            iconTmpBound.set(50f, 65f - 25f, 100f, 65f + 25f)
            if(dlg.useRefIcon){
                ctx.drawBitmap(dlg.reficon.bitmap, null, iconTmpBound, null, FittingMode.FIT, 0.5f, 0.5f)
            }else{
                ctx.drawBitmap(dlg.icon, null, iconTmpBound, null, FittingMode.FIT, 0.5f, 0.5f)
            }

            if(context.vsh.hasConcurrentLoading){
                view.widgets.analogClock.render(ctx)
            }

            if(dlg.shouldClose){
                view.switchScreen(dlg.closeDialogTo)
            }

            // Draw subview content
            ctx.withClip(tmpBound){
                ctx.withTranslation(tmpBound.left, tmpBound.top){
                    tmpBound.bottom -= tmpBound.top
                    tmpBound.top = 0.0f
                    dlg.onDraw(ctx, tmpBound, time.deltaTime)
                }
            }

            val asian = !PadKey.spotMarkedByX
            val lAlign = textPaint.textAlign
            textPaint.textAlign = Paint.Align.CENTER
            // draw dialog buttons
            if(dlg.hasNegativeButton){
                ctx.drawText(
                        " {cancel} ${dlg.negativeButton} ".toButtonSpan(context.vsh),
                        0.5f.toLerp(scaling.target.centerX(), scaling.target.left),
                        scaling.target.bottom - 50.0f, 0.5f,
                        textPaint)
            }

            if(dlg.hasPositiveButton){
                ctx.drawText(
                        " {confirm} ${dlg.positiveButton} ".toButtonSpan(context.vsh),
                        0.5f.toLerp(scaling.target.centerX(), scaling.target.right),
                        scaling.target.bottom - 50.0f, 0.5f,
                        textPaint
                )
            }
            textPaint.textAlign = lAlign
        }else{
            view.switchScreen(screens.mainMenu)
        }
        ctx.restoreToCount(ctxState)
    }

    override fun onTouchScreen(start: PointF, current: PointF, action:Int){
        val dlg = activeDialog
        if(dlg != null){
            dTmpBound.set(scaling.target.left, scaling.target.top + 100f, scaling.target.right, scaling.target.bottom - 100f)
            val offA = tmpPointA
            val offB = tmpPointB

            if(dTmpBound.contains(start) || dTmpBound.contains(current)){

                offA.set(start.x - dTmpBound.left, start.y - dTmpBound.top)
                offB.set(current.x - dTmpBound.left, current.y - dTmpBound.top)
                dlg.onTouch(offA,offB,action)
            }else{
                if(scaling.target.contains(start) && start.y > dTmpBound.bottom && action == android.view.MotionEvent.ACTION_UP){
                    if(dlg.hasPositiveButton || dlg.hasNegativeButton){
                        dlg.onDialogButton(start.x > scaling.target.centerX())
                    }
                }
            }
        }
    }

    override fun end() {
        activeDialog?.onClose()
        activeDialog = null
    }

    override fun onGamepadInput(key:PadKey, isDown: Boolean) : Boolean {
        var retval = false
        val dlg = activeDialog
        if(dlg != null){
            when(key){
                PadKey.Confirm, PadKey.StaticConfirm -> if(isDown) {
                    dlg.onDialogButton(true)
                    retval = true
                }
                PadKey.Cancel, PadKey.StaticCancel -> if(isDown) {
                    dlg.onDialogButton(false)
                    retval = true
                }
                else -> retval = dlg.onGamepad(key, isDown)
            }
        }
        return retval
    }
}