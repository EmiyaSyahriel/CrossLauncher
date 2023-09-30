package id.psw.vshlauncher.views.dialogviews

import android.graphics.*
import android.text.TextPaint
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.minus
import id.psw.vshlauncher.*
import id.psw.vshlauncher.submodules.PadKey
import id.psw.vshlauncher.submodules.XMBAdaptiveIconRenderer
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.DrawExtension
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.views.wrapText
import kotlin.math.abs

class IconPriorityDialogView(v: XmbView) : XmbDialogSubview(v) {
    companion object {
        const val TAG = "iconldr.sys"
        val iconTypeToStrId = arrayOf(
            R.string.dlg_iconprior_priority_app_icon, //XMBAdaptiveIconRenderer.ICON_PRIORITY_TYPE_APP_ICON
            R.string.dlg_iconprior_priority_app_icon_adaptive, //XMBAdaptiveIconRenderer.ICON_PRIORITY_TYPE_APP_ICON
            R.string.dlg_iconprior_priority_app_banner, //XMBAdaptiveIconRenderer.ICON_PRIORITY_TYPE_APP_ICON
            R.string.dlg_iconprior_priority_app_banner_adaptive //XMBAdaptiveIconRenderer.ICON_PRIORITY_TYPE_APP_ICON
        )
    }

    private var isSelecting = false
    private var sourceNumber = 0
    private var targetNumber = 0
    private var cursor = 0
    override val positiveButton: String get() = vsh.getString(isSelecting.select(R.string.rearrange_end, R.string.rearrange_start))
    override val negativeButton: String get()= vsh.getString(isSelecting.select(android.R.string.cancel, R.string.common_save))

    override val title: String
        get() = vsh.getString(R.string.settings_system_reorder_icon_loading_name)

    override val hasNegativeButton: Boolean = true
    override val hasPositiveButton: Boolean = true
    private val tPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = FontCollections.masterFont
        textSize = 20.0f
        color = Color.WHITE
    }

    private val iconBitmap = vsh.resources.getDrawable(R.drawable.icon_storage).toBitmap(128,128)
    private val pNum = 0
    override val icon: Bitmap = iconBitmap

    private val priorityArray = arrayOf(0,0,0,0)

    init {
        for(i in 0 .. 3){ priorityArray[i] = XMBAdaptiveIconRenderer.getIconPriorityAt(i) }

        checkAndFixDupe()
    }

    // Check in-case there is a duplicated item
    private fun checkAndFixDupe() {
        var needFix = false

        priorityArray.forEachIndexed aScope@ { ia, a ->
            priorityArray.forEachIndexed { ib, b ->
                if(ia != ib && a == b) {
                    needFix = true
                    Logger.w(TAG, "Same number at index $ia and index $ib ($a == $b)")
                    return@aScope
                }
            }
        }

        if(!needFix) return
        priorityArray[0] = XMBAdaptiveIconRenderer.ICON_PRIORITY_TYPE_APP_ICON_ADAPTIVE
        priorityArray[1] = XMBAdaptiveIconRenderer.ICON_PRIORITY_TYPE_APP_BANNER_ADAPTIVE
        priorityArray[2] = XMBAdaptiveIconRenderer.ICON_PRIORITY_TYPE_APP_BANNER_LEGACY
        priorityArray[3] = XMBAdaptiveIconRenderer.ICON_PRIORITY_TYPE_APP_ICON_LEGACY
    }

    override fun onDialogButton(isPositive: Boolean) {
        if(isPositive){
            // Switch
            if(isSelecting){
                targetNumber = cursor
                swap()
            }else{
                sourceNumber = cursor
                isSelecting = true
            }
        }else{
            if(isSelecting){
                isSelecting = false
            }else{
                finish(view.screens.mainMenu)
            }
        }
    }

    private fun updateCursor(isDown : Boolean) : Boolean{
        cursor += isDown.select(1, -1)
        cursor = cursor.coerceIn(0, priorityArray.size - 1)
        return true
    }

    override fun onGamepad(key: PadKey, isPress: Boolean): Boolean {
        return if(isPress){
            when(key){
                PadKey.PadD -> updateCursor(true)
                PadKey.PadU -> updateCursor(false)
                else -> super.onGamepad(key, isPress)
            }
        } else super.onGamepad(key, isPress)
    }

    private val selectRect = RectF()

    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime: Float) {
        val cX = drawBound.centerX()
        val cY = drawBound.centerY()
        val pLine = tPaint.textSize * 1.25f
        val bY = drawBound.bottom - (pLine * 1.1f)
        val tY = drawBound.top + pLine
        val warnStrTop =
            tPaint.wrapText(
                vsh.getString(R.string.dlg_iconprior_warning_top),
                drawBound.width() - 100.0f).lines()

        val warnStrBtm =
            tPaint.wrapText(
                vsh.getString(R.string.dlg_iconprior_warning_btm),
                drawBound.width() - 100.0f).lines()

        tPaint.textAlign = Paint.Align.CENTER
        warnStrTop.forEachIndexed {
            i, it ->
            ctx.drawText(it, cX, tY + (i * pLine), tPaint)
        }
        warnStrBtm.forEachIndexed {
                i, it ->
            ctx.drawText(it, cX, bY - (i * pLine), tPaint)
        }


        var fY = cY - ((priorityArray.size * pLine) / 2.0f)
        val sfY = fY + (pLine * 0.25f)

        for(i in priorityArray){
            ctx.drawText(vsh.getString(iconTypeToStrId[i]), cX, fY, tPaint)
            fY += pLine
        }

        val w = 150.0f
        val w2 = 160.0f
        val curY = sfY + (pLine * (cursor - 1))
        selectRect.set(cX - w, curY, cX + w, curY + pLine)
        DrawExtension.glowOverlay(ctx, selectRect, 8, tPaint, true, 0.0f)
        if(isSelecting){

            val sCurY = sfY + (pLine * (sourceNumber - 1))
            selectRect.set(cX - w2, sCurY, cX + w2, sCurY + pLine)
            DrawExtension.glowOverlay(ctx, selectRect, 8, tPaint, true, 0.0f)
        }

        super.onDraw(ctx, drawBound, deltaTime)
    }

    private fun swap() {
        isSelecting = false
        if(sourceNumber > priorityArray.size || targetNumber > priorityArray.size){
            return
        }
        val a = priorityArray[sourceNumber]
        priorityArray[sourceNumber] = priorityArray[targetNumber]
        priorityArray[targetNumber] = a
    }

    override fun onTouch(a: PointF, b: PointF, act: Int) {
        val yD = (a - b).y

        if(abs(yD) > 50.0f){
            cursor = (cursor + (yD < 0).select(1, -1)).coerceIn(0, 3)

            b.y += 100.0f
            vsh.xmbView?.context?.xmb?.touchStartPointF?.set(b)
        }


        super.onTouch(a, b, act)
    }

    override fun onClose() {
        // Save
        var va = 0
        for(i in 0 .. 3){
            val orv = (priorityArray[i] shl ((3 - i) * 2))
            va = va or orv
        }
        XMBAdaptiveIconRenderer.Companion.AdaptiveRenderSetting.iconPriority = va
        vsh.M.pref.set(PrefEntry.ICON_RENDERER_PRIORITY, va)
        iconBitmap.recycle()
        super.onClose()
    }
}