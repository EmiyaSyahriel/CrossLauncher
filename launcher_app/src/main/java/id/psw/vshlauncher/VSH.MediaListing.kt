package id.psw.vshlauncher

import android.Manifest
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi

fun VSH.mediaListingStart(){
    // Return directly if media started
    if(mediaListingStarted) return

    // Check for permission
    if (sdkAtLeast(23) && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        return

    val sndProj = arrayOf(
        MediaStore.Audio.AudioColumns._ID,
        MediaStore.Audio.AudioColumns.DATA,
        MediaStore.Audio.AudioColumns.TITLE,
        MediaStore.Audio.AudioColumns.ALBUM,
        MediaStore.Audio.AudioColumns.ARTIST,
        MediaStore.Audio.AudioColumns.DATE_MODIFIED,
        MediaStore.Audio.AudioColumns.SIZE,
        MediaStore.Audio.AudioColumns.MIME_TYPE
    )

    val extCol = if(sdkAtLeast(30))
        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
    else MediaStore.Files.getContentUri("external")

    val vidProj = arrayOf(
        MediaStore.Video.VideoColumns._ID,
        MediaStore.Video.VideoColumns.DISPLAY_NAME,
        MediaStore.Video.VideoColumns.TITLE,
        MediaStore.Video.VideoColumns.ALBUM,
        MediaStore.Video.VideoColumns.DATA,
        MediaStore.Video.VideoColumns.SIZE,
        MediaStore.Video.VideoColumns.MIME_TYPE
    )


    val sndCur = contentResolver.query(extCol, sndProj, null, null, null)
    val vidCur = contentResolver.query(extCol, vidProj, null, null, null)



    sndCur?.close()
    vidCur?.close()
    mediaListingRegisterUpdater()
    mediaListingStarted = true
}

var isMediaListingUpdaterRegistered = false

fun VSH.mediaListingRegisterUpdater(){
    if(isMediaListingUpdaterRegistered) return

    if(sdkAtLeast(Build.VERSION_CODES.N))


    isMediaListingUpdaterRegistered = true
}

fun VSH.mediaListingAdd(uri: Uri){

}