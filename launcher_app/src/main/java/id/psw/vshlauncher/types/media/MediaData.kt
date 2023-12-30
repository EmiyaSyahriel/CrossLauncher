package id.psw.vshlauncher.types.media

import android.net.Uri

open class MediaData (
    open val id : Long,
    open val uri: Uri,
    open val data : String
    )

data class MusicData (
    override val id : Long,
    override val uri: Uri,
    override val data : String,
    val title : String,
    val album : String,
    val artist : String,
    val size : Long,
    val duration : Long,
    val mime : String,
) : MediaData(id, uri, data)

data class VideoData (
    override val id : Long,
    override val uri: Uri,
    override val data : String,
    val displayName : String,
    val size : Long,
    val duration : Long,
    val mime : String,
) : MediaData(id, uri, data)