package id.psw.vshlauncher.icontypes

import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.widget.VideoView
import androidx.core.graphics.scale
import id.psw.vshlauncher.*
import java.io.File
import java.lang.Exception

// TODO : fix cursor is mostly null when created this icon, causing the icon appear corrupted
class VideoIcon(itemID:Int, private val vsh: VSH, private val path: String) : VshY(itemID) {

    companion object{
        var doVideoPreview = true
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

    private var thumbnailVp : VideoView? = null
    private var lastPreviewPos = 0
    private var thumbnailStart = 0
    private var thumbnailEnd = 0


    init{
        loadCorruptedIcon()
        metadata = try{
            val file = File(path)
            val fileName = file.name
            val size = file.length().toSize()
            val albumArt = ThumbnailUtils.createVideoThumbnail(file.absolutePath, MediaStore.Video.Thumbnails.MINI_KIND) ?: transparentBitmap
            loadVideoPreview()
            thumbnailStart = 0
            thumbnailEnd = 30000
            VideoMetadata(itemID, file, fileName, size, albumArt)
        }catch(e:Exception){
            e.printStackTrace()
            VideoMetadata(0, null, vsh.getString(R.string.common_corrupted), "", transparentBitmap)
        }
    }

    /**
     * Main getter of video thumbnail
     *
     * TODO: Can be optimized further by caching the canvas and bitmap to variable
     */
    private fun getCurrentVideoBitmap(selected:Boolean = true) : Bitmap{
        val width = (if(selected) selectedIconSizeWidth else unselectedIconSize).toInt()
        val height = (if(selected) selectedIconSize else unselectedIconSize).toInt()
        if(doVideoPreview) {
            val tvp = thumbnailVp
            if(tvp != null){
                val retval = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                val retCan = Canvas(retval)
                tvp.draw(retCan)
                return retval
            }
        }

        return metadata.albumArt.scale(width, height)
    }

    /**
     * Load video preview of the view in case user do set it to true
     */
    private fun loadVideoPreview(){
        if(doVideoPreview){
            val tvp = VideoView(vsh)
            // set it to loop and mute audio since we shouldn't play the audio on preview anyway
            tvp.setOnPreparedListener {
                with(it) { isLooping = true; setVolume(0f, 0f) }
            }
            tvp.setVideoPath(path)
            thumbnailVp = tvp
        }
    }

    /**
     * Pause the video player if
     */
    override fun onHidden() {
        val tvp = thumbnailVp
        if(tvp != null && doVideoPreview){
            if(tvp.canPause()){
                lastPreviewPos = tvp.currentPosition
                tvp.pause()
            }
        }
    }

    override fun onScreen() {
        val tvp = thumbnailVp
        if(tvp != null && doVideoPreview){
            tvp.seekTo(lastPreviewPos)
            tvp.start()
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

    // TODO: Create a small video thumbnail and fix squared aspect ratio problem
    override val selectedIcon: Bitmap
        get() = metadata.albumArt

    override val unselectedIcon: Bitmap
        get() = metadata.albumArt

    override val onLaunch: Runnable
        get() = Runnable {
            if(isValid){
                val uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, metadata.id.toString())
                vsh.openVideoFile(metadata.file!!)
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