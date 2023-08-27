package id.psw.vshlauncher.test_activity

import android.content.Context
import android.content.Intent
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import id.psw.vshlauncher.toLerp
import id.psw.vshlauncher.submodules.XMBAdaptiveIconRenderer
import id.psw.vshlauncher.vsh
import kotlin.concurrent.thread

class XMBAdaptiveIconTestActivity : AppCompatActivity() {
    private var hasLoaded = false
    private var loadProgress = 0.0f
    private var yOffset = 0.0f
    private var lYOffset = 0.0f
    private val appList = mutableMapOf<String, Bitmap>()

    inner class AdaptiveIconTestView(private val app: Context?) : View(app) {
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            this.textSize = 12.0f
        }

        init {
            if(app != null){
                thread(true, name = "AppQuery.thread"){
                    hasLoaded = false
                    loadProgress = 0.0f
                    val apps = app.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }, 0)
                    apps.forEach { it.resolvePackageName = it.loadLabel(app.packageManager).toString() }
                    apps.sortBy { it.resolvePackageName }
                    apps.forEachIndexed { i, it ->
                        val pkg = it.activityInfo.packageName
                        if(pkg != null){
                            val appName = "${it.activityInfo.loadLabel(app.packageManager)} [${it.activityInfo.packageName}]"
                            appList[appName] = vsh.M.icons.create(it.activityInfo, vsh)
                            loadProgress = i / apps.size.toFloat()
                        }
                    }
                    hasLoaded = true
                }
            }
        }

        private var sDrawRect = RectF()
        override fun onDraw(ctx: Canvas?) {
            if(ctx != null){

                lYOffset = (0.1f).toLerp(lYOffset, yOffset)

                ctx.drawARGB(192,0,0,0)

                textPaint.textSize = 20.0f * (app?.resources?.displayMetrics?.scaledDensity ?: 1.0f)
                if(!hasLoaded){
                    textPaint.textAlign = Paint.Align.CENTER
                    ctx.drawText("Loading... ${(loadProgress * 100).toInt()}%", width/2.0f, height/2.0f, textPaint)
                }else{
                    var dHeight = 10.0f
                    appList.keys.forEachIndexed{ _ ,key ->
                        val it = appList[key]
                        if(it != null){
                            dHeight = (it.height + 0.0f).coerceAtLeast(dHeight)
                        }
                    }

                    textPaint.textAlign = Paint.Align.LEFT
                    appList.keys.forEachIndexed { i, key ->
                        val it = appList[key]
                        val y = (i * ((XMBAdaptiveIconRenderer.Height * 0.25f) + 10f)) + lYOffset
                        if(y > -XMBAdaptiveIconRenderer.Height && y < height){
                            sDrawRect.set(
                                10.0f,
                                y,
                                10.0f + ((XMBAdaptiveIconRenderer.Width) * 0.25f),
                                y + ((XMBAdaptiveIconRenderer.Height) * 0.25f)
                            )
                            if(it != null)  ctx.drawBitmap(it, null, sDrawRect, null)
                            ctx.drawText(key, sDrawRect.right + 30.0f, sDrawRect.centerY(), textPaint)
                        }
                    }
                }
                postInvalidate()
            }
        }
    }

    private lateinit var l : AdaptiveIconTestView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.show()
        supportActionBar?.title = "Lister"
        l = AdaptiveIconTestView(this)
        setContentView(l)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        var retval = false
        if(hasLoaded){
            when(keyCode){
                KeyEvent.KEYCODE_DPAD_UP -> {
                    yOffset += 30.0f
                    retval = true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    yOffset -= 30.0f
                    retval = true
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    yOffset += 300.0f
                    retval = true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    yOffset -= 300.0f
                    retval = true
                }
            }
            var maxTop = -appList.size * ((XMBAdaptiveIconRenderer.Height * 0.25f) + 10.0f)
            maxTop += l.height
            yOffset = yOffset.coerceIn(maxTop, 0.0f)
        }
        return retval || super.onKeyDown(keyCode, event)
    }
}