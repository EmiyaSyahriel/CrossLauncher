package id.psw.vshlauncher.views.dialogviews

import android.content.Intent
import android.graphics.*
import android.text.TextPaint
import id.psw.vshlauncher.*
import id.psw.vshlauncher.types.XPKGFile
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.VshViewPage
import id.psw.vshlauncher.views.XmbDialogSubview
import java.io.File
import java.util.zip.ZipFile

class InstallPackageDialogView(private val vsh: VSH, private val intent: Intent) : XmbDialogSubview(vsh) {
    override val icon: Bitmap
        get() = vsh.loadTexture(R.drawable.ic_folder)

    override val hasNegativeButton: Boolean
        get() = true

    override val title: String
        get() = vsh.getString(R.string.settings_install_package)

    private val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = FontCollections.masterFont
        textSize = 15.0f
        color = Color.WHITE
    }
    private val ip = Paint(Paint.ANTI_ALIAS_FLAG)

    private var valid = false
    private var loading = true
    private val xpkgLoadProgress : Float get()= xpkg?.loadProgress ?: 0.0f
    private var xpkg : XPKGFile? = null
    private lateinit var tmpFile : File

    init {
        vsh.threadPool.execute {
            val handle = vsh.addLoadHandle()
            loading = true
            val dat = intent.data
            if(dat != null){
                if(dat.path?.endsWith(".xpkg") == true){
                    valid = true
                }
            }

            if(valid){
                try{
                    val uri = dat!!
                    val iff = vsh.contentResolver.openInputStream(uri)!!
                    tmpFile = File.createTempFile("tmp_pkg_", ".xpkg")
                    val tof = tmpFile.outputStream()

                    val arr = ByteArray(1024)
                    var read = 0
                    while(read > 0){
                        read = iff.read(arr)
                        if(read > 0){
                            tof.write(arr, 0, read)
                            tof.flush()
                        }
                    }
                    iff.close()
                    tof.close()

                    val zipf = ZipFile(tmpFile, ZipFile.OPEN_READ)
                    xpkg = XPKGFile(zipf)

                }catch(e:Exception){
                    valid = false
                }
            }

            loading = false
            vsh.setLoadingFinished(handle)
        }
    }

    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime: Float) {
        if(loading){
            ctx.drawText("Loading package file", drawBound.centerX(), drawBound.centerY() - 20.0f, tp)
            SubDialogUI.progressBar(ctx, 0.0f, 1.0f, xpkgLoadProgress, drawBound.centerX() - 100.0f, drawBound.centerY(), 200.0f, 20.0f, Paint.Align.LEFT)
        }
        super.onDraw(ctx, drawBound, deltaTime)
    }

    override fun onDialogButton(isPositive: Boolean) {
        if(!isPositive){
            finish(VshViewPage.MainMenu)
        }
        super.onDialogButton(isPositive)
    }
}