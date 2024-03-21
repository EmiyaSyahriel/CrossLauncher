package id.psw.vshlauncher.types.media

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaDataSource
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import java.io.FileDescriptor
import java.net.HttpCookie

class StatedMediaPlayer(val id : String) : MediaPlayer() {
    companion object{
        const val TAG = "StatedMediaPlayer"
    }

    enum class State(state:Int) {
        Error      (0),
        Idle         (1 shl 0),
        Initialized  (1 shl 1),
        Preparing    (1 shl 2),
        Prepared     (1 shl 3),
        Started      (1 shl 4),
        Paused       (1 shl 5),
        Stopped      (1 shl 6),
        Complete     (1 shl 7),
        End          (1 shl 8)
    }

    private var _state : State = State.Error
    var state get() = _state
        private set (v) {
            val s = _state
            _state = v
            Log.d(TAG, "MediaPlayer $id set from $s to $v state")
        }

    init {
        state = State.Idle

        super.setOnCompletionListener {
            state = State.Complete
        }
        super.setOnPreparedListener {
            state = State.Prepared
        }
        super.setOnErrorListener { _, _, _ ->
            state = State.Error
            true
        }
    }

    override fun setDataSource(afd: AssetFileDescriptor) {
        super.setDataSource(afd)
        state = State.Initialized
    }

    override fun setDataSource(dataSource: MediaDataSource?) {
        super.setDataSource(dataSource)
        state = State.Initialized
    }

    override fun setDataSource(fd: FileDescriptor?) {
        super.setDataSource(fd)
        state = State.Initialized
    }

    override fun setDataSource(path: String?) {
        super.setDataSource(path)
        state = State.Initialized
    }

    override fun setDataSource(context: Context, uri: Uri) {
        super.setDataSource(context, uri)
        state = State.Initialized
    }

    override fun setDataSource(context: Context, uri: Uri, headers: MutableMap<String, String>?) {
        super.setDataSource(context, uri, headers)
        state = State.Initialized
    }

    override fun setDataSource(fd: FileDescriptor?, offset: Long, length: Long) {
        super.setDataSource(fd, offset, length)
        state = State.Initialized
    }

    override fun setDataSource(
        context: Context,
        uri: Uri,
        headers: MutableMap<String, String>?,
        cookies: MutableList<HttpCookie>?
    ) {
        super.setDataSource(context, uri, headers, cookies)
        state = State.Initialized
    }

    override fun setOnErrorListener(listener: OnErrorListener?) {
        return super.setOnErrorListener { mp, what, extra ->
            state = State.Error
            listener?.onError(mp, what, extra) ?: true
        }
    }

    override fun setOnCompletionListener(listener: OnCompletionListener?) {
        super.setOnCompletionListener {
            state = State.Complete
            listener?.onCompletion(it)
        }
    }

    override fun setOnPreparedListener(listener: OnPreparedListener?) {
        super.setOnPreparedListener {
            state = State.Prepared
            listener?.onPrepared(it)
        }
    }

    override fun start() {
        state = State.Started
        super.start()
    }

    override fun stop() {
        state = State.Stopped
        super.stop()
    }

    override fun pause() {
        state = State.Paused
        super.pause()
    }

    override fun reset() {
        state = State.Idle
        super.reset()
    }

    override fun prepareAsync() {
        state = State.Preparing
        super.prepareAsync()
    }

    override fun prepare() {
        state = State.Preparing
        super.prepare()
    }
}