package id.psw.vshlauncher.views.screens

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.view.MotionEvent
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.R
import id.psw.vshlauncher.VshBaseDirs
import id.psw.vshlauncher.VshResName
import id.psw.vshlauncher.VshResTypes
import id.psw.vshlauncher.getDrawable
import id.psw.vshlauncher.makeTextPaint
import id.psw.vshlauncher.submodules.PadKey
import id.psw.vshlauncher.types.FileQuery
import id.psw.vshlauncher.views.XmbScreen
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.vsh
import java.io.File

class XmbColdboot(view : XmbView) : XmbScreen(view) {
    var image : Bitmap? = null
    val imagePaint : Paint = Paint().apply{
        alpha = 0
    }
    var isL1Down = false
    val epiwarnPaint = vsh.makeTextPaint(size = 20.0f, color = Color.WHITE)
    var waveSpeed = 1.0f
    var hideEpilepsyWarning = false

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

    override fun onTouchScreen(a: PointF, b: PointF, act: Int) {
        if(act == MotionEvent.ACTION_DOWN){
            // Skip coldboot
            when {
                currentTime <= 5.0f -> currentTime = 5.0f
                currentTime > 5.0f && currentTime <= 10.0f && !hideEpilepsyWarning -> currentTime = 10.0f
            }
        }
    }

    override fun start() {
        currentTime = 0.0f

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