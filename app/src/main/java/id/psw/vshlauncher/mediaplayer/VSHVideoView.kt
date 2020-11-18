package id.psw.vshlauncher.mediaplayer

import android.content.Context
import android.graphics.Point
import android.media.MediaMetadataRetriever
import android.util.AttributeSet
import android.widget.VideoView

class VSHVideoView : VideoView {

    constructor(context: Context): super(context){ }
    constructor(context: Context,attributeSet: AttributeSet): super(context, attributeSet){ }
    constructor(context: Context, attributeSet: AttributeSet, defStyle:Int):super(context, attributeSet, defStyle){}
    private val mmr = MediaMetadataRetriever()

    override fun setVideoPath(path: String) {
        super.setVideoPath(path)
        mmr.setDataSource(path)
    }

    fun getVideoResoultion() : Point{
        val wStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        val hStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        val w = wStr.toIntOrNull() ?: 854
        val h = hStr.toIntOrNull() ?: 480
        return Point(w,h)
    }
}