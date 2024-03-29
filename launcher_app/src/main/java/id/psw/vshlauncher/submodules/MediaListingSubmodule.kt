package id.psw.vshlauncher.submodules

import android.Manifest
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Intent
import android.database.ContentObserver
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import id.psw.vshlauncher.R
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.addToCategory
import id.psw.vshlauncher.sdkAtLeast
import id.psw.vshlauncher.types.XmbItem
import id.psw.vshlauncher.types.items.XmbMenuItem
import id.psw.vshlauncher.types.media.LinearMediaList
import id.psw.vshlauncher.types.media.MediaData
import id.psw.vshlauncher.types.media.MusicData
import id.psw.vshlauncher.types.media.VideoData
import id.psw.vshlauncher.types.media.XmbMusicItem
import id.psw.vshlauncher.types.media.XmbPhotoItem
import id.psw.vshlauncher.types.media.XmbVideoItem
import id.psw.vshlauncher.views.dialogviews.ConfirmDialogView
import id.psw.vshlauncher.xmb
import java.io.File

class MediaListingSubmodule(private val vsh : Vsh) : IVshSubmodule {
    private val linearMediaList = LinearMediaList()
    private var mediaListingStarted = false

    companion object {
        const val RQI_PICK_PHOTO_DIR = 1024 + 0xFE
        const val TAG = "MediaListing"
    }

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

    private fun addVideoListing(root: Uri, cursor: Cursor){
        try {
            // TODO : Use getString for Unknowns
            val id    = cursor.getValue(videoProjection[0], Cursor::getLong, 0L)
            val name  = cursor.getValue(videoProjection[1], Cursor::getString, "Unknown.3gp")
            val path  = cursor.getValue(videoProjection[2], Cursor::getString, "/dev/null")
            val size  = cursor.getValue(videoProjection[3], Cursor::getLong, 0L)
            val dur   = cursor.getValue(videoProjection[4], Cursor::getLong, 0L)
            val mime  = cursor.getValue(videoProjection[5], Cursor::getString, "video/*")
            val uri   = ContentUris.withAppendedId(root, id)

            linearMediaList.videos.add(
                XmbVideoItem(vsh, VideoData(id, uri, path, name, size, dur, mime))
            )
        }catch(cio: CursorIndexOutOfBoundsException){
            cio.printStackTrace()
        }
    }
    private fun addAudioListing(root: Uri, cursor: Cursor){
        try {
            // TODO : Use getString for Unknowns
            val id    = cursor.getValue(audioProjection[0], Cursor::getLong,   0L)
            val path  = cursor.getValue(audioProjection[1], Cursor::getString, "/dev/null")
            val title = cursor.getValue(audioProjection[2], Cursor::getString, "No Title")
            val album = cursor.getValue(audioProjection[3], Cursor::getString, "No Album")
            val artis = cursor.getValue(audioProjection[4], Cursor::getString, "Unknown Artist")
            val size  = cursor.getValue(audioProjection[5], Cursor::getLong,   0L)
            val dur   = cursor.getValue(audioProjection[6], Cursor::getLong,   0L)
            val mime  = cursor.getValue(audioProjection[7], Cursor::getString, "audio/*")
            val uri   = ContentUris.withAppendedId(root, id)

            linearMediaList.musics.add(
                XmbMusicItem(vsh, MusicData(id, uri, path, title, album, artis, size, dur, mime))
            )
        }catch(cio: CursorIndexOutOfBoundsException){
            cio.printStackTrace()
        }
    }

    fun createMediaMenuItems(_itemMenus : ArrayList<XmbMenuItem>, data: MediaData){
        // Play Media
        _itemMenus.add(XmbMenuItem.XmbMenuItemLambda(
            { vsh.getString(R.string.media_play ) },
            {false}, 0)
        {
            vsh.openFileOnExternalApp(File(data.data), false, vsh.getString(R.string.media_play))
        })

        // Open With
        _itemMenus.add(XmbMenuItem.XmbMenuItemLambda(
            { vsh.getString(R.string.media_open_with) },
            {false}, 1)
        {
            vsh.openFileOnExternalApp(File(data.data), true, vsh.getString(R.string.media_open_with))
        })

        // Delete File
        _itemMenus.add(XmbMenuItem.XmbMenuItemLambda(
            { vsh.getString(R.string.media_delete) },
            {false}, 2)
        {
            vsh.xmbView?.showDialog(
                ConfirmDialogView(
                    vsh.safeXmbView,
                    vsh.getString(R.string.media_delete),
                    R.drawable.ic_delete,
                    vsh.getString(R.string.media_delete_confirmation).format(data.data)
                ){ confirmed ->
                    if(confirmed){
                        deleteMedia(data.uri)
                    }
                })
        })

        // Share File (You cannot copy since launcher have no direct interface to storage other than it's own)
        _itemMenus.add(XmbMenuItem.XmbMenuItemLambda(
            { vsh.getString(R.string.media_share) },
            {false}, 3)
        {
            val i= Intent(Intent.ACTION_SEND)
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            i.setDataAndType(data.uri, vsh.contentResolver.getType(data.uri))
            i.putExtra(Intent.EXTRA_STREAM, data.uri)

            vsh.xmb.startActivity(Intent.createChooser(i, vsh.getString(R.string.media_share)))
        })
    }


