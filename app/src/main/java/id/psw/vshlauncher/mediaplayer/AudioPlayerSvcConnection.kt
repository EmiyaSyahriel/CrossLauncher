package id.psw.vshlauncher.mediaplayer

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

class AudioPlayerSvcConnection : ServiceConnection {
    var player : XMBAudioPlayerService.MediaPlayerBinder? = null

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

    }

    override fun onServiceDisconnected(name: ComponentName?) {
    }

}