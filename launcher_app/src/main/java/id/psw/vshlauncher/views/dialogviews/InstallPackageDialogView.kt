package id.psw.vshlauncher.views.dialogviews

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextPaint
import androidx.core.database.getStringOrNull
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.*
import id.psw.vshlauncher.types.Ref
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.types.XPKGFile
import id.psw.vshlauncher.types.items.XMBAppItem
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.*
import kotlinx.coroutines.sync.Mutex
import java.io.File
import java.util.zip.ZipFile
import kotlin.math.min


@SuppressLint("UseCompatLoadingForDrawables")
class InstallPackageDialogView(private val vsh: VSH, private val intent: Intent) : XmbDialogSubview(vsh) {

    companion object {
        var storageDestination = File("")

        private val validAppFileSuffices = arrayOf (
            "/PIC0.PNG",
            "/PIC0.JPG",
            "/PIC1.PNG",
            "/PIC1.JPG",
            "/PIC0_P.PNG",
            "/PIC0_P.JPG",
            "/PIC1_P.PNG",
            "/PIC1_P.JPG",
            "/ICON0.PNG",
            "/ICON0.JPG",
            "/ICON1.APNG",
            "/ICON1.WEBP",
            "/ICON1.MP4",
            "/ICON1.GIF",
            "/SND0.MP3",
            "/SND0.AAC",
            "/SND0.MID",
            "/SND0.MIDI",
        )

    }

    override val icon: Bitmap
        get() = vsh.loadTexture(R.drawable.ic_folder, "icon_folder", true)

    override val hasNegativeButton: Boolean
        get() = status.canShowButton

    override val hasPositiveButton: Boolean
        get() = status.canShowButton && !status.isFinished

    override val positiveButton: String
        get() = vsh.getString(R.string.common_install)

    override val negativeButton: String
        get() = vsh.getString(status.isFinished.select(R.string.common_back, android.R.string.cancel))

    private var installingForSystem = false

    override val title: String
        get() = vsh.getString(R.string.settings_install_package)

