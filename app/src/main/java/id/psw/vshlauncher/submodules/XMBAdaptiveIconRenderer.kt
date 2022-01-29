package id.psw.vshlauncher.submodules

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.*
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.combine
import id.psw.vshlauncher.fitFillSelect
import id.psw.vshlauncher.hasSize
import id.psw.vshlauncher.toLerp
import java.io.File
import java.io.FileDescriptor
import java.lang.Exception

class XMBAdaptiveIconRenderer(ctx: Context) {

    companion object {
        private const val TAG = "XMBIconGen"
        private var WIDTH = 320
        private var HEIGHT = 176
        val Width get() = WIDTH
        val Height get() = HEIGHT

        /**
         * Setting for adaptive renderer
         *
         * values:
         * - Scaling: -1 = 0%, 0.0 = Fit, 1.0 = Fill, 2.0 = 200% Fill
         * - Anchor: 0.0 = Left, 0.5 = Center, 1.0 = Right
         */
        object AdaptiveRenderSetting {
            var ForeScale = 1.0f
            var BackScale = 1.0f
            var ForeYAnchor = 0.5f
            var BackYAnchor = 0.5f
            var ForeXAnchor = 0.5f
            var BackXAnchor = 0.5f
            var LegacyScale = 0.5f
            var LegacyXAnchor = 0.5f
            var LegacyYAnchor = 0.5f
        }

        object LegacyRenderSetting{
            var scale = 0.0f
            var xAnchor = 0.5f
            var yAnchor = 0.5f
        }

    }

    private val pm = ctx.packageManager
    private val fileRoots = arrayListOf<File>()

    init {
        val d = ctx.resources.displayMetrics.density
        WIDTH = (320 * d).toInt()
        HEIGHT = (176 * d).toInt()
        val mSb = StringBuilder()
        mSb.appendLine("File source : ")
        ctx.getExternalFilesDirs("").forEach {
            val rootFile = it.combine("dev_hdd0","game")
            mSb.appendLine(rootFile)
            fileRoots.add(rootFile)
        }
        Log.d(TAG, mSb.toString())
    }

    private fun drawFittedBitmap(c:Canvas, d:Drawable?, scale:Float, xAnchor:Float, yAnchor:Float, drawRect:RectF){
        var b : Bitmap? = null
        if(d != null){
            b = if(d.hasSize){
                val fbScale = fitFillSelect(
                    d.intrinsicWidth.toFloat(),
                    WIDTH.toFloat(),
                    d.intrinsicHeight.toFloat(),
                    HEIGHT.toFloat(),
                    WIDTH.toFloat(),
                    HEIGHT.toFloat(),
                    scale
                )
                d.toBitmap(
                    (fbScale * d.intrinsicWidth).toInt(),
                    (fbScale * d.intrinsicHeight).toInt(),
                    Bitmap.Config.ARGB_8888);
            } else {
                d.toBitmap(WIDTH, HEIGHT)
            }
        }
        if(b != null){
            drawRect.left = xAnchor.toLerp(0.0f, WIDTH.toFloat() - b.width )
            drawRect.top = yAnchor.toLerp(0.0f, HEIGHT.toFloat() - b.height)
            drawRect.right = drawRect.left + b.width
            drawRect.bottom = drawRect.top + b.height
            c.drawBitmap(b, null, drawRect, null)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun drawAdaptive(ctx:Canvas, icon : Drawable, banner : Drawable?){
        var fbScale = 1.0f
        val drawRect = RectF()
        with(AdaptiveRenderSetting){
            if(banner is AdaptiveIconDrawable)
            {
                drawFittedBitmap(ctx, banner.background, BackScale, BackXAnchor, BackYAnchor, drawRect)
                drawFittedBitmap(ctx, banner.foreground, ForeScale, ForeXAnchor, ForeYAnchor, drawRect)
            }else if(banner != null && banner.hasSize){
                drawLegacy(ctx, banner)
            }else if(icon is AdaptiveIconDrawable){
                drawFittedBitmap(ctx, icon.background, BackScale, BackXAnchor, BackYAnchor, drawRect)
                drawFittedBitmap(ctx, icon.foreground, ForeScale, ForeXAnchor, ForeYAnchor, drawRect)
            }else{
                drawLegacy(ctx, icon);
            }
        }
    }

    private fun drawLegacy(ctx:Canvas, legacyIcon:Drawable){
        with(AdaptiveRenderSetting)
        {
            drawFittedBitmap(ctx, legacyIcon, LegacyScale, LegacyXAnchor, LegacyYAnchor, RectF())
        }
    }

    private fun loadCustomIcon(act:ActivityInfo) :Bitmap?{
        fileRoots.forEach {
            val f = it.combine(act.name, "ICON0.PNG")
            if(f.exists()){
                try{
                    val b = BitmapFactory.decodeFile(f.absolutePath)
                    if( b != null) return b
                }catch (e:Exception){
                    Log.e(TAG, "Cannot decode custom image : ${f.absolutePath}")
                }
            }
        }
        return null
    }

    fun create(act:ActivityInfo) : Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        val canvas = Canvas(bitmap)
        val custom = loadCustomIcon(act)

        if(custom != null){
            return custom
        }else{
            val d = act.loadIcon(pm)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val b = act.loadBanner(pm)

                if(act.packageName.contains("myPiano")){
                    print("Debug Breakpoint here")
                }

                drawAdaptive(canvas, d, b)
            }else{
                drawLegacy(canvas, d)
            }
        }

        return bitmap
    }
}