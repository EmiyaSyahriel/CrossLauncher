package id.psw.vshlauncher.views.dialogviews

import android.app.ProgressDialog.show
import android.graphics.*
import android.os.Build
import android.text.TextPaint
import android.view.MotionEvent
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.minus
import androidx.core.graphics.withRotation
import id.psw.vshlauncher.*
import id.psw.vshlauncher.submodules.GamepadSubmodule
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.types.items.XMBAppItem
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.VshViewPage
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.views.drawBitmap
import id.psw.vshlauncher.views.nativedlg.NativeEditTextDialog
import kotlin.math.abs

class AppInfoDialogView(private val vsh: VSH, private val app : XMBAppItem) : XmbDialogSubview(vsh) {
    companion object {
        const val POS_NAME = 0
        const val POS_DESC = 2
        const val POS_ALBUM = 3
        const val POS_HIDDEN = 4
        const val POS_CATEGORY = 5
        const val POS_OPEN_IN_SYSTEM = 9
        const val TRANSITE_TIME = 0.125f

        private val bmpRectF = RectF()
        private val selRectF = RectF()
        private val szBufRectF = RectF()
        private val validSelections = arrayOf(POS_NAME, POS_DESC, POS_ALBUM, POS_HIDDEN, POS_CATEGORY, POS_OPEN_IN_SYSTEM)
    }

    override val hasNegativeButton: Boolean = true
    override val hasPositiveButton: Boolean get() = true
    override val title: String
        get() = vsh.getString(R.string.view_app_info)

