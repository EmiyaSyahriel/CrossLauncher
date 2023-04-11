package id.psw.vshlauncher.views.dialogviews

import android.graphics.*
import android.text.TextPaint
import androidx.core.graphics.minus
import id.psw.vshlauncher.*
import id.psw.vshlauncher.submodules.GamepadSubmodule
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.types.items.XMBAppItem
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.DrawExtension
import id.psw.vshlauncher.views.VshViewPage
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.views.drawBitmap

class LegacyIconBackgroundDialogView(private val vsh: VSH) : XmbDialogSubview(vsh) {
    override val title: String get() = vsh.getString(R.string.dlg_legacyicon_title)
    override val hasNegativeButton: Boolean get() = true
    override val hasPositiveButton: Boolean get() = true
    override val positiveButton: String get() = vsh.getString(R.string.common_save)
    override val negativeButton: String get() = vsh.getString(android.R.string.cancel)

    private var bgColor = Color.argb(0,0,0,0)
    private var bgEnabled = false
    private var selection : Int = 0
    private var sampleIcon : Bitmap = XMBItem.WHITE_BITMAP
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
        bgEnabled = vsh.pref.getBoolean(PrefEntry.ICON_RENDERER_LEGACY_BACKGROUND, false)
        bgColor = vsh.pref.getInt(PrefEntry.ICON_RENDERER_LEGACY_BACK_COLOR, Color.WHITE)
        selection = 0
        sampleIcon = vsh.loadTexture(R.drawable.ic_legacy_icon_background_preview, 320, 176)
        displayPaint.color = bgColor
    }

    override fun onDialogButton(isPositive: Boolean) {
        super.onDialogButton(isPositive)
        if(isPositive){
            // Save
            vsh.pref.edit()
                .putBoolean(PrefEntry.ICON_RENDERER_LEGACY_BACKGROUND, bgEnabled)
                .putInt(PrefEntry.ICON_RENDERER_LEGACY_BACK_COLOR, bgColor)
                .apply()

            vsh.iconAdapter.readPreferences()
        }
        finish(VshViewPage.MainMenu)
    }

    override fun onTouch(a: PointF, b: PointF, act: Int) {

    }

    private fun onAdd(adv : Int) : Boolean{
        var r = Color.red(bgColor)
        var g = Color.green(bgColor)
        var b = Color.blue(bgColor)
        var a = Color.alpha(bgColor)
        when(selection){
            0 -> bgEnabled = !bgEnabled
            1 -> r = (r + adv).coerceIn(0, 255)
            2 -> g = (g + adv).coerceIn(0, 255)
            3 -> b = (b + adv).coerceIn(0, 255)
            4 -> a = (a + adv).coerceIn(0, 255)
        }
        bgColor = Color.argb(a,r,g,b)
        return true
    }

    override fun onGamepad(key: GamepadSubmodule.Key, isPress: Boolean): Boolean {
        return if(isPress){
            when(key){
                GamepadSubmodule.Key.PadU -> { selection = (selection - 1).coerceIn(0, 4); true; }
                GamepadSubmodule.Key.PadD -> { selection = (selection + 1).coerceIn(0, 4); true; }
                GamepadSubmodule.Key.PadL -> onAdd(-1)
                GamepadSubmodule.Key.PadR -> onAdd(1)
                GamepadSubmodule.Key.L1 -> onAdd(-10)
                GamepadSubmodule.Key.R1 -> onAdd(10)
                else -> false
            }
        }else{
            false
        }
    }

    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime: Float) {
        super.onDraw(ctx, drawBound, deltaTime)
/// region Key Value Pair
        val center = drawBound.centerX()
        val y = drawBound.centerY()
        for(i in 0..4){
            val yy =y + (i * textPaint.textSize)
            kvpRect.set(center - 200.0f, yy, center + 200.0f, yy + textPaint.textSize)
            when(i){
                0 -> {
                    DrawExtension.editorCheckBox(vsh, ctx, selection == i, "Enabled", bgEnabled, textPaint, 0.3f, kvpRect)
                }
                1 ->{
                    val v = Color.red(bgColor)
                    DrawExtension.editorGauge(vsh, ctx, selection == i, "Red", v/255.0f, "$v", textPaint, 0.3f, kvpRect)
                }
                2 ->{
                    val v = Color.green(bgColor)
                    DrawExtension.editorGauge(vsh, ctx, selection == i, "Green", v/255.0f, "$v", textPaint, 0.3f, kvpRect)
                }
                3 ->{
                    val v = Color.blue(bgColor)
                    DrawExtension.editorGauge(vsh, ctx, selection == i, "Blue", v/255.0f, "$v", textPaint, 0.3f, kvpRect)
                }
                4 ->{
                    val v = Color.alpha(bgColor)
                    DrawExtension.editorGauge(vsh, ctx, selection == i, "Alpha", v/255.0f, "$v", textPaint, 0.3f, kvpRect)
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

        displayPaint.color = bgColor

        if(bgEnabled) ctx.drawRect(iconRect, displayPaint)
        ctx.drawBitmap(sampleIcon, null, iconRect, null, FittingMode.FIT)
///endregion
    }
}