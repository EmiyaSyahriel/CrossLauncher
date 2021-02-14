package id.psw.vshlauncher.mediaplayer

import android.graphics.Point
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import id.psw.vshlauncher.ControlRenderable
import id.psw.vshlauncher.CurrentAppData
import id.psw.vshlauncher.MediaPlayerState
import id.psw.vshlauncher.R
import java.io.File

class XMBVideoPlayer : AppCompatActivity() {

    private val TAG = "video.self"
    lateinit var vp : VSHVideoView
    lateinit var scaler : VideoScalableViewGroup
    private lateinit var control : VSHVideoControl
    private var playerState = MediaPlayerState.Uninitialized
    var path = ""
    var selection = Point(0, 0)
    var selectionControl = mapOf(
        Pair(
            Point(0, 0), ControlRenderable(
                0x00,
                true,
                R.string.videoplayer_stop,
                Runnable { playerStop() })
        ),
        Pair(
            Point(-1, 0), ControlRenderable(
                0x01,
                true,
                R.string.videoplayer_pause,
                Runnable { playerPause() })
        ),
        Pair(
            Point(-2, 0), ControlRenderable(
                0x02,
                true,
                R.string.videoplayer_play,
                Runnable { playerPlay() })
        ),
        Pair(
            Point(-3, 0), ControlRenderable(
                0x03,
                true,
                R.string.videoplayer_fwd,
                Runnable { playerFwd() })
        ),
        Pair(
            Point(-4, 0), ControlRenderable(
                0x04,
                true,
                R.string.videoplayer_bwd,
                Runnable { playerBwd() })
        ),
        Pair(
            Point(5, -1), ControlRenderable(
                0x05,
                true,
                R.string.videoplayer_switchdisplay,
                Runnable { switchControlDisplay() })
        ),
        Pair(
            Point(4, -1), ControlRenderable(
                0x06,
                false,
                R.string.videoplayer_delete,
                Runnable { deleteVideoFile() })
        ),
        Pair(
            Point(3, -1), ControlRenderable(
                0x07,
                false,
                R.string.videoplayer_setthumbnail,
                Runnable { playerSetCurrentAsThumbnail() })
        ),
        Pair(
            Point(2, -1), ControlRenderable(
                0x08,
                true,
                R.string.videoplayer_switchar,
                Runnable { playerChangeAR() })
        ),
        Pair(
            Point(1, -1), ControlRenderable(
                0x09,
                false,
                R.string.videoplayer_viewsetting,
                Runnable { playerSwitchVideoSettingWidget() })
        ),
        Pair(
            Point(0, -1), ControlRenderable(
                0x10,
                false,
                R.string.videoplayer_volume,
                Runnable { playerSwitchVolumeWidget() })
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xmbvp)

        vp = findViewById(R.id.videoplayer)
        control = findViewById(R.id.vshcon)
        scaler = findViewById(R.id.scaler)

        makeNoTitle()
        makeFullScreen()
        attachEventListeners()

        path = intent.data?.path ?: CurrentAppData.selectedVideoPath
        if(File(path).exists()){
            vp.setVideoPath(path)
        }else{
            exit()
        }
    }

    private fun exit(){
        finish()
        overridePendingTransition(R.anim.anim_ps3_zoomfadein, R.anim.anim_ps3_zoomfadeout)
    }

    override fun onBackPressed() {
        exit()
    }

    override fun onStart() {
        super.onStart()
        makeNoTitle()
        makeFullScreen()
    }

    override fun onResume() {
        super.onResume()
        makeNoTitle()
        makeFullScreen()
    }

    private fun makeNoTitle() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= 29) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    private fun makeFullScreen() {
        if (Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }
    private fun moveSelection(x: Int, y: Int){
        val newPoint = Point(selection.x + x, selection.y + y)
        if(selectionControl.containsKey(newPoint)){
            selection = newPoint
        }
    }

    private fun runSelection(){
        val sel = selectionControl[selection]
        if(sel?.enabled == true){
            selectionControl[selection]?.runnable?.run()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when(keyCode){
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                moveSelection(-1, 0)
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                moveSelection(0, -1)
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                moveSelection(1, 0)
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                moveSelection(0, 1)
            }
            KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_ENTER -> {
                runSelection()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun attachEventListeners(){
        control.activity = this
        vp.setOnPreparedListener {
            Log.d(TAG, "Media Player is prepared")
            playerState = MediaPlayerState.Prepared
            playerPlay()
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

    var lastPos = PointF(0f, 0f)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var retval= false
        if(event != null){
            when(event.actionMasked){
                MotionEvent.ACTION_MOVE, MotionEvent.ACTION_HOVER_MOVE -> {
                    var xMove = event.x - lastPos.x
                    var yMove = event.y - lastPos.y

                }
            }
        }
        return retval || super.onTouchEvent(event)
    }

    private fun switchControlDisplay() {
        control.isVisible = !control.isVisible
    }

    private fun deleteVideoFile() {
        // TODO: Implement video delete dialog
        Log.d(TAG, "TODO: Implement video delete dialog")
    }
    private fun playerSwitchVideoSettingWidget(){
        // TODO: Implement Switch Video Setting Widget
        Log.d(TAG, "TODO: Switch Video Setting Widget")
    }

    private fun playerSwitchVolumeWidget(){
        // TODO: Implement Switch Volume Settings
        Log.d(TAG, "TODO: Switch Volume Widget")
    }

    private fun playerSetCurrentAsThumbnail() {
        // TODO: Implement Set current time as Thumbnail
        Log.d(TAG, "TODO: Implement Set current time as Thumbnail")
    }

    private var pauseLocation = 0

    private fun playerPause(){
        Log.d(TAG, "Pause...")
        pauseLocation = vp.currentPosition
        vp.pause()
    }

    private fun playerPlay (){
        Log.d(TAG, "Playing...")
        vp.seekTo(pauseLocation)
        vp.start()
    }
    private fun playerChangeAR(){
        Log.d(TAG, "Switch AR...")
        scaler.switchScaling()
    }
    private fun playerStop (){ Log.d(TAG, "Stop..."); vp.stopPlayback() }
    private fun playerSeekChecked(second: Float){
        val newTime = vp.currentPosition + (second * 1000).toInt()
        if(newTime > 0 && newTime < vp.duration && playerState == MediaPlayerState.Prepared){
            vp.seekTo(newTime)
        }
    }
    private fun playerFwd  (){ vp.seekTo(vp.currentPosition + 10000)}
    private fun playerBwd  (){ vp.seekTo(vp.currentPosition - 10000)}
}