    private val tBig = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = FontCollections.masterFont
        textSize = 25.0f
        color = Color.WHITE
    }

    private val tSmall = TextPaint(tBig).apply {
        textSize = 20.0f
    }

    private enum class Validity {
        Loading,
        Valid,
        TypeMismatch,
        Corrupted
    }

    private enum class Status {
        Preload,
        UriVerification,
        CopyPackage,
        ReadPackage,
        LoadAsset,
        LoadFailed,
        PackageNotInstalled,
        Confirmation,
        Installing,
        InstallSuccess,
        InstallFailed;

        val isLoading get() =       this == UriVerification || this == CopyPackage    || this == ReadPackage    || this == LoadAsset
        val canShowButton get() =   this == LoadFailed      || this == Confirmation   || this == InstallSuccess || this == InstallFailed  || this == PackageNotInstalled
        val isFinished get() =      this == InstallSuccess  || this == InstallFailed
    }

    private val ip = Paint(Paint.ANTI_ALIAS_FLAG)

    private var valid : Validity = Validity.Loading
    private val xpkgLoadProgress : Float get()= xpkg?.loadProgress ?: 0.0f
    private var xpkgCopyProgress = 0.0f
    private var xpkg : XPKGFile? = null
    private lateinit var tmpFile : File
    private var fileSize = 0L
    private var filePath = "/dev_emmc0/packages/mirishita.xpkg"
    private var pkgIcon : Bitmap
    private val rectFBuf = RectF()
    private var status = Status.Preload

    private val installInfoLock = Mutex(false)
    private var installProgress = 0.0f
    private var installInfo = ""

    init {
        pkgIcon = vsh.resources.getDrawable(R.drawable.miptex_icon_unknown).toBitmap(320, 176)

        clearPackageCache()

        vsh.threadPool.execute {
            status = Status.Preload

            tmpFile = File(filePath)
            val handle = vsh.addLoadHandle()

            status = Status.UriVerification
            valid = Validity.Loading
            val dat = intent.data
            if(dat != null){
                if(dat.path?.endsWith(".xpkg") != true){
                    valid = Validity.TypeMismatch
                    status = Status.LoadFailed
                    return@execute
                }
            }

            filePath = dat?.path ?: filePath
            val fileNameRef = Ref("")
            filePath = tryGetFileName(dat, fileNameRef).select(fileNameRef.p, filePath)

            if(valid == Validity.Loading){
                try{
                    status = Status.CopyPackage
                    val uri = dat!!
                    val iff = vsh.contentResolver.openInputStream(uri)!!
                    val len : Long = if(Build.VERSION.SDK_INT >= 29){
                        val file = vsh.contentResolver.openFile(uri, "r", null)
                        val len = file!!.statSize
                        file.close()
                        len
                    }else{
                        iff.available().toLong()
                    }
                    tmpFile = File.createTempFile("tmp_pkg_", ".xpkg", vsh.externalCacheDir)
                    val tof = tmpFile.outputStream()

                    val arr = ByteArray(1024)
                    var read = 0
                    var tRead = 0.0f
                    do {
                        read = iff.read(arr)
                        if(read > 0){
                            tof.write(arr, 0, read)
                            tof.flush()
                        }
                        xpkgCopyProgress = len / tRead
                        tRead += read
                    }while(read > 0)
                    tof.close()
                    iff.close()


                    status = Status.ReadPackage
                    fileSize = tmpFile.length()
                    val zipf = ZipFile(tmpFile, ZipFile.OPEN_READ)
                    xpkg = XPKGFile(zipf)

                    status = Status.LoadAsset
                    val ppkg = xpkg

                    if(ppkg != null){
                        if(ppkg.iconPath.isNotEmpty()){
                            val key = ppkg.iconPath
                            val str = zipf.getInputStream(zipf.getEntry(key))
                            try{
                                pkgIcon = BitmapFactory.decodeStream(str)
                            }catch(e:Exception){
                                e.printStackTrace()
                            }
                            str.close()
                        }

                        val req = ppkg.checkInstalls.split(";")
                        val callback = arrayListOf<XMBItem>()
                        val apps = vsh.categories.find { it.id == VSH.ITEM_CATEGORY_APPS }?.content ?: callback
                        val game = vsh.categories.find { it.id == VSH.ITEM_CATEGORY_GAME }?.content ?: callback
                        val list = arrayListOf<XMBItem>().apply { addAll(apps); addAll(game) }
                        val canInstall = list.indexOfFirst {
                            val e = it as XMBAppItem?
                            req.any { itt ->
                                itt.trim() == e?.packageName
                            }
                        } > 1
                        status = canInstall.select(Status.Confirmation, Status.PackageNotInstalled)
                    }else{
                        status = Status.LoadFailed
                        valid = Validity.Corrupted
                    }
                }catch(e:Exception){
                    e.printStackTrace()
                    status = Status.LoadFailed
                    valid = Validity.Corrupted
                }
            }

            vsh.setLoadingFinished(handle)
        }
    }

    private fun clearPackageCache() {
        vsh.allCacheDirs.forEach { cdir ->
            cdir.listFiles { _, name -> name.endsWith(".xpkg") }?.forEach { pkg ->
                pkg.delete()
            }
        }
    }

    private val cFilePathDrawTime = Ref(0.0f)

    private fun tryGetFileName(uri: Uri?, outStr:Ref<String>) : Boolean {
        if(uri == null) return false

        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = vsh.contentResolver.query(uri, proj, null, null, null, null)
        if(cursor != null){
            cursor.moveToFirst()
            val ci = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            outStr.p = cursor.getStringOrNull(ci) ?: ""
            cursor.close()
            return outStr.p.isNotEmpty()
        }

        return false
    }
    private fun getPathFileName(src:String): String{
        return src.split(File.separatorChar).last()
    }

    private fun tryWrite(f:File) : Boolean {
        try{
            val cf = File(f, "io.check")
            if(cf.exists()) cf.delete() else cf.createNewFile()
            return true
        }catch(_:Exception){

        }
        return false
    }

    private fun installData(){
        val nwStr = vsh.getString(R.string.error_pkgi_no_media)
        vsh.threadPool.execute {
            status = Status.Installing
            fun setProgressInfo(info:String) {
                synchronized(installInfoLock){
                    installInfo = info
                }
            }

            if(!storageDestination.exists()){
                val dirs = vsh.getExternalFilesDirs(null)
                storageDestination = vsh.filesDir
                for(dir in dirs){
                    if(tryWrite(dir)){
                        storageDestination = dir
                        break
                    }
                }

                if(!tryWrite(storageDestination)){
                    setProgressInfo(nwStr)
                    return@execute
                }
            }


            installingForSystem = false

            try{
                val xpp = xpkg!!

                val sizeSum = xpp.zip.entries().asSequence().sumOf { it.size }.coerceAtLeast(1L)
                var copyNow = 0L

                fun postProgress(){
                    synchronized(installInfoLock){
                        installProgress = copyNow.toFloat() / sizeSum
                    }
                }

                xpp.info.sectionNames.forEach { sect ->
                    val type = xpp.info[sect, "TYPE"] ?: "".uppercase()
                    val isPlugin = type == "PLUGIN"
                    val isApps = type == "APPS" || type == "GAME"
                    val isSystem = type == "SYSTEM"
                    if(isPlugin || isApps || isSystem) {
                        val id = xpp.info[sect, "TITLE_ID"] ?: ""
                        val targetDir = when {
                            isApps -> storageDestination.combine(VshBaseDirs.APPS_DIR, id)
                            isPlugin -> storageDestination.combine(VshBaseDirs.PLUGINS_DIR, id)
                            isSystem -> storageDestination
                            else -> storageDestination
                        }
                        val files = xpp.fileNames.filter {
                            val startOk = it.startsWith(sect)
                            val endOk = validAppFileSuffices.indexOfFirst { iit ->
                                it.endsWith(iit)
                            } > -1

                            startOk && isApps.select(endOk, true)
                        }
                        for (file in files) {
                            val e = xpp.zip.getEntry(file)
                            if (e != null) {
                                val fName = isApps.select(getPathFileName(file).toString(), file)
                                val targetFile = targetDir.combine(fName)
                                val ins = xpp.zip.getInputStream(e)

                                if(targetFile?.parentFile?.isFile == true){
                                    targetFile.parentFile?.delete()
                                }

                                if(targetFile?.parentFile?.exists() == false){
                                    targetFile.parentFile?.mkdirs()
                                }

                                if (targetFile?.exists() == false) {
                                    targetFile.createNewFile()
                                }

                                setProgressInfo(file)

                                val out = targetFile?.outputStream()
                                if(out != null){
                                    out.channel.truncate(0L)

                                    var read = 0
                                    val bufSize = 4096
                                    val bar = ByteArray(bufSize)
                                    do {
                                        read = ins.read(bar)
                                        out.write(bar, 0, read)
                                        copyNow += read
                                        postProgress()
                                    } while (read >= bufSize || ins.available() > 0)
                                }
                            }
                        }

                        if(isSystem){
                            installingForSystem = true
                        }

                    }
                }
                status = Status.InstallSuccess
            }catch (e:Exception){
                status = Status.InstallFailed
                setProgressInfo(e.message ?: e.toString())
                e.printStackTrace()
            }
        }
    }

    private fun drawIcon(ctx:Canvas, drawBound: RectF, deltaTime:Float){
        cFilePathDrawTime.p += deltaTime
        rectFBuf.set(
            drawBound.centerX() - 240.0f,
            drawBound.centerY() - 66.0f,
            drawBound.centerX()  ,
            drawBound.centerY() + 66.0f)

        rectFBuf.offset(-120.0f, 0.0f)
        ctx.drawBitmap(pkgIcon, null, rectFBuf, ip, FittingMode.FIT, 0.5f, 0.5f)

        // Path
        tSmall.textAlign = Paint.Align.CENTER
        DrawExtension.scrollText(ctx, filePath,
            rectFBuf.centerX() - 200.0f,
            rectFBuf.centerX() + 200.0f,
            rectFBuf.bottom + 20.0f,
            tSmall, 0.5f, cFilePathDrawTime,10.0f)
        ctx.drawText(fileSize.asBytes(), rectFBuf.centerX(), rectFBuf.bottom + 50.0f, tSmall, 1.0f)
    }

    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime: Float) {
        val xpp = xpkg
        if(status.isLoading){
            tBig.textAlign = Paint.Align.CENTER
            ctx.drawText(vsh.getString(R.string.package_import_status_loading), drawBound.centerX(), drawBound.centerY() - 20.0f, tBig)
            DrawExtension.progressBar(ctx, 0.0f, 1.0f, xpkgLoadProgress, drawBound.centerX() - 300.0f, drawBound.centerY(), 600.0f, 10.0f, Paint.Align.LEFT)
        } else if(status == Status.LoadFailed){
            tBig.textAlign = Paint.Align.CENTER
            val str = when(valid) {
                Validity.TypeMismatch -> vsh.getString(R.string.package_import_error_type_mismatch)
                Validity.Corrupted -> vsh.getString(R.string.package_import_error_corrupt)
                else -> vsh.getString(R.string.package_import_unknown_error)
            }
            ctx.drawText(str, drawBound.centerX(), drawBound.centerY(), tBig)
        }
        else if(status == Status.Confirmation) {
            if(xpp != null){
                drawIcon(ctx, drawBound, deltaTime)
                tBig.textAlign = Paint.Align.LEFT
                // ctx.drawText("Copy Possible!", drawBound.centerX() + 10.0f, drawBound.centerY(), tBig)
                val left = drawBound.centerX() + 10.0f
                val right = min(drawBound.right - 20.0f, drawBound.centerX() + 500.0f)
                tSmall.textAlign = Paint.Align.LEFT
                DrawExtension.scrollText(ctx, xpp.title, left, right, drawBound.centerY() - 70.0f, tBig, 0.0f, cFilePathDrawTime.p, 24.0f)
                DrawExtension.scrollText(ctx, "by ${xpp.author}", left, right, drawBound.centerY() - 40.0f, tSmall, 0.0f, cFilePathDrawTime.p, 24.0f)
                DrawExtension.scrollText(ctx, "Packaged by ${xpp.packager}", left, right, drawBound.centerY() - 20.0f, tSmall, 0.0f, cFilePathDrawTime.p, 24.0f)

                val descText = tSmall.wrapText(xpp.description, right - left).lines()
                descText.forEachIndexed { i, s ->
                    DrawExtension.scrollText(ctx, s, left, right, drawBound.centerY() + 30.0f + (i * tSmall.textSize), tSmall, 0.0f, cFilePathDrawTime.p, 24.0f)
                }
            }
        }else if(status == Status.PackageNotInstalled){
            drawIcon(ctx, drawBound, deltaTime)
            tBig.textAlign = Paint.Align.LEFT
            val installChecks = xpkg?.checkInstalls?.replace("?", "\n")
            val lines = "Requested application is not installed : \n${installChecks}".lines()
            val y = drawBound.centerY() - (lines.size * (tBig.textSize * 1.2f) / 2.0f)
            lines.forEachIndexed { index, s ->
                ctx.drawText(s, drawBound.centerX() - 80.0f, y + (index * (tBig.textSize * 1.2f)), tBig)
            }
        }else if(status == Status.Installing){
            drawIcon(ctx, drawBound, deltaTime)
            val left = drawBound.centerX() - 50.0f
            val right = drawBound.right - 100.0f
            val y  = drawBound.centerY() - 100.0f
            val tCenter = ((left + right) / 2.0f) - 100.0f
            tBig.textAlign = Paint.Align.LEFT
            ctx.drawText("Installing...", tCenter, y, tBig)
            ctx.drawText("Do not close the launcher", tCenter, y + 30.0f, tBig)
            DrawExtension.progressBar(ctx, 0.0f, 1.0f, installProgress, left, drawBound.centerY(), right - left, 12.0f)
            tSmall.textAlign = Paint.Align.LEFT
            ctx.drawText(installInfo, left, drawBound.centerY() + 50.0f, tSmall)
        }else if(status.isFinished){
            drawIcon(ctx, drawBound, deltaTime)
            val left = drawBound.centerX() - 50.0f
            val right = drawBound.right - 100.0f
            tBig.textAlign = Paint.Align.CENTER
            val str = (status == Status.InstallSuccess).select("Installation Success", "Installation Failed : $installInfo")
            DrawExtension.scrollText(ctx, str, left, right, drawBound.centerY(), tBig, 0.0f, cFilePathDrawTime, 12.0f)

            if(installingForSystem && status == Status.InstallSuccess){

                val cStr = "Installed Package contains new system file, Please restart launcher for some changes to take effect."
                DrawExtension.scrollText(ctx, cStr, drawBound.left + 100.0f, drawBound.right - 100.0f, drawBound.bottom - (tBig.textSize * 2.0f), tBig, 0.0f, cFilePathDrawTime, 12.0f)
            }
        }
        super.onDraw(ctx, drawBound, deltaTime)
    }

    override fun onClose() {
        if(tmpFile.exists()){
            tmpFile.delete()
        }
        pkgIcon.recycle()
    }

    override fun onDialogButton(isPositive: Boolean) {
        if(status.canShowButton){
            if(isPositive){
                if(status == Status.Confirmation){
                    installData()
                }
            }else{
                finish(VshViewPage.MainMenu)
            }
        }
    }
}