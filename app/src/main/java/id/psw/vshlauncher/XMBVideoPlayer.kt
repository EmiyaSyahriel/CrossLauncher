package id.psw.vshlauncher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.VideoView
import java.io.File

class XMBVideoPlayer : AppCompatActivity() {

    private lateinit var vp : VideoView
    private lateinit var control : VSHVideoControl
    private var playerState = MediaPlayerState.Uninitialized

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xmbvp)

        vp = findViewById<VideoView>(R.id.videoplayer)
        control = findViewById<VSHVideoControl>(R.id.vshcon)

        attachEventListeners()

        val path = intent.data?.path ?: ""
        if(File(path).exists()){
            vp.setVideoPath(path)
        }else{
            overridePendingTransition(R.anim.anim_ps3_zoomfadein, R.anim.anim_ps3_zoomfadeout)
            finish()
        }
    }

    private fun attachEventListeners(){
        control.activity = this
        vp.setOnPreparedListener { playerState = MediaPlayerState.Prepared }
        vp.setOnCompletionListener { playerState = MediaPlayerState.Completed }
        vp.setOnErrorListener { _, _, _ -> playerState = MediaPlayerState.Error; false}
    }

    private fun playerPause(){ if(vp.canPause()) vp.pause() }
    private fun playerPlay (){ if(vp.currentPosition == 0) vp.start() else vp.resume()}
    private fun playerStop (){ vp.stopPlayback() }
    private fun playerSeekChecked(second : Float){
        val newTime = vp.currentPosition + (second * 1000).toInt()
        if(newTime > 0 && newTime < vp.duration && playerState == MediaPlayerState.Prepared){
            vp.seekTo(newTime)
        }
    }
    private fun playerFwd  (){ vp.seekTo(vp.currentPosition + 10000)}
    private fun playerBwd  (){ vp.seekTo(vp.currentPosition - 10000)}
}