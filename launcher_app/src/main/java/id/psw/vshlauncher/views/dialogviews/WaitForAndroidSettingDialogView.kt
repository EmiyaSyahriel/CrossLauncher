package id.psw.vshlauncher.views.dialogviews

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.provider.Settings
import id.psw.vshlauncher.R
import id.psw.vshlauncher.makeTextPaint
import id.psw.vshlauncher.postNotification
import id.psw.vshlauncher.select
import id.psw.vshlauncher.views.DrawExtension
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.views.wrapText
import id.psw.vshlauncher.xmb
import java.util.Timer
import kotlin.concurrent.timerTask

class WaitForAndroidSettingDialogView(xmb: XmbView, private val displayName: String, private val intentId:String) : XmbDialogSubview(xmb)  {
    companion object {
        const val WAIT_TIME = 1000L
        const val TIMER_ID = "wait_for_setting_window"
    }

    private enum class State {
        Waiting,
        Failed,
        Closing
    }
    private var state = State.Waiting
    private var isTimerCancelled = false
    private val timer = Timer(TIMER_ID)
    private var paint = vsh.makeTextPaint(20.0f, Paint.Align.CENTER)

    override val title: String
        get() = vsh.getString((state == State.Waiting).select(R.string.common_loading, R.string.error_setting_not_found))

    override val hasNegativeButton: Boolean
        get() = state != State.Waiting

    override val hasPositiveButton: Boolean
        get() = state != State.Waiting

    override val negativeButton: String
        get() = vsh.getString(R.string.dlg_confirm_cancel)

    override val positiveButton: String
        get() = vsh.getString(R.string.dlg_confirm_confirm)

    private var lastException = Exception("Unknown Exception is thrown")
    private var started = false

    override fun onStart() {
        if(started) return
        started = true
        timer.schedule(timerTask { onScheduleEnd() }, WAIT_TIME)
        vsh.xmb.onPauseCallbacks.add(::cancelScheduleAndClose)

        launch(intentId, false)
    }

    private fun onScheduleEnd(){
        state = State.Failed
        lastException = Exception("Android failed launch requested intent")
    }

    private fun cancelTimer(){
        if(isTimerCancelled) return
        isTimerCancelled = true

        timer.cancel()
        timer.purge()
    }

    override fun onDialogButton(isPositive: Boolean) {
        if(state == State.Waiting) return

        if(isPositive){
            if(state == State.Failed){
                launch(Settings.ACTION_SETTINGS, true)
            }
            closeDialog()
        }else{
            closeDialog()
        }
    }

    private fun launch(actionSettings: String, closeOnFail : Boolean) {
        val i = Intent(actionSettings)
        if(!actionSettings.startsWith("id.psw.vshlauncher")){
            i.flags = i.flags or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            vsh.xmb.startActivity(i)
        }catch(e:Exception){
            lastException = e
            if(closeOnFail) {
                vsh.postNotification(R.drawable.ic_error, e.javaClass.name, e.localizedMessage ?: "No message", 5.0f)
                closeDialog()
            }
            if(state == State.Waiting){
                state = State.Failed
                cancelTimer()
            }
        }
    }

    private fun closeDialog(){
        val p = vsh.xmbView?.screens?.mainMenu
        if(p != null) finish(p)
    }

    private fun cancelScheduleAndClose(){
        cancelTimer()

        vsh.xmb.onPauseCallbacks.remove(::cancelScheduleAndClose)
        closeDialog()
    }

    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime: Float) {
        val txt = when(state){
            State.Waiting -> vsh.getString(R.string.common_loading)
            State.Closing -> vsh.getString(R.string.error_setting_wait_dialog_closing_msg)
            State.Failed -> {
                vsh.getString(R.string.error_setting_not_found_description).format(displayName, "${lastException.javaClass.name} - ${lastException.message}")
            }
        }

        val lines = paint.wrapText(txt, (drawBound.width() - 300.0f).coerceIn(500.0f, 1000.0f)).lines()

        val lineSize = paint.textSize * 1.25f
        val topY = drawBound.centerY () - ((lineSize * lines.size) / 2)
        lines.forEachIndexed { i, s ->
            val y = (i * lineSize) + topY
            ctx.drawText(s, drawBound.centerX(), y, paint)
        }
    }
}