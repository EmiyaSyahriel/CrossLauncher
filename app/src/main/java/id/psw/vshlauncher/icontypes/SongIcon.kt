package id.psw.vshlauncher.icontypes

import android.annotation.SuppressLint
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import id.psw.vshlauncher.R
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.VshY
import id.psw.vshlauncher.toSize
import java.io.File

class SongIcon(itemID:Int, private val cursor: Cursor?, private val vsh:VSH) : VshY(itemID){
    data class SongMetadata(
        val id:Int,
        val title:String,
        val artist:String,
        val album:String,
        val path:String,
        val size:String,
        val albumArt:Drawable
    )

    companion object{
        var songList : ArrayList<SongMetadata> = arrayListOf()
        fun indexOfSong(id:Int) : Int{
            return songList.indexOfFirst { it.id == id }
        }
        fun findSongWithId(id:Int): SongMetadata?{
            return songList.first { it.id == id }
        }
        var cachedDefaultIcon : Drawable? = null
    }

    var metadata : SongMetadata
    private var isValid = false

    init {
        if(cursor != null){
            val titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val pathCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val title = cursor.getString(titleCol)
            val artist = cursor.getString(artistCol) ?: vsh.getString(R.string.unknown)
            val path = cursor.getString(pathCol)
            val album = cursor.getString(albumCol) ?: vsh.getString(R.string.unknown)
            val size = File(path).length().toSize()
            metadata = SongMetadata(itemID, title, artist, album, path, size, getAlbumArt(path))
            isValid = true
        }
        metadata = SongMetadata(0, vsh.getString(R.string.common_corrupted), "", "", "", "?? B", cachedDefaultIcon ?: transparentDrawable)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun getAlbumArt(path:String) : Drawable {
        if(cachedDefaultIcon == null) cachedDefaultIcon = vsh.resources.getDrawable(R.drawable.icon_cda)
        var retval = cachedDefaultIcon ?: transparentDrawable
        try{
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(vsh, Uri.parse(path))
            val data = mmr.embeddedPicture
            if(data != null) retval = BitmapFactory.decodeByteArray(data, 0, data.size).toDrawable(vsh.resources)
            mmr.release()
        }catch(e:Exception){
        }
        return retval
    }

    override val onLaunch: Runnable
        get() = Runnable {
            if(isValid){
                vsh.openAudioFile(metadata)
            }else{
                Toast.makeText(vsh, vsh.getString(R.string.audio_corrupted), Toast.LENGTH_SHORT).show()
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