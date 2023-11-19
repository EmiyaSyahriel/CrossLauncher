package id.psw.vshlauncher.types.media

data class MusicData (
    val id : Long,
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
    val displayName : String,
    val data : String,
    val size : Long,
    val duration : Long,
    val mime : String,
)