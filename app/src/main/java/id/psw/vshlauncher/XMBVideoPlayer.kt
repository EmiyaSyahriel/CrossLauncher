package id.psw.vshlauncher

import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.VideoView
import java.io.File

class XMBVideoPlayer : AppCompatActivity() {

    private val TAG = "video.self"
    lateinit var vp : VideoView
    private lateinit var control : VSHVideoControl
    private var playerState = MediaPlayerState.Uninitialized
    var path = ""
    var selection = Point(0,0)
    var selectionControl = mapOf(
        Pair(Point( 0,0), ControlRenderable(0, true, R.string.videoplayer_stop, Runnable{ playerStop() })),
        Pair(Point(-1,0), ControlRenderable(1, true, R.string.videoplayer_pause, Runnable{ playerPause()})),
        Pair(Point(-2,0), ControlRenderable(2, true, R.string.videoplayer_play, Runnable{ playerPlay() })),
        Pair(Point(-3,0), ControlRenderable(3, true, R.string.app_name, Runnable{ playerFwd()  })),
        Pair(Point(-4,0), ControlRenderable(4, true, R.string.app_name, Runnable{ playerBwd()  }))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xmbvp)

        vp = findViewById<VideoView>(R.id.videoplayer)
        control = findViewById<VSHVideoControl>(R.id.vshcon)

        attachEventListeners()

        path = intent.data?.path ?: CurrentAppData.selectedVideoPath
        if(File(path).exists()){
            vp.setVideoPath(path)
        }else{
            overridePendingTransition(R.anim.anim_ps3_zoomfadein, R.anim.anim_ps3_zoomfadeout)
            finish()
        }

    }

    private fun moveSelection(x:Int, y:Int){
        val newPoint = Point(selection.x + x, selection.y + y)
        if(selectionControl.containsKey(newPoint)){
            selection = newPoint
        }
    }

    private fun runSelection(){
        selectionControl[selection]?.runnable?.run()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when(keyCode){
            KeyEvent.KEYCODE_DPAD_LEFT -> { moveSelection(-1,0) }
            KeyEvent.KEYCODE_DPAD_UP -> { moveSelection(0,-1) }
            KeyEvent.KEYCODE_DPAD_RIGHT -> { moveSelection(1,0) }
            KeyEvent.KEYCODE_DPAD_DOWN -> { moveSelection(0,-1) }
            KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_ENTER -> { runSelection() }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun attachEventListeners(){
        control.activity = this
        vp.setOnPreparedListener {
            Log.d(TAG, "Media Player is prepared")
            playerState = MediaPlayerState.Prepared
        }
        vp.setOnCompletionListener {
            Log.d(TAG, "Media Player completed")
            playerState = MediaPlayerState.Completed
        }
        vp.setOnErrorListener { _, _, _ ->
            Log.d(TAG, "Media Player Error")
            playerState = MediaPlayerState.Error; false
        }
    }

    private fun playerPause(){ Log.d(TAG, "Pause..."); if(vp.canPause()) vp.pause() }
    private fun playerPlay (){
        Log.d(TAG, "Playing...")
        if(vp.currentPosition == 0) vp.start() else vp.resume()
    }
    private fun playerStop (){ Log.d(TAG, "Stop..."); vp.stopPlayback() }
    private fun playerSeekChecked(second : Float){
        val newTime = vp.currentPosition + (second * 1000).toInt()
        if(newTime > 0 && newTime < vp.duration && playerState == MediaPlayerState.Prepared){
            vp.seekTo(newTime)
        }
    }
    private fun playerFwd  (){ vp.seekTo(vp.currentPosition + 10000)}
    private fun playerBwd  (){ vp.seekTo(vp.currentPosition - 10000)}
}