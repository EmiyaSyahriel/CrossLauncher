package id.psw.vshlauncher

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import id.psw.vshlauncher.types.XmbItem
import id.psw.vshlauncher.types.media.MusicData
import id.psw.vshlauncher.types.media.VideoData
import id.psw.vshlauncher.types.media.XmbMusicItem
import id.psw.vshlauncher.types.media.XmbPhotoItem
import id.psw.vshlauncher.types.media.XmbVideoItem

fun Vsh.cleanMediaListing(){
    for(cat in categories){
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

fun <T> Cursor.getValue(id: String, getter : Cursor.(Int) -> T, defVal : T)  : T {
    val kId = getColumnIndex(id)
    if(kId < 0) return defVal
    return getter(this, kId)
}

fun Vsh.mediaListingStart(){
    // Return directly if media started
    if(mediaListingStarted) return

    // Check for permission
    /*
    if (sdkAtLeast(23) && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
        xmb.runOnUiThread {
            if(ActivityCompat.shouldShowRequestPermissionRationale(xmb, Manifest.permission.READ_EXTERNAL_STORAGE)){
                xmb.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), Vsh.ACT_REQ_MEDIA_LISTING)
            }
        }
        return
    }*/

    cleanMediaListing()

    val sndProj = arrayOf(
        MediaStore.Audio.AudioColumns._ID,       /* long _id */
        MediaStore.Audio.AudioColumns.DATA,      /* string absolute_path */
        MediaStore.Audio.AudioColumns.TITLE,     /* string title */
        MediaStore.Audio.AudioColumns.ALBUM,     /* string album */
        MediaStore.Audio.AudioColumns.ARTIST,    /* string artist */
        MediaStore.Audio.AudioColumns.SIZE,      /* long size */
        MediaStore.Audio.AudioColumns.DURATION,  /* long duration_ms */
        MediaStore.Audio.AudioColumns.MIME_TYPE  /* string mime_type */
    )

    val vidProj = arrayOf(
        MediaStore.Video.VideoColumns._ID,          /* long _id */
        MediaStore.Video.VideoColumns.DISPLAY_NAME, /* string display_name */
        MediaStore.Video.VideoColumns.DATA,         /* string absolute_path */
        MediaStore.Video.VideoColumns.SIZE,         /* long size */
        MediaStore.Video.VideoColumns.DURATION,     /* long duration_ms */
        MediaStore.Video.VideoColumns.MIME_TYPE     /* string mime_type */
    )

    val sndCol = if(sdkAtLeast(30))
        MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    val vidCol = if(sdkAtLeast(30))
        MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    else MediaStore.Video.Media.EXTERNAL_CONTENT_URI

    val sndCur = contentResolver.query(sndCol, sndProj, null, null, null)
    val vidCur = contentResolver.query(vidCol, vidProj, null, null, null)

    if(sndCur != null){

        sndCur.moveToFirst()
        while(!sndCur.isAfterLast){
            // TODO : Use getString for Unknowns
            val id    = sndCur.getValue(sndProj[0], Cursor::getLong,   0L)
            val path  = sndCur.getValue(sndProj[1], Cursor::getString, "/dev/null")
            val title = sndCur.getValue(sndProj[2], Cursor::getString, "No Title")
            val album = sndCur.getValue(sndProj[3], Cursor::getString, "No Album")
            val artis = sndCur.getValue(sndProj[4], Cursor::getString, "Unknown Artist")
            val size  = sndCur.getValue(sndProj[5], Cursor::getLong,   0L)
            val dur   = sndCur.getValue(sndProj[6], Cursor::getLong,   0L)
            val mime  = sndCur.getValue(sndProj[7], Cursor::getString, "audio/*")
            linearMediaList.musics.add(XmbMusicItem(vsh,
                MusicData(id, path, title, album, artis, size, dur, mime))
            )
            sndCur.moveToNext()
        }
        sndCur.close()
    }

    if(vidCur != null){
        while(!vidCur.isAfterLast){
            val id    = vidCur.getValue(vidProj[0], Cursor::getLong, 0L)
            val name  = vidCur.getValue(vidProj[1], Cursor::getString, "Unknown.3gp")
            val path  = vidCur.getValue(vidProj[2], Cursor::getString, "/dev/null")
            val size  = vidCur.getValue(vidProj[3], Cursor::getLong, 0L)
            val dur   = vidCur.getValue(vidProj[4], Cursor::getLong, 0L)
            val mime  = vidCur.getValue(vidProj[5], Cursor::getString, "video/*")
            linearMediaList.videos.add(XmbVideoItem(vsh,
                VideoData(id, name, path, size, dur, mime))
            )
            vidCur.moveToNext()
        }
        vidCur.close()
    }

    // Update Items
    mediaListingRegisterUpdater()
    mediaListingStarted = true
}

var isMediaListingUpdaterRegistered = false

fun Vsh.mediaListingRegisterUpdater(){
    if(isMediaListingUpdaterRegistered) return

    if(sdkAtLeast(Build.VERSION_CODES.N))
        isMediaListingUpdaterRegistered = true
}

fun Vsh.mediaListingAdd(uri: Uri){

}