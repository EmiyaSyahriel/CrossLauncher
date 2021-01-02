package id.psw.vshlauncher.mediaplayer

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.os.Binder
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationManagerCompat
import id.psw.vshlauncher.mediaplayer.livevisualizer.ILiveVisualizer
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

// TODO: Bind it to main activity
class XMBAudioPlayerService : Service() {

    companion object{
        private val TAG = "musicplayer.self"
        var isMediaLoading = false
        var mediaArtist = ""
        var mediaTitle = ""
        var currentTime = 0L
        var duration = 0L
        var visImage : ILiveVisualizer? = null
        var shuffled = false
        var repeatMode = false
        var playingSomething = false
        var allFiles : ArrayList<String> = arrayListOf()
        var currentPlaylist : ArrayList<String> = arrayListOf()
        var playlistNumber : Int = 0
    }

    object Actions {
        const val PLAY = "id.psw.vshlauncher.mediaplayer.commands.play"
        const val PAUSE = "id.psw.vshlauncher.mediaplayer.commands.pause"
        const val STOP = "id.psw.vshlauncher.mediaplayer.commands.stop"
        const val NEXT = "id.psw.vshlauncher.mediaplayer.commands.next"
        const val PREV = "id.psw.vshlauncher.mediaplayer.commands.previous"
        const val SET_PLAYLIST = "id.psw.vshlauncher.mediaplayer.commands.set_playlist"
        const val SELECT_TRACK = "id.psw.vshlauncher.mediaplayer.commands.tracknumber"
        const val SEEK_FORWARD = "id.psw.vshlauncher.mediaplayer.commands.ffwd"
        const val SEEK_BACKWARD = "id.psw.vshlauncher.mediaplayer.commands.fbwd"
        const val SEEK_ASSIGN = "id.psw.vshlauncher.mediaplayer.commands.seek"
    }

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var visualizer: Visualizer
    private var bindContext = MediaPlayerBinder()
    private var timer = Timer("")
    private lateinit var notification : Notification
    private lateinit var normalNotifView : RemoteViews
    private lateinit var expandedNotifView : RemoteViews
    private lateinit var notifManager : NotificationManagerCompat
    private lateinit var instance : XMBAudioPlayerService

    override fun onCreate() {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnPreparedListener { mediaPlayer.start() }
        visualizer= Visualizer(mediaPlayer.audioSessionId)
        timer.scheduleAtFixedRate(0L, 16L){ mUpdate() }
        instance = this
    }

    inner class MediaPlayerBinder : Binder() {
        fun getService() : XMBAudioPlayerService? {
            return instance
        }
    }

    inner class MediaPlayerEventReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent != null) onReceiveCommand(intent)
        }
    }

    override fun onBind(intent: Intent): IBinder{ return bindContext }

    override fun onDestroy() {
        timer.cancel()
        timer.purge()
    }

    private fun mUpdate(){
    }

    private fun onReceiveCommand(intent: Intent){
        when(intent.action){
            Actions.NEXT -> nextAudio()
        }
    }

    private fun nextAudio(){
        if(currentPlaylist.size < playlistNumber + 1) playlistNumber++
    }

    private fun prevAudio(){
        if(currentPlaylist.size > 1) playlistNumber--
    }

    private fun setAudioSource(){

    }

    private fun playMedia(){

    }
}