    private fun deleteMedia(uri:Uri){
        val rsl = vsh.contentResolver
        try {
            vsh.contentResolver.delete(uri, null, null)
            mediaListingStart()
        }catch (scEx: SecurityException){
            if(sdkAtLeast(Build.VERSION_CODES.Q)){
                val rse = scEx as RecoverableSecurityException
                val isr = IntentSenderRequest.Builder(rse.userAction.actionIntent.intentSender).build()
                vsh.xmb.mediaDeletionActivityResult.launch(isr)
            }
        }
    }

    private fun beginMediaListingVideo(){
        val vidCols = if(sdkAtLeast(30))
            arrayOf(
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
            )
        else arrayOf(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Video.Media.INTERNAL_CONTENT_URI
        )

        for(vidCol in vidCols){
            val vidCur = vsh.contentResolver.query(vidCol, videoProjection, null, null, null)

            if(vidCur != null){
                vidCur.moveToFirst()
                while(!vidCur.isAfterLast){
                    addVideoListing(vidCol, vidCur)
                    vidCur.moveToNext()
                }
                vidCur.close()
            }
        }
    }

    private fun beginMediaListingAudio(){
        val sndCols = if(sdkAtLeast(30))
            arrayOf(
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
            )
        else arrayOf(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Audio.Media.INTERNAL_CONTENT_URI
        )

        for(sndCol in sndCols){
            val sndCur = vsh.contentResolver.query(sndCol, audioProjection, null, null, null)

            if(sndCur != null){
                sndCur.moveToFirst()
                while(!sndCur.isAfterLast){
                    addAudioListing(sndCol, sndCur)
                    sndCur.moveToNext()
                }
                sndCur.close()
            }
        }
    }

    fun addPhotoDirectory(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val i = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            vsh.xmb.startActivityForResult(i, RQI_PICK_PHOTO_DIR)
        }
    }

    fun onDirectoryPicked(i: Intent){

    }

    private fun beginMediaListingPhoto(){
        // TODO: List photos
    }

    var hasPermissionCached = false
        private set

    val hasPermission: Boolean get() {
        var ok =  true
        if (sdkAtLeast(Build.VERSION_CODES.TIRAMISU)) {
            ok = vsh.hasPermissionGranted(Manifest.permission.READ_MEDIA_AUDIO ) && ok
            ok = vsh.hasPermissionGranted(Manifest.permission.READ_MEDIA_VIDEO ) && ok
            ok = vsh.hasPermissionGranted(Manifest.permission.READ_MEDIA_IMAGES) && ok
        }
        hasPermissionCached = ok
        return ok
    }

    fun requestPermission(){
        if(!hasPermission){
            val names = arrayListOf<String>()

            // names.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (sdkAtLeast(Build.VERSION_CODES.TIRAMISU)) {
                names.add(Manifest.permission.READ_MEDIA_AUDIO )
                names.add(Manifest.permission.READ_MEDIA_VIDEO )
                names.add(Manifest.permission.READ_MEDIA_IMAGES)
            }

            val arr = Array(names.size){ i -> names[i] }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                vsh.xmb.requestPermissions(arr, RQI_PICK_PHOTO_DIR)
            }
        }
    }

    fun mediaListingStart(){
        // Return directly if media already started
        if(mediaListingStarted) return

        // No permission, return
        if(!hasPermission) return

        mediaListingStarted = true

        cleanMediaListing()
        beginMediaListingVideo()
        beginMediaListingAudio()
        beginMediaListingPhoto()

        addMediaToCategories()

        // Update Items
        mediaListingStarted = false
    }

    private fun addMediaToCategories() {
        for(l in linearMediaList.musics){
            Log.d(TAG, "Music : ${l.id}")
            vsh.addToCategory(Vsh.ITEM_CATEGORY_MUSIC, l)
        }

        for(l in linearMediaList.videos){
            Log.d(TAG, "Video : ${l.id}")
            vsh.addToCategory(Vsh.ITEM_CATEGORY_VIDEO, l)
        }

        for(l in linearMediaList.photos){
            Log.d(TAG, "Photo : ${l.id}")
            vsh.addToCategory(Vsh.ITEM_CATEGORY_PHOTO, l)
        }
    }

    private val videoObserver : ContentObserver by lazy {
        val o = object : ContentObserver(Handler(vsh.mainLooper)) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)

                if (uri == null) return
                val c = vsh.contentResolver.query(uri, videoProjection, null, null, null)

                if (c != null) {
                    while (!c.isAfterLast) {
                        addVideoListing(uri, c)
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
                        addAudioListing(uri, c)
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