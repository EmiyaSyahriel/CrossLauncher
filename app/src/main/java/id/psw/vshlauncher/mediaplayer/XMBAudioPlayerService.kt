package id.psw.vshlauncher.mediaplayer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.os.Binder
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import id.psw.vshlauncher.R
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

        const val ACTION_MEDIA_PLAY = "id.psw.vshlauncher.mediaplayer.ACTION_MEDIA_PLAY"
        const val ACTION_MEDIA_STOP = "id.psw.vshlauncher.mediaplayer.ACTION_MEDIA_STOP"
        const val ACTION_MEDIA_NEXT = "id.psw.vshlauncher.mediaplayer.ACTION_MEDIA_NEXT"
        const val ACTION_MEDIA_PREV = "id.psw.vshlauncher.mediaplayer.ACTION_MEDIA_PREV"
        const val ACTION_MEDIA_FFWD = "id.psw.vshlauncher.mediaplayer.ACTION_MEDIA_FFWD"
        const val ACTION_MEDIA_FBWD = "id.psw.vshlauncher.mediaplayer.ACTION_MEDIA_FBWD"
        const val ACTION_MEDIA_DISP = "id.psw.vshlauncher.mediaplayer.ACTION_MEDIA_DISP"
        const val NOTIFICATION_ID = "id.psw.vshlauncher.mediaplayer.ACTION_MEDIA_FBWD"
    }

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var visualizer: Visualizer
    private var bindContext = MediaPlayerBinder()
    private var instance : XMBAudioPlayerService = this
    private var timer = Timer("")
    private lateinit var notification : Notification
    private lateinit var normalNotifView : RemoteViews
    private lateinit var expandedNotifView : RemoteViews
    private lateinit var notifManager : NotificationManagerCompat

    override fun onCreate() {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnPreparedListener { mediaPlayer.start() }
        visualizer= Visualizer(mediaPlayer.audioSessionId)
        timer.scheduleAtFixedRate(0L, 16L){ mUpdate() }
        createNotif()

    }

    private fun createNotif(){
        normalNotifView = RemoteViews(packageName, R.layout.notification_xmbmp_normal)
        expandedNotifView = RemoteViews(packageName, R.layout.notification_xmbmp_expanded)
        notifManager = NotificationManagerCompat.from(this)

        val playPauseIntent = Intent(this, MediaPlayerBroadcastReceiver::class.java)
            .apply {
            action = ACTION_MEDIA_PLAY
            putExtra("NOTIF_ID", 0)
        }
        val nextIntent = Intent(this, MediaPlayerBroadcastReceiver::class.java)
            .apply {
            action = ACTION_MEDIA_NEXT
            putExtra("NOTIF_ID", 0)
        }
        val prevIntent = Intent(this, MediaPlayerBroadcastReceiver::class.java)
            .apply {
            action = ACTION_MEDIA_PREV
            putExtra("NOTIF_ID", 0)
        }
        val ffwdIntent = Intent(this, MediaPlayerBroadcastReceiver::class.java)
            .apply {
            action = ACTION_MEDIA_FFWD
            putExtra("NOTIF_ID", 0)
        }
        val fbwdIntent = Intent(this, MediaPlayerBroadcastReceiver::class.java)
            .apply {
            action = ACTION_MEDIA_FBWD
            putExtra("NOTIF_ID", 0)
        }
        val displayIntent = Intent(this, MediaPlayerBroadcastReceiver::class.java)
            .apply {
            action = ACTION_MEDIA_DISP
            putExtra("NOTIF_ID", 0)
        }
        val stopIntent = Intent(this, MediaPlayerBroadcastReceiver::class.java).
        apply {
            action = ACTION_MEDIA_STOP
            putExtra("NOTIF_ID", 0)
        }

        expandedNotifView.setOnClickPendingIntent(R.id.mp_btn_play, PendingIntent.getBroadcast(this, 0, playPauseIntent, 0))

        notification = NotificationCompat
            .Builder(this, "VSH Media Player")
            .setCustomBigContentView(expandedNotifView)
            .setCustomContentView(normalNotifView)
            .build()
        notification.flags = Notification.FLAG_ONGOING_EVENT
    }

    inner class MediaPlayerBinder : Binder() {
        fun getService() : XMBAudioPlayerService{
            return instance
        }
    }

    inner class MediaPlayerBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

        }
    }

    override fun onBind(intent: Intent): IBinder{ return bindContext }

    override fun onDestroy() {
        timer.cancel()
        timer.purge()
    }

    private fun mUpdate(){
    }

    private fun mNotifUpdate(){

    }
}
