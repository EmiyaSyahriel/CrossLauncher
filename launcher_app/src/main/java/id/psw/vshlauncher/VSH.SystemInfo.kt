package id.psw.vshlauncher

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

class VSHSystemInfo {
    var batteryLevel : Float = 0.0f
    var isCharging : Boolean = false
}

private val vshSystemInfo = VSHSystemInfo()

fun VSH.updateBatteryInfo(){
    // TODO : Old API, change to Listener one
    val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val bStat = registerReceiver(null, intentFilter)
    val level = bStat?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
    val scale = bStat?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
    val chargeState = bStat?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    vshSystemInfo.isCharging = chargeState == BatteryManager.BATTERY_STATUS_CHARGING || chargeState == BatteryManager.BATTERY_STATUS_FULL
    vshSystemInfo.batteryLevel = level.toFloat() / scale.toFloat()
}

fun VSH.getBatteryLevel() : Float = vshSystemInfo.batteryLevel
fun VSH.isBatteryCharging() : Boolean = vshSystemInfo.isCharging