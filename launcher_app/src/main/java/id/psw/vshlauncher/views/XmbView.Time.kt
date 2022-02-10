package id.psw.vshlauncher.views

import android.os.SystemClock

data class VshViewTimeData (
    var lastRenderTime : Long = 0L,
    var deltaTime: Float = 0.0f,
    var currentTime: Float = 0.0f,
    var deltaTimeDbl: Double = 0.0,
    var currentTimeDbl: Double = 0.0
        )

fun XmbView.tickTime(){
    if(time.lastRenderTime == 0L){
        time.lastRenderTime = SystemClock.elapsedRealtime()
    }
    val currentMs = SystemClock.elapsedRealtime()
    val deltaMs = currentMs - time.lastRenderTime
    time.lastRenderTime = currentMs
    time.deltaTime = deltaMs * 0.001f
    time.currentTime += time.deltaTime
    time.deltaTimeDbl = deltaMs * 0.001
    time.currentTimeDbl += time.deltaTimeDbl

    when(currentPage){
        VshViewPage.GameBoot -> state.gameBoot.currentTime += time.deltaTime
        VshViewPage.ColdBoot -> state.coldBoot.currentTime += time.deltaTime
        VshViewPage.MainMenu -> state.crossMenu.currentTime += time.deltaTime
    }
}