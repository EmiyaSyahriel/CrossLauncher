package id.psw.vshlauncher.icontypes

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.SurfaceTexture
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
import id.psw.vshlauncher.icontypes.AppIcon.Companion.selectedIconSize
import id.psw.vshlauncher.views.VshView
import java.io.File
import java.lang.Exception

class VideoIcon(itemID:Int, private val ctx: VSH, private val path: String) : XMBIcon("xmb_video_${itemID}") {

    companion object{
        var doVideoPreview = false
        private var corruptedIcon : Bitmap = TransparentBitmap
        private var corruptedIconUnselected : Bitmap = TransparentBitmap
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
    private var texView = TextureView(ctx)
    private var srView = SurfaceView(ctx)
    private var surface : Surface? = null

    init{
        loadCorruptedIcon()

        createMenu()
            .add("Open"){onLaunch()}
            .add("Delete"){}
            .apply()

        metadata = try{
            val file = File(path)
            val fileName = file.name
            val size = file.length().toSize()
            val albumArt = ThumbnailUtils.createVideoThumbnail(file.absolutePath, MediaStore.Video.Thumbnails.MINI_KIND) ?: XMBIcon.TransparentBitmap
            bakeAlbumArt(albumArt)
            thumbnailStart = 0f
            thumbnailEnd = 10f
            startGrabberThread()
            isValid=true
            VideoMetadata(itemID, file, fileName, size, albumArt)
        }catch(e:Exception){
            e.printStackTrace()
            VideoMetadata(0, null, ctx.getString(R.string.common_corrupted), "", XMBIcon.TransparentBitmap)
        }
    }

    private fun arFitCalc(width:Int, height:Int, selected: Boolean): Point {
        val iconSize = 70f
        val mainAr = width.toFloat() / height
        val ratioX = (iconSize * 1.89f) / width
        val ratioY = iconSize / height
        val ratio = if(mainAr > 1) ratioX else ratioY
        val density = 1
        return Point((width * ratio * density).toInt(), (height * ratio * density).toInt())
    }

    private fun bakeAlbumArt(albumArt: Bitmap){
        val selectedSizePoint = arFitCalc(albumArt.width, albumArt.height, true)
        val unselectedSizePoint = arFitCalc(albumArt.width, albumArt.height, false)
    }

    /**
     * Pause the video player if
     */
    fun onHidden() {
        isPaused = true
    }

    fun onScreen() {
        isPaused = false
    }

    private fun startGrabberThread(){
        if(doVideoPreview){
            mp.setDataSource(path)
            mp.setSurface(surface)
            mp.isLooping = true
            mp.setOnPreparedListener { mp.start() }
        }
    }

    private fun loadCorruptedIcon(){
        icon.unload()
    }

    // TODO: Create a small video thumbnail
    override fun onLaunch() {
            if(isValid){
                val uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, metadata.id.toString())
                ctx.openVideoFile(metadata.file!!)
            }else{
                Toast.makeText(ctx, "Invalid video file", Toast.LENGTH_LONG).show()
            }
        }

    override val name: String
        get() = metadata.fileName

    override val hasDescription: Boolean get() = true
    override val description: String
        get() = metadata.size

}