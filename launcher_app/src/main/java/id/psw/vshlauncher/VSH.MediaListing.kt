package id.psw.vshlauncher

import android.Manifest
import android.content.ContentUris
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

    mediaListingStarted = true

    val isAllowed = if(sdkAtLeast(33))
        hasPermission(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
        )
        else
            hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)

    if (isAllowed){
        xmb.runOnUiThread {
            if(ActivityCompat.shouldShowRequestPermissionRationale(xmb, Manifest.permission.READ_EXTERNAL_STORAGE)){
                val i = 0

                val perms = arrayListOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )

                if(sdkAtLeast(33)){
                    perms.addAllV(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO
                    )
                }

                val permsArr = Array(perms.size){ perms[it] }
                if(sdkAtLeast(23)){
                    xmb.requestPermissions(permsArr, Vsh.ACT_REQ_MEDIA_LISTING)
                }
            }
        }
        mediaListingStarted = false
        return
    }

    cleanMediaListing()

    val sndProj = arrayOf(
        MediaStore.Audio.AudioColumns._ID,       /* long _id */
        MediaStore.Audio.AudioColumns.DATA,      /* string absolute_path */
        MediaStore.Audio.AudioColumns.TITLE,     /* string title */
        MediaStore.Audio.AudioColumns.ALBUM,     /* string album */
        MediaStore.Audio.AudioColumns.ARTIST,    /* string artist */
        MediaStore.Audio.AudioColumns.SIZE,      /* long size */
        MediaStore.Audio.AudioColumns.DURATION,  /* long duration_ms */
        MediaStore.Audio.AudioColumns.MIME_TYPE,  /* string mime_type */
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
        arrayOf(
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        )
    else arrayOf(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        MediaStore.Audio.Media.INTERNAL_CONTENT_URI
    )

    val vidCol = if(sdkAtLeast(30))
        arrayOf(
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        )
    else arrayOf(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        MediaStore.Video.Media.INTERNAL_CONTENT_URI
    )

    val sndDataPairs = Array(sndCol.size){
        Pair(sndCol[it], contentResolver.query(sndCol[it], sndProj, null, null, null))
    }

    val vidDataPairs =Array(vidCol.size){
        Pair(vidCol[it], contentResolver.query(vidCol[it], vidProj, null, null, null))
    }

    for(pair in sndDataPairs){
        val sndCur = pair.second
        if(sndCur != null){
            sndCur.moveToFirst()
            while(!sndCur.isAfterLast){
                try {
                    // TODO : Use getString for Unknowns
                    val id    = sndCur.getValue(sndProj[0], Cursor::getLong,   0L)
                    val path  = sndCur.getValue(sndProj[1], Cursor::getString, "/dev/null")
                    val title = sndCur.getValue(sndProj[2], Cursor::getString, "No Title")
                    val album = sndCur.getValue(sndProj[3], Cursor::getString, "No Album")
                    val artis = sndCur.getValue(sndProj[4], Cursor::getString, "Unknown Artist")
                    val size  = sndCur.getValue(sndProj[5], Cursor::getLong,   0L)
                    val dur   = sndCur.getValue(sndProj[6], Cursor::getLong,   0L)
                    val mime  = sndCur.getValue(sndProj[7], Cursor::getString, "audio/*")
                    val uri   = ContentUris.withAppendedId(pair.first, id)

                    // Find duplicate
                    if(linearMediaList.musics.find { it.data.id == id } == null){
                        linearMediaList.musics.add(XmbMusicItem(vsh,
                            MusicData(id, uri, path, title, album, artis, size, dur, mime))
                        )
                    }
                }catch(e:Exception){
                    e.printStackTrace()
                }
                finally {
                }
                sndCur.moveToNext()
            }
            sndCur.close()
        }
    }

    for(pair in vidDataPairs){
        val vidCur = pair.second
        if(vidCur != null){
            while(!vidCur.isAfterLast){
                try{
                    val id    = vidCur.getValue(vidProj[0], Cursor::getLong, 0L)
                    val name  = vidCur.getValue(vidProj[1], Cursor::getString, "Unknown.3gp")
                    val path  = vidCur.getValue(vidProj[2], Cursor::getString, "/dev/null")
                    val size  = vidCur.getValue(vidProj[3], Cursor::getLong, 0L)
                    val dur   = vidCur.getValue(vidProj[4], Cursor::getLong, 0L)
                    val mime  = vidCur.getValue(vidProj[5], Cursor::getString, "video/*")
                    val uri   = ContentUris.withAppendedId(pair.first, id)

                    if(linearMediaList.videos.find { it.data.id == id } == null){
                        linearMediaList.videos.add(XmbVideoItem(vsh,
                            VideoData(id, uri, name, path, size, dur, mime))
                        )
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                } finally {
                }
                vidCur.moveToNext()
            }
            vidCur.close()
        }
    }

    // Update Items
    mediaListingRegisterUpdater()
    mediaListingStarted = false
}

var isMediaListingUpdaterRegistered = false

fun Vsh.mediaListingRegisterUpdater(){
    if(isMediaListingUpdaterRegistered) return

    if(sdkAtLeast(Build.VERSION_CODES.N))
        isMediaListingUpdaterRegistered = true

    val music = vsh.categories.find { it.id == Vsh.ITEM_CATEGORY_MUSIC }

    if(music != null){
        music.content.removeAll { it is XmbMusicItem }
        music.content.addAll(linearMediaList.musics)
    }

    val video = vsh.categories.find { it.id == Vsh.ITEM_CATEGORY_VIDEO }
    if(video != null){
        video.content.removeAll { it is XmbVideoItem }
        video.content.addAll(linearMediaList.videos)
    }
}

fun Vsh.mediaListingAdd(uri: Uri){

}