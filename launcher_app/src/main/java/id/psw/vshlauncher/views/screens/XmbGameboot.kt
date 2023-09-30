package id.psw.vshlauncher.views.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.RectF
import android.view.MotionEvent
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withScale
import id.psw.vshlauncher.R
import id.psw.vshlauncher.VshBaseDirs
import id.psw.vshlauncher.VshResName
import id.psw.vshlauncher.VshResTypes
import id.psw.vshlauncher.getDrawable
import id.psw.vshlauncher.lerpFactor
import id.psw.vshlauncher.submodules.PadKey
import id.psw.vshlauncher.toLerp
import id.psw.vshlauncher.types.FileQuery
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.GameBootParticleSystem
import id.psw.vshlauncher.views.XmbScreen
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.views.drawBitmap
import id.psw.vshlauncher.vsh
import java.io.File

class XmbGameboot (view : XmbView) : XmbScreen(view) {
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

    private fun colorFilterCache(color:Int): PorterDuffColorFilter{
        return if(cachedPDuffFilter.containsKey(color)){
            cachedPDuffFilter[color]!!
        }else{
            val retval = PorterDuffColorFilter(color, android.graphics.PorterDuff.Mode.MULTIPLY)
            cachedPDuffFilter[color] = retval
            id.psw.vshlauncher.Logger.d(TAG, "Color filter for color 0x${color.toUInt().toString(16)} created and cached")
            retval
        }
    }

    private fun updateParticlePaintColor(color:Int){
        pParticlePaint.color = color
        pParticlePaint.colorFilter = colorFilterCache(color)
    }

    private fun playGameBootSound(){
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

    private fun ensureImageLoaded(){
        if(image == null){

            // Load custom gameboot if exists
            val i = FileQuery(VshBaseDirs.VSH_RESOURCES_DIR)
                    .withNames(VshResName.GAMEBOOT)
                    .withExtensionArray(VshResTypes.IMAGES)
                    .onlyIncludeExists(true)
                    .execute(context.vsh).firstOrNull()
            if(i != null) { image = BitmapFactory.decodeFile(i.absolutePath) }

            // Load default if no custom gameboot can be loaded
            if(image == null){
                image = view.getDrawable(R.drawable.gameboot_internal)?.toBitmap(1280, 720)
            }
        }

        if(pParticleTexture == null){
            pParticleTexture = view.getDrawable(R.drawable.miptex_flakes)?.toBitmap(128, 16)
        }
    }

    override fun start() {
        currentTime = 0.0f
        ensureImageLoaded()
        playGameBootSound()
    }

    fun bootInto(skip:Boolean, bootFunc : () -> Unit){
        this.skip = skip
        onBoot = bootFunc
        view.switchScreen(view.screens.gameBoot)
        M.audio.removeAudioSource()
    }

    private fun bootDirectly(){
        onBoot?.invoke()
        onBoot = null
        view.switchScreen(view.screens.mainMenu)
    }

    private fun drawParticle(ctx: Canvas){
        val pTex = pParticleTexture
        if(pTex != null){
            val fit = scaling.viewport.width().coerceAtMost(scaling.viewport.height())
            particleSystem.update(time.deltaTime)
            particleSystem.particles.forEach{
                updateParticlePaintColor(it.baseColor)
                val tx = (id.psw.vshlauncher.views.GameBootParticleSystem.weighting(it.t) * 8).toInt() * 16
                pTempTexCoord.set(tx, 0, tx + 16, 16)
                val sZ = (it.size * fit) / 2.0f
                val pX = it.x.toLerp(scaling.viewport.left, scaling.viewport.right)
                val pY = it.y.toLerp(scaling.viewport.top, scaling.viewport.bottom)
                pTempPosition.set(pX - sZ, pY - sZ, pX + sZ, pY + sZ)
                ctx.drawBitmap(pTex, pTempTexCoord, pTempPosition, pParticlePaint)
            }
        }
    }
    override fun render(ctx: Canvas){
        ensureImageLoaded()
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

            ctx.drawColor(id.psw.vshlauncher.FColor.setAlpha(android.graphics.Color.BLACK, bgAlpha))
            drawParticle(ctx)
            if(bootImg != null){
                ctx.withScale(
                        baseScale,
                        baseScale,
                        scaling.target.centerX(),
                        scaling.target.centerY()
                ) {
                    imagePaint.alpha = (baseAlpha * 255).toInt()
                    ctx.drawBitmap(bootImg, null, scaling.target, imagePaint, id.psw.vshlauncher.FittingMode.FIT)
                }
            }
        }
    }

    override fun onTouchScreen(start: PointF, current: PointF, action: Int) {
        if(action == MotionEvent.ACTION_DOWN){
            currentTime = 4.0f
        }
    }

    override fun end() {
        currentTime = 0.0f

        // Unload gameboot image
        image?.recycle()
        image = null
    }

    override fun onGamepadInput(key: PadKey, isDown: Boolean): Boolean {
        var retval =false

        if(isDown && (key == PadKey.Cancel || key == PadKey.StaticCancel)){
            view.switchScreen(view.screens.mainMenu)
            retval = true
        }
        return retval
    }
}