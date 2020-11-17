package id.psw.vshlauncher.icontypes

import android.database.Cursor
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import id.psw.vshlauncher.*
import java.io.File

class VideoIcon(itemID:Int, private val vsh: VSH, private val cursor: Cursor?) : VshY(itemID) {

    data class VideoMetadata(
        val id : Int,
        val file: File?,
        val fileName : String,
        val size : String,
        val albumArt : Bitmap
    )

    private var metadata : VideoMetadata
    private var isValid = false


    init{
        if(cursor != null){
            val dataCol = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
            val file = File(cursor.getString(dataCol))
            val fileName = file.name
            val size = file.length().toSize()
            val albumArt = ThumbnailUtils.createVideoThumbnail(file.absolutePath, MediaStore.Video.Thumbnails.MINI_KIND) ?: transparentBitmap
            metadata = VideoMetadata(itemID, file, fileName, size, albumArt)
        }
        metadata = VideoMetadata(0, null, vsh.getString(R.string.common_corrupted), "", transparentBitmap)
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