package id.psw.vshlauncher.views

import android.os.SystemClock

data class TimeData (
    var lastRenderTime : Long = 0L,
    var deltaTime: Float = 0.0f,
    var currentTime: Float = 0.0f,
    var deltaTimeDbl: Double = 0.0,
    var currentTimeDbl: Double = 0.0
        ){
    fun tickTime(v: XmbView){
        if(lastRenderTime == 0L){
            lastRenderTime = SystemClock.elapsedRealtime()
        }
        val currentMs = SystemClock.elapsedRealtime()
        val deltaMs = currentMs - lastRenderTime
        lastRenderTime = currentMs
        deltaTime = deltaMs * 0.001f
        currentTime += deltaTime
        deltaTimeDbl = deltaMs * 0.001
        currentTimeDbl += deltaTimeDbl

        v.activeScreen.currentTime += deltaTime
    }
}
