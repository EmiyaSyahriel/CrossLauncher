package id.psw.vshlauncher.views.dialogviews

import android.content.Intent
import android.graphics.*
import android.os.Build
import android.util.Base64
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.*
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.types.XMBShortcutInfo
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.VshViewPage
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.views.drawBitmap
import id.psw.vshlauncher.views.drawText

class InstallShortcutDialogView(private val vsh: VSH, private val intent: Intent) : XmbDialogSubview(vsh) {
    override val title: String
        get() = vsh.getString(R.string.install_shortcut_dialog_title)

    override val hasNegativeButton: Boolean = true
    override val hasPositiveButton: Boolean = true
    override val negativeButton: String = vsh.getString(android.R.string.cancel)
    override val positiveButton: String = vsh.getString(R.string.common_install)

    override val icon: Bitmap = ResourcesCompat.getDrawable(vsh.resources, R.drawable.category_shortcut, null)?.toBitmap(50,50) ?: XMBItem.WHITE_BITMAP

    private val shortcut = XMBShortcutInfo(vsh, intent)

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = FontCollections.masterFont
        textSize = 25.0f
        color = Color.WHITE
    }


    override fun onClose() {
        if(icon != XMBItem.WHITE_BITMAP){
            icon.recycle()
        }
    }

    override fun onDialogButton(isPositive: Boolean) {
        if(isPositive){
            val req = shortcut.pinItem
            if(req != null){
                val id = "${shortcut.packageName}_${shortcut.id}"
                val idb = id.toByteArray(Charsets.UTF_16)
                val b64 = Base64.encode(idb, Base64.DEFAULT)

                val files = vsh.getAllPathsFor(VshBaseDirs.USER_DIR, "shortcuts", "${b64}.ini", createParentDir = true)
                var file = files.find { it.exists() }
                if(file == null){
                    files[0].createNewFile()
                    file = files[0]
                }

                shortcut.write(file)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    req.accept()
                }
                vsh.reloadShortcutList()
            }
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
        ctx.drawBitmap(shortcut.icon, null, tmpRectF, null, FittingMode.FIT, 0.5f, 0.5f)
        var textTop = bTop + 150.0f
        val textCenter = 0.40f.toLerp(drawBound.left, drawBound.right)

        arrayOf(
            "ID" to shortcut.id,
            "Name" to shortcut.name,
            "Long Name" to shortcut.longName,
            "Package" to shortcut.packageName,
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