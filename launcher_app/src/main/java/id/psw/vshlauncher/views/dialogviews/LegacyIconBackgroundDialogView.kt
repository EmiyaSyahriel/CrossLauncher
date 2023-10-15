package id.psw.vshlauncher.views.dialogviews

import android.graphics.*
import android.text.TextPaint
import androidx.core.graphics.minus
import id.psw.vshlauncher.*
import id.psw.vshlauncher.submodules.PadKey
import id.psw.vshlauncher.types.Ref
import id.psw.vshlauncher.types.XmbItem
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.*
import kotlin.math.abs

class LegacyIconBackgroundDialogView(v: XmbView) : XmbDialogSubview(v) {
    override val title: String get() = vsh.getString(R.string.dlg_legacyicon_title)
    override val hasNegativeButton: Boolean get() = true
    override val hasPositiveButton: Boolean get() = true
    override val positiveButton: String get() = vsh.getString(R.string.common_save)
    override val negativeButton: String get() = vsh.getString(android.R.string.cancel)
    private val yourColor = Ref(0)

    private var bgColor = Color.argb(0,0,0,0)
    private var bgYouX = 0
    private var bgYouY = 0
    private var bgMode = 0
    private var supportsYou = false
    private var selection : Int = 0
    private var sampleIcon : Bitmap = XmbItem.WHITE_BITMAP
    private var displayPaint : Paint = Paint().apply {
    }
    private var textPaint : TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 20.0f
        typeface = FontCollections.masterFont
    }
    private var iconRect = RectF()
    private var kvpRect = RectF()
    private var iconCenter = PointF()

    override fun onStart() {
        super.onStart()
        bgMode = vsh.M.pref.get(PrefEntry.ICON_RENDERER_LEGACY_BACKGROUND, 0)
        bgColor = vsh.M.pref.get(PrefEntry.ICON_RENDERER_LEGACY_BACK_COLOR, Color.WHITE)
        val you = vsh.M.pref.get(PrefEntry.ICON_RENDERER_LEGACY_BACK_MATERIAL_YOU, Color.WHITE)
        bgYouX = you / 100
        bgYouY = you % 100
        supportsYou = getMaterialYouColor(vsh, bgYouX, bgYouY, yourColor)
        selection = 0
        sampleIcon = vsh.loadTexture(R.drawable.ic_legacy_icon_background_preview, 320, 176)
        displayPaint.color = bgColor
    }

    override fun onDialogButton(isPositive: Boolean) {
        super.onDialogButton(isPositive)
        if(isPositive){

            val bgYou = bgYouX * 100 + bgYouY

            // Save
            vsh.M.pref
                .set(PrefEntry.ICON_RENDERER_LEGACY_BACKGROUND, bgMode)
                .set(PrefEntry.ICON_RENDERER_LEGACY_BACK_COLOR, bgColor)
                .set(PrefEntry.ICON_RENDERER_LEGACY_BACK_MATERIAL_YOU, bgYou)

            vsh.M.icons.readPreferences()
        }
        finish(view.screens.mainMenu)
    }

    override fun onTouch(a: PointF, b: PointF, act: Int) {
        val limit = when(bgMode) {
            1 -> 4
            2 -> 2
            else -> 0
        }
        val dif = (a - b)
        val yD = dif.y
        val xD = dif.x
        // Vertical

        var update = false
        if(abs(yD) > abs(xD)){
            if(abs(yD) > 50.0f){
                selection = if(yD < 0.0f){
                    (selection + 1 )
                }else{
                    (selection - 1 )
                }.coerceIn(0, limit)
                update = true
            }
        }else{ // Horizontal
            val xSense = if(
                selection == 0 || (bgMode == 2 && selection == 1)
            ) 100.0f else 50.0f

            if(abs(xD) > xSense){
                onAdd((xD > 0).select(-1, 1))
                update = true
            }
        }

        if(update){
            b.y += 100.0f
            vsh.xmbView!!.context.xmb.touchStartPointF.set(b)
        }
        super.onTouch(a, b, act)
    }

    private fun onAdd(adv : Int) : Boolean{
        val nrm = (adv > 0).select(1,-1)
        val modeMax = supportsYou.select(2, 1)
        when (bgMode) {
            0 -> {
                when(selection){
                    0 -> bgMode = (bgMode + nrm).coerceIn(0, modeMax)
                }
            }
            1 -> {
                var r = Color.red(bgColor)
                var g = Color.green(bgColor)
                var b = Color.blue(bgColor)
                var a = Color.alpha(bgColor)
                when(selection){
                    0 -> bgMode = (bgMode + nrm).coerceIn(0, modeMax)
                    1 -> r = (r + adv).coerceIn(0, 255)
                    2 -> g = (g + adv).coerceIn(0, 255)
                    3 -> b = (b + adv).coerceIn(0, 255)
                    4 -> a = (a + adv).coerceIn(0, 255)
                }
                bgColor = Color.argb(a,r,g,b)
            }
            2 -> {
                when(selection){
                    0 -> bgMode = (bgMode + nrm).coerceIn(0, modeMax)
                    1 -> bgYouX = (bgYouX + nrm).coerceIn(0, 2)
                    2 -> bgYouY = (bgYouY + nrm).coerceIn(0, accentBrightness.size - 1)
                }
                supportsYou = getMaterialYouColor(vsh, bgYouX, bgYouY, yourColor)
            }
        }
        return true
    }

    override fun onGamepad(key: PadKey, isPress: Boolean): Boolean {
        val clampNum = when(bgMode) {
            1 -> 4
            2 -> 2
            else -> 1
        }
        return if(isPress){
            when(key){
                PadKey.PadU -> { selection = (selection - 1).coerceIn(0, clampNum); true; }
                PadKey.PadD -> { selection = (selection + 1).coerceIn(0, clampNum); true; }
                PadKey.PadL -> onAdd(-1)
                PadKey.PadR -> onAdd(1)
                PadKey.L1 -> onAdd(-10)
                PadKey.R1 -> onAdd(10)
                else -> false
            }
        }else{
            false
        }
    }

    private val modes = arrayListOf(
        vsh.getString(R.string.common_disabled),
        vsh.getString(R.string.common_enabled),
        vsh.getString(R.string.dlg_legacyicon_material_you)
    )

    private val accentNumbers = arrayListOf(
        vsh.getString(R.string.dlg_legacyicon_material_you_accent_1),
        vsh.getString(R.string.dlg_legacyicon_material_you_accent_2),
        vsh.getString(R.string.dlg_legacyicon_material_you_accent_3)
    )

    private val accentBrightness = arrayListOf(
        "0", "10", "50",
        "100", "200", "300",
        "400", "500", "600",
        "700", "800", "900",
        "1000"
    )

    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime: Float) {
        super.onDraw(ctx, drawBound, deltaTime)

/// region Key Value Pair
        val center = drawBound.centerX()
        val y = drawBound.centerY()
        for(i in 0..4){
            val yy =y + (i * textPaint.textSize)
            kvpRect.set(center - 200.0f, yy, center + 200.0f, yy + textPaint.textSize)

            val modeSelector = {
                DrawExtension.editorTextValues(vsh, ctx, selection == i, vsh.getString(R.string.Mode), bgMode, modes, textPaint, 0.3f, kvpRect)
                if(!supportsYou){
                    bgMode = bgMode.coerceIn(0, 1)
                }
            }

            when (bgMode) {
                0 -> {
                    when(i){
                        0 -> {
                            //DrawExtension.editorCheckBox(vsh, ctx, selection == i, "Enabled", bgEnabled, textPaint, 0.3f, kvpRect)
                            modeSelector()
                        }
                    }
                }
                1 -> {
                    when(i){
                        0 -> {
                            //DrawExtension.editorCheckBox(vsh, ctx, selection == i, "Enabled", bgEnabled, textPaint, 0.3f, kvpRect)
                            modeSelector()
                        }
                        1 ->{
                            val v = Color.red(bgColor)
                            DrawExtension.editorGauge(vsh, ctx, selection == i, vsh.getString(
                                R.string.color_red), v/255.0f, "$v", textPaint, 0.3f, kvpRect)
                        }
                        2 ->{
                            val v = Color.green(bgColor)
                            DrawExtension.editorGauge(vsh, ctx, selection == i, vsh.getString(
                                R.string.color_green), v/255.0f, "$v", textPaint, 0.3f, kvpRect)
                        }
                        3 ->{
                            val v = Color.blue(bgColor)
                            DrawExtension.editorGauge(vsh, ctx, selection == i, vsh.getString(
                                R.string.color_blue), v/255.0f, "$v", textPaint, 0.3f, kvpRect)
                        }
                        4 ->{
                            val v = Color.alpha(bgColor)
                            DrawExtension.editorGauge(vsh, ctx, selection == i, vsh.getString(
                                R.string.color_alpha), v/255.0f, "$v", textPaint, 0.3f, kvpRect)
                        }
                    }
                }
                2 -> {
                    when(i){
                        0 -> {
                            //DrawExtension.editorCheckBox(vsh, ctx, selection == i, "Enabled", bgEnabled, textPaint, 0.3f, kvpRect)
                            modeSelector()
                        }
                        1 ->{
                            DrawExtension.editorTextValues(vsh, ctx, selection == i, vsh.getString(
                                                            R.string.dlg_legacyicon_you_accent), bgYouX, accentNumbers, textPaint, 0.3f, kvpRect)
                        }
                        2 ->{
                            DrawExtension.editorTextValues(vsh, ctx, selection == i, vsh.getString(
                                                            R.string.dlg_legacyicon_you_brightness), bgYouY, accentBrightness, textPaint, 0.3f, kvpRect)
                        }
                    }
                }
            }
        }
/// endregion

/// region Preview Icon
        iconCenter.set(
            drawBound.centerX(),
            drawBound.centerY() - 100.0f
        )

        iconRect.set(
            iconCenter.x - 120.0f,
            iconCenter.y - 66.0f,
            iconCenter.x + 120.0f,
            iconCenter.y + 66.0f
        )


        val lines = textPaint.wrapText(vsh.getString(R.string.dlg_legacyicon_update_info), 800.0f).split('\n')
        var cY = drawBound.bottom - (lines.size * textPaint.textSize)
        for(line in lines){
            textPaint.textAlign = Paint.Align.CENTER
            ctx.drawText(line, drawBound.centerX(), cY, textPaint)
            cY += textPaint.textSize
        }

        displayPaint.color = (bgMode == 2 && supportsYou).select(yourColor.p, bgColor)

        if(bgMode != 0) ctx.drawRect(iconRect, displayPaint)
        ctx.drawBitmap(sampleIcon, null, iconRect, null, FittingMode.FIT)
///endregion
    }
}