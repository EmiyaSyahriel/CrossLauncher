package id.psw.vshlauncher.submodules

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.*
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.*
import id.psw.vshlauncher.types.Ref
import java.io.File
import java.io.FileDescriptor
import java.lang.Exception

class XMBAdaptiveIconRenderer(ctx: VSH) {

    companion object {
        private const val TAG = "XMBIconGen"
        var BaseWidth = 120
        var BaseHeight = 66
        var WIDTH = BaseWidth
        var HEIGHT = BaseHeight
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
            var LegacyScale = 0.0f
            var LegacyXAnchor = 0.5f
            var LegacyYAnchor = 0.5f
        }
    }

    private val pm = ctx.packageManager
    private val fileRoots = ArrayList<File>()

    init {
        val d = ctx.resources.displayMetrics.density
        WIDTH = (BaseWidth * d).toInt()
        HEIGHT = (BaseHeight * d).toInt()
        val mSb = StringBuilder()
        mSb.appendLine("Icon file source : ")
        ctx.getAllPathsFor(VshBaseDirs.APPS_DIR, createParentDir = true).forEach {
            fileRoots.add(it)
            mSb.appendLine(it.absolutePath)
        }
        Log.d(TAG, mSb.toString())
    }

    private fun drawFittedBitmap(c:Canvas, d:Drawable?, scale:Float, xAnchor:Float, yAnchor:Float, drawRect:RectF){
        var b : Bitmap? = null
        if(d != null){
            val fw = WIDTH.toFloat()
            val fh = HEIGHT.toFloat()
            b = if(d.hasSize){
                val fbScale = fitFillSelect(
                    d.intrinsicWidth.toFloat(), fw,
                    d.intrinsicHeight.toFloat(), fh,
                    fw, fh,
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
        val drawRect = emptyRectF
        with(AdaptiveRenderSetting){
            var isBannerUsed = false
            if(banner.hasSize){
                isBannerUsed = banner?.intrinsicHeight != banner?.intrinsicWidth
            }

            if(isBannerUsed){
                if(banner is AdaptiveIconDrawable)
                {
                    drawFittedBitmap(ctx, banner.background, BackScale, BackXAnchor, BackYAnchor, drawRect)
                    drawFittedBitmap(ctx, banner.foreground, ForeScale, ForeXAnchor, ForeYAnchor, drawRect)
                }else if(banner != null && banner.hasSize){
                    drawLegacy(ctx, banner)
                }
            }else{
                if(icon is AdaptiveIconDrawable){
                    drawFittedBitmap(ctx, icon.background, BackScale, BackXAnchor, BackYAnchor, drawRect)
                    drawFittedBitmap(ctx, icon.foreground, ForeScale, ForeXAnchor, ForeYAnchor, drawRect)
                }else{
                    drawLegacy(ctx, icon);
                }
            }
        }
    }

    private val emptyRectF =RectF()
    private fun drawLegacy(ctx:Canvas, legacyIcon:Drawable){
        with(AdaptiveRenderSetting)
        {
            drawFittedBitmap(ctx, legacyIcon, LegacyScale, LegacyXAnchor, LegacyYAnchor, emptyRectF)
        }
    }

    private fun loadCustomIcon(act:ActivityInfo) :Bitmap?{
        fileRoots.forEach {
            val f = it.combine(act.uniqueActivityName, "ICON0.PNG")
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

    fun create(act:ActivityInfo, vsh:VSH) : Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        val canvas = Canvas(bitmap)
        val custom = loadCustomIcon(act)

        if(custom != null){
            bitmap.recycle()
            return custom
        }else{
            var d = act.loadIcon(pm)
            val spResRef = Ref<Resources>(vsh.resources)
            val spResIdRef = Ref(0)
            val apkHasSpecialRes = hasSpecialRes(act, vsh, spResRef, spResIdRef)

            if(apkHasSpecialRes){
                d = ResourcesCompat.getDrawable(spResRef.p, spResIdRef.p, null)
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !apkHasSpecialRes){
                val b = act.loadBanner(pm)
                drawAdaptive(canvas, d, b)
            }else{
                drawLegacy(canvas, d)
            }
        }

        return bitmap
    }

    /**
     * Check if app package contains Cross Launcher-specific icon
     *
     * Developer may add one by putting a drawable with name of "psw_crosslauncher_banner" (R.drawable.psw_crosslauncher_banner)
     * in their app package.
     */
    private fun hasSpecialRes(act: ActivityInfo, vsh: VSH, spResRef: Ref<Resources>, spResIdRef : Ref<Int>): Boolean {
        try{
            spResRef.p = vsh.packageManager.getResourcesForApplication(act.applicationInfo)
            spResIdRef.p = spResRef.p.getIdentifier("psw_crosslauncher_banner", "drawable", act.packageName)
            return spResIdRef.p != 0
        }catch(e:Exception){ }
        return false
    }
}