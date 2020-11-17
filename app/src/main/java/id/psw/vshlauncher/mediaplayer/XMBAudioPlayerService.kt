package id.psw.vshlauncher.mediaplayer

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.os.Binder
import android.os.IBinder

// TODO: Bind it to main activity
class XMBAudioPlayerService : Service() {

    companion object{
        private val TAG = "musicplayer.self"
        var isMediaLoading = false
        var mediaArtist = ""
        var mediaTitle = ""
    }

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var visualizer: Visualizer
    private var bindContext = MediaPlayerBinder()
    private var instance : XMBAudioPlayerService = this

    override fun onCreate() {
        mediaPlayer = MediaPlayer()
        visualizer= Visualizer(mediaPlayer.audioSessionId)
    }

    inner class MediaPlayerBinder : Binder() {
        fun getService() : XMBAudioPlayerService{
            return instance
        }
    }

    override fun onBind(intent: Intent): IBinder{ return bindContext }
}
