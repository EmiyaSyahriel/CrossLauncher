package id.psw.vshlauncher.views.dialogviews

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.*
import android.os.Build
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.FittingMode
import id.psw.vshlauncher.R
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.submodules.GamepadSubmodule
import id.psw.vshlauncher.toLerp
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.VshViewPage
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.views.drawBitmap
import id.psw.vshlauncher.views.drawText
import id.psw.vshlauncher.submodules.GamepadSubmodule.Key as Key

class InstallShortcutDialogView(private val vsh: VSH, private val intent: Intent) : XmbDialogSubview(vsh) {
    override val title: String
        get() = vsh.getString(R.string.install_shortcut_dialog_title)

    override val hasNegativeButton: Boolean = true
    override val hasPositiveButton: Boolean = true
    override val negativeButton: String = vsh.getString(android.R.string.cancel)
    override val positiveButton: String = vsh.getString(R.string.common_install)

    override val icon: Bitmap = ResourcesCompat.getDrawable(vsh.resources, R.drawable.category_shortcut, null)?.toBitmap(50,50) ?: XMBItem.WHITE_BITMAP

    private lateinit var shortcutIcon : Bitmap
    private var shortcutId : String = "KO.ID!"
    private var shortcutName : String = "No Name"
    private var shortcutNameLong : String = ""
    private var shortcutPackage : String = "com.package.what"
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = FontCollections.masterFont
        textSize = 25.0f
        color = Color.WHITE
    }

    init {
        var isNextGen = false

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if(intent.action!!.equals(LauncherApps.ACTION_CONFIRM_PIN_SHORTCUT, true)){
                val lcher = vsh.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
                val pinItem = lcher.getPinItemRequest(intent)
                val shInfo = pinItem.shortcutInfo
                if(shInfo != null){
                    shortcutIcon = lcher
                        .getShortcutIconDrawable(shInfo, vsh.resources.displayMetrics.densityDpi)
                        .toBitmap(100,100)
                    shortcutId = shInfo.id
                    shortcutName = shInfo.shortLabel?.toString() ?: shortcutName
                    shortcutNameLong = shInfo.longLabel?.toString() ?: shortcutNameLong
                    shortcutPackage = shInfo.activity?.toShortString() ?: shortcutPackage
                    isNextGen = true
                }
            }
        }

        // Use old Android static shortcut installation
        if(!isNextGen){
            if(intent.action!!.equals(Intent.ACTION_CREATE_SHORTCUT, true)){
                shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME) ?: shortcutName
                shortcutIcon = intent.getParcelableExtra<Bitmap>(Intent.EXTRA_SHORTCUT_ICON) ?: XMBItem.WHITE_BITMAP
                shortcutPackage = intent.component?.toShortString() ?: shortcutPackage
            }
        }

    }

    override fun onClose() {
        if(icon != XMBItem.WHITE_BITMAP){
            icon.recycle()
        }
        if(shortcutIcon != XMBItem.WHITE_BITMAP){
            shortcutIcon.recycle()
        }
    }

    override fun onDialogButton(isPositive: Boolean) {
        if(isPositive){
            vsh.installShortcut(intent)
        }
        finish(VshViewPage.MainMenu)
    }

    private val tmpRectF = RectF()
    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime:Float) {
        val bTop = drawBound.top + 100.0f
        tmpRectF.set(
            drawBound.centerX() - 50.0f,
            bTop + 25.0f,
            drawBound.centerX() + 50.0f,
            bTop + 125.0f
        )
        ctx.drawBitmap(shortcutIcon, null, tmpRectF, null, FittingMode.FIT, 0.5f, 0.5f)
        var textTop = bTop + 150.0f
        val textCenter = 0.40f.toLerp(drawBound.left, drawBound.right)

        arrayOf(
            "ID" to shortcutId,
            "Name" to shortcutName,
            "Long Name" to shortcutNameLong,
            "Package" to shortcutPackage,
        ).forEach{
           if(it.second.isNotEmpty()){
               textPaint.textAlign = Paint.Align.RIGHT
                ctx.drawText(it.first, textCenter - 10.0f, textTop, textPaint, 0.5f)
               textPaint.textAlign = Paint.Align.LEFT
               ctx.drawText(it.second, textCenter + 10.0f, textTop, textPaint, 0.5f)

               textTop += textPaint.textSize
           }
        }
        textPaint.textAlign = Paint.Align.CENTER
        ctx.drawText("Install this shortcut to Cross Launcher?",
            drawBound.centerX(),
            textTop + textPaint.textSize,
            textPaint,
            0.5f)
    }
}