package id.psw.vshlauncher.submodules

import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.*
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.*
import id.psw.vshlauncher.types.FileQuery
import id.psw.vshlauncher.types.Ref
import java.io.File
import java.lang.Exception

class XMBAdaptiveIconRenderer(private val ctx: VSH) {

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
            var legacyBg = 0
            var legacyBgYouA = 0
            var legacyBgYouB = 0
            var legacyBgColor = 0x7FFFFFFF
            /** Usage : Leftmost 2 bits is highest
             - 00 = Application Icon
             - 01 = Adaptive Application Icon
             - 10 = Application Banner (Usually used by Android TV Launcher)
             - 11 = Adaptive Application Banner
             Highest means if available then use, if not, use the lower
            */
            var iconPriority = 0b01111000
        }
        const val ICON_PRIORITY_TYPE_APP_ICON_LEGACY = 0b00
        const val ICON_PRIORITY_TYPE_APP_ICON_ADAPTIVE = 0b01
        const val ICON_PRIORITY_TYPE_APP_BANNER_LEGACY = 0b10
        const val ICON_PRIORITY_TYPE_APP_BANNER_ADAPTIVE = 0b11

        fun getIconPriorityAt(at:Int) : Int{
            return (AdaptiveRenderSetting.iconPriority shr ((3 - at) * 2)) and 0b11
        }
    }

    private val pm = ctx.packageManager
    private val fileRoots = ArrayList<File>()
    private val materialYouColor = Ref(0)
    private var supportsMaterialYou = getMaterialYouColor(ctx, 0, 0, materialYouColor)

    init {
        val d = ctx.resources.displayMetrics.density
        WIDTH = (BaseWidth * d).toInt()
        HEIGHT = (BaseHeight * d).toInt()
        val mSb = StringBuilder()
        mSb.appendLine("Icon file source : ")
        FileQuery(VshBaseDirs.APPS_DIR).createParentDirectory(true).execute(ctx).forEach {
            fileRoots.add(it)
            mSb.appendLine(it.absolutePath)
        }
        Logger.d(TAG, mSb.toString())
        readPreferences()
    }

    fun readPreferences(){
        AdaptiveRenderSetting.legacyBg = ctx.pref.getInt(PrefEntry.ICON_RENDERER_LEGACY_BACKGROUND, 0)
        AdaptiveRenderSetting.legacyBgColor = ctx.pref.getInt(PrefEntry.ICON_RENDERER_LEGACY_BACK_COLOR, 0x7FFFFFFF)
        val you = ctx.pref.getInt(PrefEntry.ICON_RENDERER_LEGACY_BACK_MATERIAL_YOU, 0)
        AdaptiveRenderSetting.legacyBgYouA = you / 100
        AdaptiveRenderSetting.legacyBgYouB = you % 100
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
        val drawRect = emptyRectF
        with(AdaptiveRenderSetting){
            var isBannerUsed = false
            if(banner.hasSize){
                isBannerUsed = banner?.intrinsicHeight != banner?.intrinsicWidth
            }

            for(i in 0 .. 3){
                when(getIconPriorityAt(i)){
                    ICON_PRIORITY_TYPE_APP_ICON_ADAPTIVE -> {
                        if(icon is AdaptiveIconDrawable){
                            drawFittedBitmap(ctx, icon.background, BackScale, BackXAnchor, BackYAnchor, drawRect)
                            drawFittedBitmap(ctx, icon.foreground, ForeScale, ForeXAnchor, ForeYAnchor, drawRect)
                            return
                        }
                    }
                    ICON_PRIORITY_TYPE_APP_BANNER_LEGACY -> {
                        if(isBannerUsed && banner != null && banner.hasSize){
                            drawLegacy(ctx, banner)
                            return
                        }
                    }
                    ICON_PRIORITY_TYPE_APP_BANNER_ADAPTIVE -> {
                        if(isBannerUsed && banner is AdaptiveIconDrawable)
                        {
                            drawFittedBitmap(ctx, banner.background, BackScale, BackXAnchor, BackYAnchor, drawRect)
                            drawFittedBitmap(ctx, banner.foreground, ForeScale, ForeXAnchor, ForeYAnchor, drawRect)
                            return
                        }
                    }
                    ICON_PRIORITY_TYPE_APP_ICON_LEGACY -> {
                        // Legacy Icon is absolute fallback
                        drawLegacy(ctx, icon)
                        return
                    }
                }
            }
        }
    }

    private val emptyRectF =RectF()
    private fun drawLegacy(ctx:Canvas, legacyIcon:Drawable){
        val that = this.ctx
        with(AdaptiveRenderSetting)
        {
            supportsMaterialYou = getMaterialYouColor(that, legacyBgYouA, legacyBgYouB, materialYouColor)

            val color = (legacyBg == 2 && supportsMaterialYou).select(materialYouColor.p, legacyBgColor)

            if(legacyBg != 0){
                ctx.drawARGB(
                    (Color.alpha(color) shl 1 or 1),
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )
            }

            drawFittedBitmap(ctx, legacyIcon, LegacyScale, LegacyXAnchor, LegacyYAnchor, emptyRectF)
        }
    }

    private fun loadCustomIcon(act:ActivityInfo) :Bitmap?{
        fileRoots.forEach {
            val f = it.combine(act.uniqueActivityName, "ICON0.PNG")
            if(f?.exists() == true){
                try{
                    val b = BitmapFactory.decodeFile(f.absolutePath)
                    if( b != null) return b
                }catch (e:Exception){
                    Logger.e(TAG, "Cannot decode custom image : ${f.absolutePath}")
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