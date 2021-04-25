package id.psw.vshlauncher.icontypes

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import id.psw.vshlauncher.R
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.toSize
import java.io.File

// TODO : fix cursor is mostly null when created this icon, causing the icon appear corrupted
class SongIcon(itemID:Int, private val path : String, val ctx:VSH) : XMBIcon("xmb_song_${itemID}"){
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

    init {
        metadata = try{
            createMenu()
                .add("Open") { onLaunch() }
                .add("Delete"){}
                .apply()

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
            SongMetadata(0, ctx.getString(R.string.common_corrupted), "", "", "", "?? B", cachedDefaultIcon ?: XMBIcon.TransparentDrawable)
        }
        loadIcon()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun getAlbumArt(path:String) : Drawable {
        if(cachedDefaultIcon == null) cachedDefaultIcon = ctx.resources.getDrawable(R.drawable.icon_cda)
        var retval = cachedDefaultIcon ?: ColorDrawable(Color.TRANSPARENT)
        try{
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(ctx, Uri.parse(path))
            val data = mmr.embeddedPicture
            if(data != null) retval = BitmapFactory.decodeByteArray(data, 0, data.size).toDrawable(ctx.resources)
            mmr.release()
        }catch(e:Exception){
        }
        return retval
    }

    override fun onLaunch() {
            if(isValid){
                ctx.openAudioFile(metadata)
            }else{
                Toast.makeText(ctx, ctx.getString(R.string.audio_corrupted), Toast.LENGTH_SHORT).show()
            }
        }

    private fun loadIcon(){
        if(isValid){
            icon.reload(getAlbumArt(path).toBitmap(), 75)
        }
    }

    private fun unloadIcon(){
        if(isValid){
            icon.unload()
        }
    }

    fun onScreen() {
        if(dynamicUnload) loadIcon()
    }

    fun onHidden() {
        if(dynamicUnload) unloadIcon()
    }

    override val name: String
        get() = metadata.title

    override val hasDescription: Boolean
        get() = true

    override val description: String
        get() = "${metadata.album} - ${metadata.artist}"

}