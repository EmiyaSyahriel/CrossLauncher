package id.psw.vshlauncher.views

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.*
import java.io.File

data class VshViewColdBootState(
    var currentTime : Float = 0.0f,
    var image : Bitmap? = null,
    val imagePaint : Paint = Paint().apply{
        alpha = 0
    }
)

fun XmbView.playColdBootSound() {
    var isFound = false
    val vsh = context.vsh
    val vshIterator : (File) -> Unit = { it ->
        if(it.exists() && !isFound){
            isFound = true
            vsh.setSystemAudioSource(it)
        }
    }

    vsh.getAllPathsFor(VshBaseDirs.VSH_RESOURCES_DIR, VshResName.COLDBOOT_SOUND_MP3)
        .forEach { vshIterator(it) }
    vsh.getAllPathsFor(VshBaseDirs.VSH_RESOURCES_DIR, VshResName.COLDBOOT_SOUND_AAC)
        .forEach { vshIterator(it) }
}

fun XmbView.cbStart(){
    state.coldBoot.currentTime = 0.0f
    cbEnsureImageLoaded()
    playColdBootSound()
}

fun XmbView.cbEnsureImageLoaded(){
    if(state.coldBoot.image == null){
        state.coldBoot.image = getDrawable(R.drawable.coldboot_internal)?.toBitmap(1280, 720)
    }
}

fun XmbView.cbRender(ctx: Canvas){
    cbEnsureImageLoaded()
    val img = state.coldBoot.image
    val cTime = state.coldBoot.currentTime
    if(img != null){
        when {
            cTime < 1.0f -> {
                state.coldBoot.imagePaint.alpha = (cTime.toLerp(0f, 255f)).toInt().coerceIn(0,255)
            }
            cTime > 4.0f && cTime < 5.0f -> {
                state.coldBoot.imagePaint.alpha = (cTime.lerpFactor(5.0f, 4.0f) * 255).toInt().coerceIn(0,255)
            }
            cTime > 5.0f -> {
                switchPage(VshViewPage.MainMenu)
            }
            else -> {
                state.coldBoot.imagePaint.alpha = 255
            }
        }

        //ctx.withScale(img.width * s, img.height * s, width * 0.5f, height* 0.5f){
            ctx.drawARGB((state.coldBoot.imagePaint.alpha * 0.75f).toInt(), 0,0,0)
            val that = this
            ctx.drawBitmap(img, null, scaling.target, state.coldBoot.imagePaint, FittingMode.FIT)
            //ctx.drawRect(scaling.target, state.coldBoot.imagePaint)
        //}
    }else{
        switchPage(VshViewPage.MainMenu)
    }
}

fun XmbView.cbEnd(){

}
