package id.psw.vshlauncher.types.media

import android.net.Uri

data class MusicData (
    val id : Long,
    val uri: Uri,
    val data : String,
    val title : String,
    val album : String,
    val artist : String,
    val size : Long,
    val duration : Long,
    val mime : String,
)

data class VideoData (
    val id : Long,
    val uri: Uri,
    val displayName : String,
    val data : String,
    val size : Long,
    val duration : Long,
    val mime : String,
)