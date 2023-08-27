package id.psw.vshlauncher.views

import android.graphics.*
import android.view.MotionEvent
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withScale
import id.psw.vshlauncher.*
import id.psw.vshlauncher.submodules.GamepadSubmodule
import id.psw.vshlauncher.submodules.PadKey
import id.psw.vshlauncher.types.FileQuery
import id.psw.vshlauncher.typography.FontCollections
import java.io.File
import java.nio.file.Files.exists

class VshViewGameBootState {
    var currentTime : Float = 0.0f
    var image : Bitmap? = null
    val imagePaint : Paint = Paint().apply{
        alpha = 0
        typeface = FontCollections.masterFont
    }
    var skip = false
    var defaultSkip = false
    var onBoot : (() -> Unit)? = null
    val particleSystem = GameBootParticleSystem().apply { init(256) }
    val drawDebug = true
    val pParticlePaint = Paint()
    var pParticleTexture : Bitmap? = null
    val pTempTexCoord = Rect()
    val pTempPosition = RectF()
    val cachedPDuffFilter = mutableMapOf<Int, PorterDuffColorFilter>()
    val TAG = "xmb.gameboot"
}

private fun XmbView.gbColorFilterCache(color:Int): PorterDuffColorFilter{
    with(state.gameBoot){
        return if(cachedPDuffFilter.containsKey(color)){
            cachedPDuffFilter[color]!!
        }else{
            val retval = PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)
            cachedPDuffFilter[color] = retval
            Logger.d(TAG, "Color filter for color 0x${color.toUInt().toString(16)} created and cached")
            retval
        }
    }
}

private fun XmbView.gbUpdateParticlePaintColor(color:Int){
    with(state.gameBoot){
        pParticlePaint.color = color
        pParticlePaint.colorFilter = gbColorFilterCache(color)
    }
}

fun XmbView.playGameBootSound(){
    var isFound = false
    val vsh = context.vsh
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

fun XmbView.gbStart(){
    state.gameBoot.currentTime = 0.0f
    gbEnsureImageLoaded()
    playGameBootSound()
}

private fun XmbView.gbEnsureImageLoaded(){
    if(state.gameBoot.image == null){

        // Load custom gameboot if exists
        val i = FileQuery(VshBaseDirs.VSH_RESOURCES_DIR)
            .withNames(VshResName.GAMEBOOT)
            .withExtensionArray(VshResTypes.IMAGES)
            .onlyIncludeExists(true)
            .execute(context.vsh).firstOrNull()
        if(i != null) { state.gameBoot.image = BitmapFactory.decodeFile(i.absolutePath) }

        // Load default if no custom gameboot can be loaded
        if(state.gameBoot.image == null){
            state.gameBoot.image = getDrawable(R.drawable.gameboot_internal)?.toBitmap(1280, 720)
        }
    }

    if(state.gameBoot.pParticleTexture == null){
        state.gameBoot.pParticleTexture = getDrawable(R.drawable.miptex_flakes)?.toBitmap(128, 16)
    }
}

fun XmbView.bootInto(skip:Boolean, bootFunc : () -> Unit){
    state.gameBoot.skip = skip
    state.gameBoot.onBoot = bootFunc
    switchPage(VshViewPage.GameBoot)
    M.audio.removeAudioSource()
}

private fun XmbView.bootDirectly(){
    with(state.gameBoot){
        onBoot?.invoke()
        onBoot = null
        switchPage(VshViewPage.MainMenu)
    }
}

private fun XmbView.drawParticle(ctx:Canvas){
    with(state.gameBoot){
        val pTex = pParticleTexture
        if(pTex != null){
            val fit = scaling.viewport.width().coerceAtMost(scaling.viewport.height())
            particleSystem.update(time.deltaTime)
            particleSystem.particles.forEach{
                gbUpdateParticlePaintColor(it.baseColor)
                val tx = (GameBootParticleSystem.weighting(it.t) * 8).toInt() * 16
                pTempTexCoord.set(tx, 0, tx + 16, 16)
                val sZ = (it.size * fit) / 2.0f
                val pX = it.x.toLerp(scaling.viewport.left, scaling.viewport.right)
                val pY = it.y.toLerp(scaling.viewport.top, scaling.viewport.bottom)
                pTempPosition.set(pX - sZ, pY - sZ, pX + sZ, pY + sZ)
                ctx.drawBitmap(pTex, pTempTexCoord, pTempPosition, pParticlePaint)
            }
        }
    }
}

fun XmbView.gbRender(ctx: Canvas){
    gbEnsureImageLoaded()
    with(state.gameBoot){
        if(skip || defaultSkip){
            bootDirectly()
        }else{
            val bootImg = image
            var baseScale = 0.0f
            val bgAlpha = (currentTime / 0.5f).coerceIn(0.0f, 1.0f)
            var baseAlpha = 1.0f

            when {
                currentTime < 0.5f -> {
                    val s = (currentTime / 0.5f).coerceIn(0.0f, 1.0f).toLerp(0.0f,1.0f)
                    baseScale = s.toLerp(0.5f, 1.0f)
                    baseAlpha = s
                }
                currentTime > 0.5f && currentTime < 2.7f ->{
                    baseScale = 1.0f
                    baseAlpha = 1.0f
                }
                currentTime > 2.7f && currentTime < 3.0f -> {
                    val s = currentTime.lerpFactor(3.0f, 2.7f)
                    baseScale = s.toLerp(3.0f, 1.0f)
                    baseAlpha = s
                }
                currentTime > 3.0f -> {
                    if(drawDebug){
                        bootDirectly()
                    }else{
                        bootDirectly()
                    }
                    baseAlpha = 0.0f
                    baseScale = 0.0f
                }
            }

            ctx.drawColor(FColor.setAlpha(Color.BLACK, bgAlpha))
            drawParticle(ctx)
            if(bootImg != null){
                ctx.withScale(
                    baseScale,
                    baseScale,
                    scaling.target.centerX(),
                    scaling.target.centerY()
                ) {
                    imagePaint.alpha = (baseAlpha * 255).toInt()
                    ctx.drawBitmap(bootImg, null, scaling.target, imagePaint, FittingMode.FIT)
                }
            }
        }
    }
}

fun XmbView.gbOnTouchScreen(a:PointF, b:PointF, act:Int){
    if(act == MotionEvent.ACTION_DOWN){
        with(state.gameBoot){
            currentTime = 4.0f
        }
    }
}

fun XmbView.gbEnd(){
    state.gameBoot.currentTime = 0.0f

    // Unload gameboot image
    state.gameBoot.image?.recycle()
    state.gameBoot.image = null
}

fun XmbView.gbOnGamepad(k: PadKey, isPress:Boolean) : Boolean {
    var retval =false

    with(state.gameBoot){
        if(isPress && (k == PadKey.Cancel || k == PadKey.StaticCancel)){
            switchPage(VshViewPage.MainMenu)
            retval = true
        }
    }

    return retval
}
