package id.psw.vshlauncher.views.dialogviews

import android.graphics.*
import android.text.InputType
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.minus
import id.psw.vshlauncher.R
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.select
import id.psw.vshlauncher.submodules.GamepadSubmodule
import id.psw.vshlauncher.submodules.PadKey
import id.psw.vshlauncher.types.Ref
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.*
import id.psw.vshlauncher.views.nativedlg.NativeEditTextDialog
import id.psw.vshlauncher.xmb
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

class CustomAspectRatioDialogView(private val vsh: VSH) : XmbDialogSubview(vsh) {
    private data class PresetSize(val w : Int, val h : Int )

    private var targetW = 1280
    private var targetH = 720
    private var aspectW = 16
    private var aspectH = 9
    private var isCustom = false

    private val presetSize = arrayOf(
        PresetSize(1280, 720),
        PresetSize(1280, 768),
        PresetSize(1280, 960),
        PresetSize(1280, 576),
        PresetSize(1280, 854),
        PresetSize(1200, 720),
        PresetSize(960, 720),
        PresetSize(1600, 720),
        PresetSize(1080, 720)
    )

    private var selection = 0
    private var dTime = 0.0f
    private val tPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 20.0f
        color = Color.WHITE
        typeface = FontCollections.masterFont
    }
    private val fPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    override val title: String
        get() = vsh.getString(R.string.settings_display_refsize_name)

    override val hasPositiveButton: Boolean
        get() = true

    override val icon: Bitmap
        get() = ResourcesCompat.getDrawable(vsh.resources, R.drawable.ic_fullscreen, null)!!.toBitmap(128, 128)

    override val hasNegativeButton: Boolean
        get() = true

    override val positiveButton: String
        get() {
            val str = if(isCustom){
                when(selection){
                    0, 1 ->  R.string.common_edit
                    2 -> R.string.common_apply
                    else -> R.string.common_toggle
                }
            }else{
                (selection == presetSize.size).select(R.string.common_toggle, R.string.common_apply)
            }
            return vsh.getString(str)
        }

    private fun isRound(f:Float) : Boolean {
        return floor(f).toInt() == ceil(f).toInt()
    }

    private fun genAspectRatio(w: Ref<Int>, h: Ref<Int>){
        var iMul = 1
        val ss = w.p.toFloat() / h.p
        val tW = ss
        val tH = 1.0f
        while(!isRound(tW * iMul) || !isRound( tH * iMul)){
            iMul++
        }
        w.p = (tW * iMul).toInt()
        h.p = (tH * iMul).toInt()
    }

    private fun reCalcAspectSize(){
        val rW = Ref(targetW)
        val rH = Ref(targetH)
        genAspectRatio(rW, rH)
        aspectW = rW.p
        aspectH = rH.p
    }

    private fun getSize(){
        val tg = vsh.xmbView?.scaling?.landTarget
        targetW = (tg?.width() ?: 1280.0f).toInt()
        targetH = (tg?.height() ?: 720.0f).toInt()
    }

    override fun onStart() {
        getSize()
        reCalcAspectSize()
        super.onStart()
    }

    private val bRect = RectF()

    private fun drawKvp(ctx:Canvas, key:String, drawBound: RectF, value:Int, y:Float, index:Int){
        tPaint.textAlign = Paint.Align.RIGHT
        ctx.drawText(key, drawBound.centerX(), y, tPaint, 0.5f)

        val tSize = tPaint.textSize * 0.75f
        bRect.set(
            drawBound.centerX() + 10.0f, y - tSize,
            drawBound.centerX() + 140.0f, y + tSize,
        )

        tPaint.textAlign = Paint.Align.CENTER
        ctx.drawText(value.toString(), bRect.centerX(), y, tPaint, 0.5f)
        if(selection == index){
            DrawExtension.glowOverlay(ctx, bRect, 10, fPaint, true, dTime)
        }
    }

    private fun drawCustom(ctx:Canvas, drawBound: RectF, deltaTime: Float){
        val wLine = tPaint.wrapText("Please keep the base size to 1280px horizontal or 720px vertical for the best experience", 500.0f).lines()
        tPaint.textAlign = Paint.Align.CENTER
        wLine.forEachIndexed {i, s ->
            ctx.drawText(s, drawBound.centerX(), drawBound.centerY() - 100.0f + (i * tPaint.textSize), tPaint)
        }

        fun getY(i:Int) : Float{
            return drawBound.centerY() - 30.0f + (i * (tPaint.textSize * 1.5f))
        }

        drawKvp(ctx, "Width", drawBound, targetW, getY(0), 0)
        drawKvp(ctx, "Height", drawBound, targetH, getY(1), 1)
        tPaint.textAlign = Paint.Align.CENTER
        ctx.drawText("Aspect Ratio : $aspectW:$aspectH", drawBound.centerX(),  getY(2), tPaint, 0.5f)

        var btnY = drawBound.centerY() + 100.0f
        val hSize = tPaint.textSize * 0.75f
        bRect.set(
            drawBound.centerX() - 50.0f, btnY - hSize,
            drawBound.centerX() + 50.0f, btnY + hSize,
        )
        ctx.drawText("Apply", bRect.centerX(), bRect.centerY(), tPaint, 0.5f)
        if(selection == 2){
            DrawExtension.glowOverlay(ctx, bRect, 10, fPaint, true, dTime)
        }

        btnY += hSize * 2
        bRect.set(
            drawBound.centerX() - 100.0f, btnY - hSize,
            drawBound.centerX() + 100.0f, btnY + hSize,
        )
        ctx.drawText("Use Presets", bRect.centerX(), bRect.centerY(), tPaint, 0.5f)
        if(selection == 3){
            DrawExtension.glowOverlay(ctx, bRect, 10, fPaint, true, dTime)
        }
    }

    private fun drawPresets(ctx: Canvas, drawBound: RectF, deltaTime: Float){
        val hSize = tPaint.textSize * 0.75f
        val topY = drawBound.centerY() - (hSize * presetSize.size)
        var cY = topY
        tPaint.textAlign = Paint.Align.CENTER
        presetSize.forEachIndexed { i, it ->
            val tW = Ref(it.w)
            val tH = Ref(it.h)

            genAspectRatio(tW, tH)

            bRect.set(
                drawBound.centerX() - 150.0f, cY - hSize,
                drawBound.centerX() + 150.0f, cY + hSize,
            )

            ctx.drawText("${it.w}x${it.h} (${tW.p}:${tH.p})", bRect.centerX(), bRect.centerY(), tPaint, 0.5f)

            if(selection == i){
                DrawExtension.glowOverlay(ctx, bRect, 10, fPaint, true, dTime)
            }
            cY += hSize * 2.0f
        }

        val btnY = cY
        bRect.set(
            drawBound.centerX() - 100.0f, btnY - hSize,
            drawBound.centerX() + 100.0f, btnY + hSize,
        )
        ctx.drawText("Use Customs", bRect.centerX(), bRect.centerY(), tPaint, 0.5f)
        if(selection == presetSize.size){
            DrawExtension.glowOverlay(ctx, bRect, 10, fPaint, true, dTime)
        }
    }

    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime: Float) {
        dTime += deltaTime
        if(isCustom){
            drawCustom(ctx, drawBound, deltaTime)
        }else{
            drawPresets(ctx, drawBound, deltaTime)
        }
    }

    override fun onClose() {
        icon.recycle()
    }

    override fun onTouch(a: PointF, b: PointF, act: Int) {
        val yD = (a - b).y

        val limit = isCustom.select(3, presetSize.size)

        if(abs(yD) > 50.0f){
            selection = if(yD < 0.0f){
                (selection + 1 )
            }else{
                (selection - 1 )
            }.coerceIn(0, limit)

            b.y += 100.0f
            vsh.xmbView!!.context.xmb.touchStartPointF.set(b)
        }
        super.onTouch(a, b, act)
    }

    override fun onGamepad(key: PadKey, isPress: Boolean): Boolean {
        if(isPress){
            val limit = isCustom.select(3, presetSize.size)
            return when(key){
                PadKey.PadU -> { selection = (selection - 1).coerceIn(0, limit); true; }
                PadKey.PadD -> { selection = (selection + 1).coerceIn(0, limit); true; }
                else -> true
            }
        }
        return false
    }

    private fun openNumberDialog(title:String, value:Int, onFinish: (Int) -> Unit ){
        NativeEditTextDialog(vsh)
            .setTitle(title)
            .setValue(value.toString())
            .setFilter(InputType.TYPE_CLASS_NUMBER)
            .setOnFinish {
                onFinish(it.toInt())
                reCalcAspectSize()
            }
            .show()
    }

    override fun onDialogButton(isPositive: Boolean) {
        if(isPositive){
            if(isCustom){
                when(selection){
                    0 -> openNumberDialog("Set Width", targetW) { targetW = it }
                    1 -> openNumberDialog("Set Height", targetH) { targetH = it }
                    2 ->  vsh.xmbView?.setReferenceScreenSize(targetW, targetH, true) // Apply
                    3 -> {
                        isCustom = !isCustom
                        selection = presetSize.size
                    }
                }
            }else{
                if(selection < presetSize.size){
                    val it = presetSize[selection]
                    vsh.xmbView?.setReferenceScreenSize(it.w, it.h, true)
                }else{
                    isCustom = !isCustom
                    getSize()
                    selection = 3
                }
            }
        }else{
            finish(VshViewPage.MainMenu)
        }
    }
}