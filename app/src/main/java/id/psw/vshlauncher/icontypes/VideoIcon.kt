package id.psw.vshlauncher.icontypes

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import android.widget.Toast
import androidx.core.graphics.scale
import id.psw.vshlauncher.*
import id.psw.vshlauncher.views.VshView
import java.io.File
import java.lang.Exception

class VideoIcon(itemID:Int, private val vsh: VSH, private val path: String) : VshY(itemID), TextureView.SurfaceTextureListener {

    companion object{
        var doVideoPreview = false
        private var corruptedIcon : Bitmap = transparentBitmap
        private var corruptedIconUnselected : Bitmap = transparentBitmap
    }

    data class VideoMetadata(
        val id : Int,
        val file: File?,
        val fileName : String,
        val size : String,
        val albumArt : Bitmap
    )

    private var metadata : VideoMetadata
    private var isValid = false

    private var videoReady = false
    private var currentTime = 0f
    private var isPaused = false
    private var thumbnailStart = 0f
    private var thumbnailEnd = 10f
    private var fps = 30

    private var mp = MediaPlayer()
    private var texView = TextureView(vsh)
    private var srView = SurfaceView(vsh)
    private var surface : Surface? = null

    private var scaledSelectedAlbumArt : Bitmap = transparentBitmap
    private var scaledUnselectedAlbumArt : Bitmap = transparentBitmap
    private var currentVideoBitmap : Bitmap = transparentBitmap

    init{
        loadCorruptedIcon()
        metadata = try{
            val file = File(path)
            val fileName = file.name
            val size = file.length().toSize()
            val albumArt = ThumbnailUtils.createVideoThumbnail(file.absolutePath, MediaStore.Video.Thumbnails.MINI_KIND) ?: transparentBitmap
            bakeAlbumArt(albumArt)
            thumbnailStart = 0f
            thumbnailEnd = 10f
            startGrabberThread()
            isValid=true
            VideoMetadata(itemID, file, fileName, size, albumArt)
        }catch(e:Exception){
            e.printStackTrace()
            VideoMetadata(0, null, vsh.getString(R.string.common_corrupted), "", transparentBitmap)
        }
    }

    private fun arFitCalc(width:Int, height:Int, selected: Boolean): Point {
        val iconSize = if(selected) selectedIconSize else unselectedIconSize
        val mainAr = width.toFloat() / height
        val ratioX = (iconSize * 1.89f) / width
        val ratioY = iconSize / height
        val ratio = if(mainAr > 1) ratioX else ratioY
        val density = vsh.vsh.density
        return Point((width * ratio * density).toInt(), (height * ratio * density).toInt())
    }

    private fun bakeAlbumArt(albumArt: Bitmap){
        val selectedSizePoint = arFitCalc(albumArt.width, albumArt.height, true)
        val unselectedSizePoint = arFitCalc(albumArt.width, albumArt.height, false)
        scaledSelectedAlbumArt = albumArt.scale(selectedSizePoint.x, selectedSizePoint.y, false)
        scaledUnselectedAlbumArt = albumArt.scale(unselectedSizePoint.x, unselectedSizePoint.y, false)
    }

    /**
     * Main getter of video thumbnail
     *
     * TODO: Can be optimized further by caching the canvas and bitmap to variable
     */
    private fun getCurrentVideoBitmap(selected:Boolean = true) : Bitmap{

        val width = (if(selected) selectedIconSizeWidth else unselectedIconSize).toInt() * vsh.vsh.density
        val height = (if(selected) selectedIconSize else unselectedIconSize).toInt()* vsh.vsh.density
        if(doVideoPreview) {
            currentTime += VshView.deltaTime
            if(currentTime >= thumbnailEnd) currentTime = thumbnailStart
            return currentVideoBitmap
        }
        return if(selected) scaledSelectedAlbumArt else scaledUnselectedAlbumArt
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        this.surface = Surface(surface)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {  }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean = false

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) { this.surface = Surface(surface) }

    /**
     * Pause the video player if
     */
    override fun onHidden() {
        isPaused = true
    }

    override fun onScreen() {
        isPaused = false
    }

    private fun startGrabberThread(){
        if(doVideoPreview){
            mp.setDataSource(path)
            mp.setSurface(surface)
            texView.surfaceTextureListener = this
            mp.isLooping = true
            mp.setOnPreparedListener { mp.start() }
        }
    }

    private fun loadCorruptedIcon(){
        if(corruptedIcon == transparentBitmap || corruptedIconUnselected == transparentBitmap){
            val brokenImage = vsh.requestCustomIcon("rco", "common_corrupted", R.drawable.common_corrupted)
            val siSize = selectedIconSize.toInt()
            val usiSize = unselectedIconSize.toInt()
            corruptedIcon = brokenImage.scale(siSize, siSize)
            corruptedIconUnselected = brokenImage.scale(usiSize, usiSize)
        }
    }

    // TODO: Create a small video thumbnail
    override val selectedIcon: Bitmap
        get() = getCurrentVideoBitmap(true)

    override val unselectedIcon: Bitmap
        get() = getCurrentVideoBitmap(false)

    override val onLaunch: Runnable
        get() = Runnable {
            if(isValid){
                val uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, metadata.id.toString())
                vsh.openVideoFile(metadata.file!!)
            }else{
                Toast.makeText(vsh, "Invalid video file", Toast.LENGTH_LONG).show()
            }
        }

    override val name: String
        get() = metadata.fileName

    override val hasDescription: Boolean get() = true
    override val description: String
        get() = metadata.size

    override val hasOptions: Boolean
        get() = true

    // TODO: add more options
    override val options: ArrayList<VshOption>
    get() {
        return VshOptionsBuilder()
            .add("Open") { onLaunch.run() }
            .add("Delete"){}
            .build()
    }
}