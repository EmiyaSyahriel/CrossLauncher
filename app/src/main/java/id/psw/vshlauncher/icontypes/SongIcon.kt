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

// TODO : fix cursor is mostly null when created this icon, causing the icon appear corrupted
class SongIcon(itemID:Int, private val path : String, private val vsh:VSH) : VshY(itemID){
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
        var dynamicUnload = true
    }

    var metadata : SongMetadata
    private var isValid = false
    private var cachedSelectedIcon = transparentBitmap
    private var cachedUnselectedIcon = transparentBitmap

    init {
        metadata = try{
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(path)
            val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: ""
            val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""
            val album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: ""
            val size = File(path).length().toSize()
            val albumArt = getAlbumArt(path)
            SongMetadata(itemID, title, artist, album, path, size, albumArt)
        }catch (e:Exception){
            e.printStackTrace()
            SongMetadata(0, vsh.getString(R.string.common_corrupted), "", "", "", "?? B", cachedDefaultIcon ?: transparentDrawable)
        }
        loadIcon()
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

    private fun loadIcon(){
        if(isValid){
            val selectedSize = (selectedIconSize * vsh.vsh.density).toInt()
            val unselectedSize = (unselectedIconSize * vsh.vsh.density).toInt()
            val loadedIcon = metadata.albumArt
            cachedSelectedIcon = loadedIcon.toBitmap(selectedSize, selectedSize)
            cachedUnselectedIcon = loadedIcon.toBitmap(unselectedSize, unselectedSize)
        }
    }

    private fun unloadIcon(){
        if(isValid){
            if(cachedSelectedIcon != transparentBitmap) cachedSelectedIcon.recycle()
            if(cachedUnselectedIcon != transparentBitmap) cachedUnselectedIcon.recycle()
            cachedSelectedIcon = transparentBitmap
            cachedUnselectedIcon = transparentBitmap
        }
    }

    override fun onScreen() {
        if(dynamicUnload) loadIcon()
    }

    override fun onHidden() {
        if(dynamicUnload) unloadIcon()
    }

    override val name: String
        get() = metadata.title

    override val hasDescription: Boolean
        get() = true

    override val description: String
        get() = "${metadata.album} - ${metadata.artist}"


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