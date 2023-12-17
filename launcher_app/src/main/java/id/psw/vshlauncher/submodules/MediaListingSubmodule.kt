package id.psw.vshlauncher.submodules

import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.sdkAtLeast
import id.psw.vshlauncher.types.XmbItem
import id.psw.vshlauncher.types.media.LinearMediaList
import id.psw.vshlauncher.types.media.MusicData
import id.psw.vshlauncher.types.media.VideoData
import id.psw.vshlauncher.types.media.XmbMusicItem
import id.psw.vshlauncher.types.media.XmbPhotoItem
import id.psw.vshlauncher.types.media.XmbVideoItem

class MediaListingSubmodule(private val vsh : Vsh) : IVshSubmodule {
    private val linearMediaList = LinearMediaList()
    private var mediaListingStarted = false

    private val videoProjection = arrayOf(
        MediaStore.Video.VideoColumns._ID,          /* long _id */
        MediaStore.Video.VideoColumns.DISPLAY_NAME, /* string display_name */
        MediaStore.Video.VideoColumns.DATA,         /* string absolute_path */
        MediaStore.Video.VideoColumns.SIZE,         /* long size */
        MediaStore.Video.VideoColumns.DURATION,     /* long duration_ms */
        MediaStore.Video.VideoColumns.MIME_TYPE     /* string mime_type */
    )

    private val audioProjection = arrayOf(
        MediaStore.Audio.AudioColumns._ID,       /* long _id */
        MediaStore.Audio.AudioColumns.DATA,      /* string absolute_path */
        MediaStore.Audio.AudioColumns.TITLE,     /* string title */
        MediaStore.Audio.AudioColumns.ALBUM,     /* string album */
        MediaStore.Audio.AudioColumns.ARTIST,    /* string artist */
        MediaStore.Audio.AudioColumns.SIZE,      /* long size */
        MediaStore.Audio.AudioColumns.DURATION,  /* long duration_ms */
        MediaStore.Audio.AudioColumns.MIME_TYPE  /* string mime_type */
    )

    override fun onCreate() {
        mediaListingRegisterObserver()
    }

    override fun onDestroy() {
        mediaListingUnregisterObserver()
    }

    fun <T> Cursor.getValue(id: String, getter : Cursor.(Int) -> T, defVal : T)  : T {
        val kId = getColumnIndex(id)
        if(kId < 0) return defVal
        return getter(this, kId)
    }

    private fun cleanMediaListing(){
        for(cat in vsh.categories){
            val removed = arrayListOf<XmbItem>()
            for(item in cat.content){
                if(item is XmbMusicItem || item is XmbPhotoItem || item is XmbVideoItem){
                    removed.add(item)
                    item.onScreenInvisible(item)
                }
            }
            cat.content.removeAll(removed.toSet())
        }

        for(i in linearMediaList.musics) i.onScreenInvisible(i)
        for(i in linearMediaList.photos) i.onScreenInvisible(i)
        for(i in linearMediaList.videos) i.onScreenInvisible(i)

        linearMediaList.musics.clear()
        linearMediaList.photos.clear()
        linearMediaList.videos.clear()
    }

    private fun addVideoListing(cursor: Cursor){
        // TODO : Use getString for Unknowns
        val id    = cursor.getValue(videoProjection[0], Cursor::getLong, 0L)
        val name  = cursor.getValue(videoProjection[1], Cursor::getString, "Unknown.3gp")
        val path  = cursor.getValue(videoProjection[2], Cursor::getString, "/dev/null")
        val size  = cursor.getValue(videoProjection[3], Cursor::getLong, 0L)
        val dur   = cursor.getValue(videoProjection[4], Cursor::getLong, 0L)
        val mime  = cursor.getValue(videoProjection[5], Cursor::getString, "video/*")
        linearMediaList.videos.add(
            XmbVideoItem(vsh, VideoData(id, name, path, size, dur, mime))
        )
    }
    private fun addAudioListing(cursor: Cursor){
        // TODO : Use getString for Unknowns
        val id    = cursor.getValue(audioProjection[0], Cursor::getLong,   0L)
        val path  = cursor.getValue(audioProjection[1], Cursor::getString, "/dev/null")
        val title = cursor.getValue(audioProjection[2], Cursor::getString, "No Title")
        val album = cursor.getValue(audioProjection[3], Cursor::getString, "No Album")
        val artis = cursor.getValue(audioProjection[4], Cursor::getString, "Unknown Artist")
        val size  = cursor.getValue(audioProjection[5], Cursor::getLong,   0L)
        val dur   = cursor.getValue(audioProjection[6], Cursor::getLong,   0L)
        val mime  = cursor.getValue(audioProjection[7], Cursor::getString, "audio/*")
        linearMediaList.musics.add(
            XmbMusicItem(vsh, MusicData(id, path, title, album, artis, size, dur, mime))
        )
    }

    private fun beginMediaListingVideo(){
        val vidCol = if(sdkAtLeast(30))
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val vidCur = vsh.contentResolver.query(vidCol, videoProjection, null, null, null)

        if(vidCur != null){
            while(!vidCur.isAfterLast){
                addVideoListing(vidCur)
                vidCur.moveToNext()
            }
            vidCur.close()
        }
    }



    private fun beginMediaListingAudio(){
        val sndCol = if(sdkAtLeast(30))
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val sndCur = vsh.contentResolver.query(sndCol, audioProjection, null, null, null)

        if(sndCur != null){

            sndCur.moveToFirst()
            while(!sndCur.isAfterLast){
                addAudioListing(sndCur)
                sndCur.moveToNext()
            }
            sndCur.close()
        }

    }

    private fun beginMediaListingPhoto(){
        // TODO: List photos
    }

    fun mediaListingStart(){
        // Return directly if media already started
        if(mediaListingStarted) return
        mediaListingStarted = true

        cleanMediaListing()
        beginMediaListingVideo()
        beginMediaListingAudio()
        beginMediaListingPhoto()

        // Update Items
        mediaListingStarted = false
    }

    private val videoObserver : ContentObserver by lazy {
        val o = object : ContentObserver(Handler(vsh.mainLooper)) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)

                if (uri == null) return
                val c = vsh.contentResolver.query(uri, videoProjection, null, null, null)

                if (c != null) {
                    while (!c.isAfterLast) {
                        addVideoListing(c)
                        c.moveToNext()
                    }
                    c.close()
                }
            }
        }
        o
    }

    private val audioObserver : ContentObserver by lazy {
        val o = object : ContentObserver(Handler(vsh.mainLooper)) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)

                if (uri == null) return
                val c = vsh.contentResolver.query(uri, audioProjection, null, null, null)

                if (c != null) {
                    while (!c.isAfterLast) {
                        addAudioListing(c)
                        c.moveToNext()
                    }
                    c.close()
                }
            }
        }
        o
    }

    private fun mediaListingRegisterObserver(){
        vsh.contentResolver.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            true, videoObserver)

        vsh.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true, audioObserver)
    }

    private fun mediaListingUnregisterObserver(){
        vsh.contentResolver.unregisterContentObserver(videoObserver)
        vsh.contentResolver.unregisterContentObserver(audioObserver)
    }
}