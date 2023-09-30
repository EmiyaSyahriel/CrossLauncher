package id.psw.vshlauncher.views.screens

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.view.MotionEvent
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.FittingMode
import id.psw.vshlauncher.R
import id.psw.vshlauncher.VshBaseDirs
import id.psw.vshlauncher.VshResName
import id.psw.vshlauncher.VshResTypes
import id.psw.vshlauncher.getDrawable
import id.psw.vshlauncher.lerpFactor
import id.psw.vshlauncher.livewallpaper.NativeGL
import id.psw.vshlauncher.livewallpaper.XMBWaveSurfaceView
import id.psw.vshlauncher.makeTextPaint
import id.psw.vshlauncher.submodules.PadKey
import id.psw.vshlauncher.toLerp
import id.psw.vshlauncher.types.FileQuery
import id.psw.vshlauncher.views.XmbScreen
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.views.drawBitmap
import id.psw.vshlauncher.views.drawText
import id.psw.vshlauncher.views.wrapText
import id.psw.vshlauncher.vsh
import java.io.File

class XmbColdboot(view : XmbView) : XmbScreen(view) {
    private var image : Bitmap? = null
    private val imagePaint : Paint = Paint().apply{
        alpha = 0
    }
    private var isL1Down = false
    private val epiwarnPaint = vsh.makeTextPaint(size = 20.0f, color = Color.WHITE)
    private var waveSpeed = 1.0f
    var hideEpilepsyWarning = false
    var transition = 0.0f

    private fun playColdBootSound(){
        var isFound = false
        val vsh = view.context.vsh
        val vshIterator : (File) -> Unit = { it ->
            if(it.exists() && !isFound){
                isFound = true
                M.audio.setSystemAudioSource(it)
            }
        }

        FileQuery(VshBaseDirs.VSH_RESOURCES_DIR)
                .withNames(VshResName.GAMEBOOT)
                .withExtensionArray(VshResTypes.SOUNDS)
                .execute(vsh)
                .forEach(vshIterator)
    }


    private fun ensureImageLoaded(){
        if(image == null){

            // Load custom coldboot if exists
            val i = FileQuery(VshBaseDirs.VSH_RESOURCES_DIR)
                    .withNames(VshResName.GAMEBOOT)
                    .withExtensionArray(VshResTypes.IMAGES)
                    .onlyIncludeExists(true)
                    .execute(context.vsh).firstOrNull()
            if(i != null) { image = BitmapFactory.decodeFile(i.absolutePath) }

            // Load default if no custom coldboot can be loaded
            if(image == null) {
                image = view.getDrawable(R.drawable.coldboot_internal)?.toBitmap(1280, 720)
            }
        }
    }

    override fun onTouchScreen(start: PointF, current: PointF, action: Int) {
        if(action == MotionEvent.ACTION_DOWN){
            // Skip coldboot
            when {
                currentTime <= 5.0f -> currentTime = 5.0f
                currentTime > 5.0f && currentTime <= 10.0f && !hideEpilepsyWarning -> currentTime = 10.0f
            }
        }
    }

    override fun render(ctx: Canvas) {
        ensureImageLoaded()
        NativeGL.setSpeed(0.0f)
        NativeGL.setVerticalScale(0.0f)

        val img = image
        val cTime = currentTime
        if (cTime < 5.0f && img != null) {
            imagePaint.alpha = when {
                cTime < 1.0f -> {
                    (cTime.toLerp(0f, 255f)).toInt().coerceIn(0, 255)
                }
                cTime > 4.0f -> {
                    (cTime.lerpFactor(5.0f, 4.0f) * 255).toInt().coerceIn(0, 255)
                }
                else -> 255
            }

            ctx.drawARGB((imagePaint.alpha * 0.75f).toInt(), 0, 0, 0)
            ctx.drawBitmap(img, null, scaling.target, imagePaint, FittingMode.FIT)
        } else if (cTime > 5.0f && cTime < 10.0f && !hideEpilepsyWarning) {
            epiwarnPaint.alpha = when {
                cTime < 6.0f -> {
                    (cTime.lerpFactor(5.0f, 6.0f) * 255).toInt().coerceIn(0, 255)
                }
                cTime > 9.0f -> {
                    (cTime.lerpFactor(10.0f, 9.0f) * 255).toInt().coerceIn(0, 255)
                }
                else -> 255
            }

            // if(cTime > 5.0f && cTime < 10.0f){
            //     NativeGL.setSpeed(state.coldBoot.waveSpeed * cTime.lerpFactor(5.0f, 10.0f).toLerp(10.0f, -10.0f).coerceIn(1.0f, 10.0f));
            // }

            ctx.drawARGB((epiwarnPaint.alpha * 0.75f).toInt(), 0, 0, 0)
            val lines =
                epiwarnPaint.wrapText(context.getString(R.string.photoepilepsy_warning), scaling.target.width() - 300.0f).lines()
            val lCount = lines.size
            val centerY = scaling.target.centerY()
            val hCount = lines.maxOf {
                val arr = FloatArray(it.length)
                epiwarnPaint.getTextWidths(it, arr)
                arr.sum()
            }
            val centerX = scaling.target.centerX()
            val xPos = centerX - (hCount * 0.5f)

            lines.forEachIndexed { i, it ->
                ctx.drawText(it, xPos, centerY + ((i - (lCount * 0.5f)) * epiwarnPaint.textSize), epiwarnPaint, 0.5f)
            }
        } else {
            view.switchScreen(view.screens.mainMenu)
        }
    }

    override fun start() {
        currentTime = 0.0f
        ensureImageLoaded()
        playColdBootSound()
        transition = 1.0f
        val pref = context.vsh.getSharedPreferences(XMBWaveSurfaceView.PREF_NAME, Context.MODE_PRIVATE)

        waveSpeed = pref.getFloat(XMBWaveSurfaceView.KEY_SPEED, 1.0f)
        NativeGL.setSpeed(0.0f)
        NativeGL.setVerticalScale(0.0f)
    }

    override fun end(){
        image?.recycle()
        image = null
    }

    override fun onGamepadInput(key: PadKey, isDown: Boolean): Boolean {
        var retval =false

        if(isDown){
            when(key){
                PadKey.Confirm, PadKey.StaticConfirm -> {
                    if(currentTime <= 5.0f) currentTime = 5.0f
                    else if(currentTime <= 10.0f) currentTime = 10.0f
                    retval = true
                }
                PadKey.Cross -> {
                    if(isL1Down){
                        (context as Activity).finish() // L1 + Cross = Finish Activity
                    }
                }
                else -> {}
            }
        }

        if(key == PadKey.L1){
            isL1Down = isDown
        }

        return retval
    }
}