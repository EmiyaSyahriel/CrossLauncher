package id.psw.vshlauncher.views.dialogviews

import android.graphics.*
import android.view.MotionEvent
import androidx.core.graphics.contains
import androidx.core.graphics.minus
import androidx.core.graphics.withClip
import id.psw.vshlauncher.*
import id.psw.vshlauncher.submodules.GamepadSubmodule
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.*
import id.psw.vshlauncher.views.nativedlg.NativeEditTextDialog
import kotlin.math.abs

class StatusBarFormatDialogView(val vsh: VSH) : XmbDialogSubview(vsh) {
    override val hasNegativeButton: Boolean = true
    override val hasPositiveButton: Boolean = true

    private val tmpRectF = RectF()
    private val tmpRectF1 = RectF()
    private var cTime = 0.0f
    private var isTextBarSelect = true
    private val tPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = FontCollections.masterFont
        textSize = 20.0f
        color = Color.WHITE
    }
    override val positiveButton: String
        get() = vsh.getString(isTextBarSelect.select(R.string.common_edit, R.string.common_save))

    private var textContent = "{operator} {sdf:dd/MM hh:mm a}"
    private var scrollOffset = 0.0f
    private val saveBtnRect = RectF()
    private val editBtnRect = RectF()

    override fun onStart() {
        textContent = vsh.pref.getString(PrefEntry.DISPLAY_STATUS_BAR_FORMAT, vsh.xmbView?.state?.crossMenu?.dateTimeFormat ?: textContent)
                    ?: textContent
    }

    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime: Float) {
        val lineSz = tPaint.textSize * 1.25f
        cTime += deltaTime

        tPaint.removeShadowLayer()
        // Draw Help Page
        tmpRectF.set(
            drawBound.left + 20.0f,
            drawBound.top + 20.0f,
            drawBound.right - 20.0f,
            drawBound.bottom - (lineSz * 6.0f),
        )
        ctx.withClip(tmpRectF){
            tPaint.textAlign = Paint.Align.LEFT
            val txt = tPaint.wrapText(vsh.getString(R.string.statusbarfmt_dlg_help), tmpRectF.width() - 20.0f).lines()

            scrollOffset = scrollOffset.coerceIn(-txt.size * tPaint.textSize, 0.0f)

            txt.forEachIndexed { i, it ->
                ctx.drawText(it, tmpRectF.left + 10.0f, (tmpRectF.top + ((i + 1) * lineSz) + scrollOffset), tPaint)
            }
        }

        tmpRectF1.set(tmpRectF.left, tmpRectF.bottom + lineSz * 1.0f, tmpRectF.right - 100.0f, tmpRectF.bottom + (lineSz * 3.0f))
        SubDialogUI.glowOverlay(ctx, tmpRectF1, (tPaint.textSize).toInt(), null, isTextBarSelect, cTime)
        ctx.withClip(tmpRectF1){
            tPaint.textAlign = Paint.Align.LEFT
            ctx.drawText(textContent, tmpRectF1.left + 10.0f, tmpRectF1.centerY(), tPaint, 0.5f)
        }

        var formatted = "ERR_FORMAT_STRUCTURE"
        try{
            formatted = vsh.xmbView?.formatStatusBar(textContent) ?: "ERR_XMBVIEW_NOT_INIT"
        }catch(e:Exception){ }

        ctx.drawText(formatted, tmpRectF1.left + 10.0f, tmpRectF1.bottom + lineSz, tPaint, 0.5f)

        if(!isTextBarSelect){
            tPaint.setShadowLayer(2.0f, 0.0f, 0.0f, Color.WHITE)
        }
        tPaint.textAlign = Paint.Align.CENTER
        ctx.drawText(vsh.getString(R.string.common_save), 0.5f.toLerp(tmpRectF1.right, tmpRectF.right), tmpRectF1.bottom, tPaint, 0.5f)
        editBtnRect.set(tmpRectF1)
        saveBtnRect.set(tmpRectF1.right, tmpRectF1.top, drawBound.right, drawBound.bottom)
        super.onDraw(ctx, drawBound, deltaTime)
    }

    private fun showTextBarEditor(){
        NativeEditTextDialog(vsh)
            .setTitle(vsh.getString(R.string.dlg_edit_status_bar_format))
            .setValue(textContent)
            .setOnFinish { textContent = it }
            .show()
    }

    private fun save(){
        vsh.xmbView?.state?.crossMenu?.dateTimeFormat = textContent
        vsh.pref.edit().putString(PrefEntry.DISPLAY_STATUS_BAR_FORMAT, textContent).apply()
        finish(VshViewPage.MainMenu)
    }

    override fun onDialogButton(isPositive: Boolean) {
        if(isPositive){
            if(isTextBarSelect){
                showTextBarEditor()
            }else{
                // Save
                save()
            }
        }else{
            finish(VshViewPage.MainMenu)
        }
    }

    override fun onGamepad(key: GamepadSubmodule.Key, isPress: Boolean): Boolean {
        return when(key){
            GamepadSubmodule.Key.PadU -> {
                if(isPress){
                    scrollOffset -= 1.0f
                    true
                }else false
            }
            GamepadSubmodule.Key.PadD -> {
                if(isPress){
                    scrollOffset += 1.0f
                    true
                }else false
            }
            GamepadSubmodule.Key.PadL, GamepadSubmodule.Key.PadR -> {
                if(isPress){
                    isTextBarSelect = !isTextBarSelect
                    true
                }else false
            }
            else -> false
        } || super.onGamepad(key, isPress)

    }

    private val lPoint = PointF()

    override fun onTouch(a: PointF, b: PointF, act: Int) {
        when(act){
            MotionEvent.ACTION_MOVE -> {
                val p = b - lPoint
                if(abs(p.x) > 100.0f){ // move horizontal
                    if(isTextBarSelect && p.x >= 0.0f) isTextBarSelect = false
                    if(!isTextBarSelect && p.x < 0.0f) isTextBarSelect = true
                    lPoint.set(b)
                }else if(abs(p.y) > 5.0f){
                    scrollOffset += p.y
                    lPoint.set(b)
                }
            }
            MotionEvent.ACTION_DOWN -> {
                lPoint.set(b)
            }
            MotionEvent.ACTION_UP -> {
                val p = b - a
                if(p.length() < 5.0f){
                    if(saveBtnRect.contains(b)){
                        save()
                    }
                    if(editBtnRect.contains(b)){
                        showTextBarEditor()
                    }
                }
                lPoint.set(0.0f, 0.0f)
            }
        }
    }

}