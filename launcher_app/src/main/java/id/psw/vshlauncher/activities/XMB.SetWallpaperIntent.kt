package id.psw.vshlauncher.activities

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Build
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import id.psw.vshlauncher.*
import id.psw.vshlauncher.submodules.PadKey
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.VshViewPage
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.views.drawBitmap
import id.psw.vshlauncher.views.showDialog

class SetWallpaperDialog(private val vsh: VSH, private val xmb:XMB, private val intent: Intent) : XmbDialogSubview(vsh) {
    override val hasNegativeButton: Boolean = true
    override val hasPositiveButton: Boolean = true
    override val icon: Bitmap get() = XMBItem.TRANSPARENT_BITMAP
    override val title: String get() = vsh.getString(R.string.dlg_set_wallpaper_title)

    private var isInternal = false
    private var imageLoaded = false
    private var image : Bitmap = XMBItem.TRANSPARENT_BITMAP
    private var bmpPaint = Paint().apply {

    }
    private var txtPaint = Paint().apply {
        typeface = FontCollections.masterFont
        textSize = 20.0f
        color = Color.WHITE

        textAlign = Paint.Align.CENTER
    }

    override val positiveButton: String
        get() = vsh.getString(R.string.common_install)
    override val negativeButton: String
        get() = vsh.getString(android.R.string.cancel)

    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime:Float) {
        ctx.drawBitmap(image, null, drawBound, bmpPaint, FittingMode.FILL)
        ctx.drawARGB(128,0,0,0)

        arrayOf("As Launcher Internal Wallpaper","As Device Wallpaper").forEachIndexed{ i, it ->
            val selected = isInternal == (i == 0)
            if(selected){
                txtPaint.setShadowLayer(10.0f, 0.0f, 0.0f, Color.WHITE)
            }else{
                txtPaint.setShadowLayer(0.0f, 0.0f, 0.0f, Color.TRANSPARENT)
            }

            ctx.drawText(it, drawBound.centerX(), drawBound.centerY()+(i * (txtPaint.textSize * 1.25f)), txtPaint)
        }
    }

    private fun checkWallpaperPermission() : Boolean{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val r = vsh.checkSelfPermission(android.Manifest.permission.SET_WALLPAPER) == PackageManager.PERMISSION_GRANTED
            if(!r && ActivityCompat.shouldShowRequestPermissionRationale(xmb, android.Manifest.permission.SET_WALLPAPER)){
                ActivityCompat.requestPermissions(xmb, arrayOf(android.Manifest.permission.SET_WALLPAPER), 12984)
            }
            r
        } else true
    }

    override fun onDialogButton(isPositive: Boolean) {
        if(isPositive){
            if(isInternal){
                // Do Internal Check
            }else{
                if(checkWallpaperPermission()){
                    val wpman = vsh.getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager
                    wpman.setBitmap(image)
                    Thread.sleep(1000L) // Wait for system to save the image into the system
                    finish(VshViewPage.MainMenu)
                }else{
                    vsh.postNotification(null,
                        "Permission not granted",
                        "If you have granted the permission on the dialog previously shown, please use press button for Install once again.", 10.0f)
                }
            }
        }else{
            finish(VshViewPage.MainMenu)
        }
    }

    override fun onClose() {
        if(imageLoaded){
            image.recycle()
        }
    }

    override fun onGamepad(key: PadKey, isPress: Boolean): Boolean {
        if(isPress){
            if(key == PadKey.PadU || key == PadKey.PadD){
                isInternal = !isInternal
                return true
            }
        }
        return super.onGamepad(key, isPress)
    }

    override fun onStart() {
        val uri = intent.clipData?.getItemAt(0)?.uri
        try{
            if(uri != null){
                image = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(vsh.contentResolver, uri))
                }else{
                    MediaStore.Images.Media.getBitmap(vsh.contentResolver, uri)
                }
                imageLoaded = true
            }
        }catch(e:Exception){
            vsh.postNotification(null, "Wallpaper decode failed",e.toString(), 5.0f)
            finish(VshViewPage.MainMenu)
        }

    }

    override fun onTouch(a: PointF, b: PointF, act: Int) {

    }
}

fun XMB.isShareIntent(intent: Intent) : Boolean = intent.action == Intent.ACTION_SEND

fun XMB.showShareIntentDialog(intent:Intent) {
    try{
        xmbView.showDialog(SetWallpaperDialog(vsh, this, intent))
    }catch(e:Exception) {
        vsh.postNotification(null, "Failed to Set Wallpaper",e.toString(), 5.0f)
        xmbView.switchScreen(VshViewPage.MainMenu)
    }
}