    private var cursorPos = 0
    private var transiteTime = 0.0f
    private lateinit var loadIcon : Bitmap
    private var tPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 20.0f
        typeface = FontCollections.masterFont
    }

    private var iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var rectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply{
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2.0f
    }

    private var _icon =vsh.loadTexture(R.drawable.icon_info, "dialog_icon_app_info", true)

    override val icon: Bitmap
        get() = _icon

    override fun onClose() {
        if(_icon != XMBItem.WHITE_BITMAP)  _icon.recycle()
        if(loadIcon != XMBItem.WHITE_BITMAP)  loadIcon.recycle()
    }

    override val negativeButton: String
        get() = vsh.getString(R.string.common_back)

    override val positiveButton: String
        get() = when(cursorPos) {
            3 -> vsh.getString(R.string.common_toggle)
            5 -> vsh.getString(R.string.category_settings)
            else -> vsh.getString(R.string.common_edit)
        }

    override fun onStart() {
        loadIcon = ResourcesCompat.getDrawable(vsh.resources,R.drawable.ic_sync_loading,null)?.toBitmap(256,256) ?: XMBItem.WHITE_BITMAP
        super.onStart()
    }

    private fun drawLoading(ctx:Canvas){
        val time = vsh.xmbView?.time?.currentTime ?: (System.currentTimeMillis() / 1000.0f)
            ctx.withRotation(
                ((time + 0.375f) * -360.0f) % 360.0f, bmpRectF.centerX(), bmpRectF.centerY()) {
                ctx.drawBitmap(loadIcon, null, bmpRectF, iconPaint, FittingMode.FIT, 0.5f, 0.5f)
            }

    }

    private val drawBound = RectF()

    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime: Float) {
        this.drawBound.set(drawBound)

        if(transiteTime < TRANSITE_TIME){
            transiteTime += vsh.xmbView?.time?.deltaTime ?: 0.015f
        }
        transiteTime = transiteTime.coerceIn(0.0f, TRANSITE_TIME)

        val t = transiteTime / TRANSITE_TIME

        val cSizeY = 132.0f
        val sizeX = t.toLerp(320.0f, 240.0f)
        val sizeY = t.toLerp(176.0f, cSizeY)

        val hSizeX = sizeX / 2.0f
        val hSizeY = sizeY / 2.0f

        val iconCX = t.toLerp(
            (0.3f).toLerp(drawBound.left, drawBound.right),
            drawBound.centerX())
        val iconCY = t.toLerp(
            drawBound.centerY(),
            drawBound.top + 20.0f + hSizeY
        )

        bmpRectF.set(
            iconCX - hSizeX,
            iconCY - hSizeY,
            iconCX + hSizeX,
            iconCY + hSizeY
        )

        if(app.hasAnimatedIcon){
            if(app.isAnimatedIconLoaded){
                val tt = vsh.xmbView?.time?.deltaTime ?: 0.015f
                ctx.drawBitmap(app.animatedIcon.getFrame(tt), null, bmpRectF, iconPaint, FittingMode.FIT, 0.5f, 0.5f)
            }else{
                drawLoading(ctx)
            }
        }else if(app.hasIcon){
            if(app.isIconLoaded){
                ctx.drawBitmap(app.icon, null, bmpRectF, iconPaint, FittingMode.FIT, 0.5f, 0.5f)
            }else{
                drawLoading(ctx)
            }
        }

        var sY = drawBound.top + cSizeY + 50.0f
        val cX = drawBound.centerX() - 100.0f

        var i = 0
        mapOf<Int, String>(
            R.string.dlg_info_name to app.displayName,
            R.string.dlg_info_pkg_name to app.packageName,
            R.string.dlg_info_desc to app.appCustomDesc.ifEmpty { "-" },
            R.string.dlg_info_album to app.appAlbum.ifEmpty { "-" },
            R.string.dlg_info_hidden to vsh.getString(app.isHiddenByCfg.select(R.string.common_yes, R.string.common_no)),
            R.string.dlg_info_category to app.appCategory.ifEmpty { "-" },
            R.string.dlg_info_update to app.displayUpdateTime,
            R.string.dlg_info_apk_size to app.fileSize,
            R.string.dlg_info_version to app.version
        ).forEach { l ->
            tPaint.textAlign = Paint.Align.RIGHT
            ctx.drawText(vsh.getString(l.key), cX, sY, tPaint)
            val str = l.value

            val isSelected = validSelections[cursorPos] == i

            if(isSelected){
                val w = tPaint.measureText(str)

                selRectF.set(cX + 20.0f, sY - tPaint.textSize , cX + 50.0f + w, sY+ 5.0f)
                ctx.drawRoundRect(selRectF, 5.0f, 5.0f, rectPaint)
            }

            tPaint.textAlign = Paint.Align.LEFT
            ctx.drawText(str, cX + 30.0f, sY, tPaint)
            sY += tPaint.textSize * 1.2f
            i++
        }

        tPaint.textAlign = Paint.Align.CENTER
        if(cursorPos == 5){
            val ccx = drawBound.centerX()
            selRectF.set(ccx - 300.0f, sY - tPaint.textSize + 20.0f, ccx + 300.0f, sY+ 5.0f + 20.0f)
            ctx.drawRoundRect(selRectF, 5.0f, 5.0f, rectPaint)
        }
        ctx.drawText(vsh.getString(R.string.app_info_by_system), drawBound.centerX(), sY + 20.0f, tPaint)
    }

    override fun onGamepad(key: GamepadSubmodule.Key, isPress: Boolean): Boolean {
        when(key){
            GamepadSubmodule.Key.PadU -> {
                if(isPress){
                    cursorPos--
                    cursorPos = cursorPos.coerceIn(0, validSelections.size-1)
                    return true
                }
            }
            GamepadSubmodule.Key.PadD -> {
                if(isPress){
                    cursorPos ++
                    cursorPos = cursorPos.coerceIn(0, validSelections.size-1)
                    return true
                }
            }
            else -> {

            }
        }

        return super.onGamepad(key, isPress)
    }

    private var touchHasMove = false

    override fun onTouch(a: PointF, b: PointF, act: Int) {
        if(act == MotionEvent.ACTION_MOVE){
            val diff = a.y - b.y
            if(abs(diff) > 50.0f){
                if(diff > 0.0f){
                    cursorPos--
                }else{
                    cursorPos++
                }

                b.y += 100.0f
                touchHasMove = true
                vsh.xmbView!!.context.xmb.touchStartPointF.set(b)
                cursorPos = cursorPos.coerceIn(0, validSelections.size - 1)
            }
        }else if(act == MotionEvent.ACTION_UP){
            if(!touchHasMove){
                if(a.y > drawBound.height() * 0.6f){
                    cursorPos++
                }else if(a.y < drawBound.height() * 0.3f){
                    cursorPos--
                }
                cursorPos = cursorPos.coerceIn(0, validSelections.size - 1)
            }
            touchHasMove = false
        }else if(act == MotionEvent.ACTION_DOWN){
            touchHasMove = false
        }
        super.onTouch(a, b, act)
    }

    override fun onDialogButton(isPositive: Boolean) {
        if(isPositive){
            when(cursorPos){
                0 -> {
                    // Rename
                    NativeEditTextDialog(vsh)
                        .setTitle(vsh.getString(R.string.dlg_info_rename))
                        .setOnFinish {
                            app.appCustomLabel = it
                        }
                        .setValue(app.displayName)
                        .show()
                }
                1 -> {
                    NativeEditTextDialog(vsh)
                        .setTitle(vsh.getString(R.string.dlg_info_redesc))
                        .setOnFinish {
                            app.appCustomDesc = it
                        }
                        .setValue(app.appCustomDesc)
                        .show()
                }
                2 -> {
                    NativeEditTextDialog(vsh)
                        .setTitle(vsh.getString(R.string.dlg_info_album))
                        .setOnFinish {
                            app.appAlbum = it
                        }
                        .setValue(app.appAlbum)
                        .show()
                }
                3 -> {
                    app.hide(!app.isHiddenByCfg)
                    // Change is Hidden
                }
                4 -> {
                    // Set Category
                    NativeEditTextDialog(vsh)
                        .setTitle(vsh.getString(R.string.dlg_info_album))
                        .setOnFinish {
                            app.appCategory = it
                        }
                        .setValue(app.appCategory)
                        .show()
                }
                5 -> {
                    // Show in Android
                    vsh.showAppInfo(app)
                }
            }
        }

        if(!isPositive) finish(VshViewPage.MainMenu)
    }
}