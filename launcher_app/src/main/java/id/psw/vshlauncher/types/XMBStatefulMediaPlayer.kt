package id.psw.vshlauncher.types

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import id.psw.vshlauncher.select
import java.io.FileDescriptor

class XMBStatefulMediaPlayer {
    private lateinit var pMediaPlayer : MediaPlayer
    val mediaPlayer : MediaPlayer get() = pMediaPlayer
    private enum class Status(v:Int){
        ERROR(-1),
        IDLE(0),
        INITIALIZED(2),
        PREPARED(2),
        PLAYING(3),
        PAUSED(4),
        COMPLETE(5)
    }

    private var status = Status.IDLE
    var currentTime : Int
        get() = (status >= Status.PREPARED).select(pMediaPlayer.currentPosition, -1)
        set(v) {
            if ((status >= Status.PREPARED)) pMediaPlayer.seekTo(v)
        }

    val duration get() = (status >= Status.PREPARED).select(pMediaPlayer.duration, -1)
    private var pVolume : Float = 1.0f
    var isLooping get() = pMediaPlayer.isLooping
    set(v) { pMediaPlayer.isLooping = v }

    val isPlaying get() = status == Status.PLAYING

    var volume
    get() = pVolume
    set(v) {
        if(status >= Status.PREPARED) {
            pVolume = v.coerceIn(0f, 1f)
            pMediaPlayer.setVolume(pVolume, pVolume)
        }
    }

    fun setDataSource(path:String) {
        if(status != Status.INITIALIZED){
            pMediaPlayer.setDataSource(path)
            startPrepare()
        }
    }

    fun setDataSource(fd:FileDescriptor) {
        if(status != Status.INITIALIZED) {
            pMediaPlayer.setDataSource(fd)
            startPrepare()
        }
    }

    fun setDataSource(ctx:Context, uri:Uri) {
        if(status != Status.INITIALIZED) {
            pMediaPlayer.setDataSource(ctx, uri)
            startPrepare()
        }
    }

    private fun startPrepare(){
        status = Status.INITIALIZED
        pMediaPlayer.prepareAsync()
    }

    constructor(context: Context, uri: Uri){
        pMediaPlayer = MediaPlayer.create(context, uri)
        registerEvents()
        startPrepare()
    }

    constructor(context: Context, id:Int){
        pMediaPlayer = MediaPlayer.create(context, id)
        registerEvents()
        startPrepare()
    }

    private fun registerEvents(){
        pMediaPlayer.setOnCompletionListener { status = Status.COMPLETE }
        pMediaPlayer.setOnErrorListener { mp, what, extra ->
            status = Status.ERROR
            true
        }
        pMediaPlayer.setOnInfoListener { mp, what, extra ->
            true
        }
        pMediaPlayer.setOnPreparedListener {
            status = Status.PREPARED
        }
    }

    constructor(){
        pMediaPlayer = MediaPlayer()
        registerEvents()
    }

    fun release(){
        if(status >= Status.ERROR){
            if(status == Status.PLAYING){
                stop()
            }
            pMediaPlayer.reset()
            pMediaPlayer.release()
        }
    }

    fun start(){
        if(status >= Status.PREPARED){
            pMediaPlayer.start()
            status = Status.PLAYING
        }
    }

    fun stop(){
        if(status == Status.PLAYING){
            pMediaPlayer.stop()
            status = Status.PREPARED
        }

    }

    fun pause(){
        if(status == Status.PLAYING){
            pMediaPlayer.stop()
            status = Status.PAUSED
        }
    }
}