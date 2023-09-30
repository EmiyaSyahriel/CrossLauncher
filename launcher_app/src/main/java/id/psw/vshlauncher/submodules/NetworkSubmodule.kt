package id.psw.vshlauncher.submodules

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import id.psw.vshlauncher.R
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.sdkAtLeast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NetworkSubmodule(private val ctx: Vsh) : IVshSubmodule {
    private var isNetworkConnected = false
    private var isQuerying = false
    private var isEthernet = false
    private var isWifi = false
    private var isMobile = false
    private var etherName = "Ethernet"
    private var simName = ""
    private var wifiName = ""
    private var isQueryingStr = "..."
    private var noInternet = " ... "
    private var keepUpdateCoroutineRunning = true
    private var hasSubscriptionManager = false
    private lateinit var subscriptMan : SubscriptionManager

    override fun onCreate() {
        noInternet = ctx.getString(R.string.nettype_no_connection)
        if (sdkAtLeast(Build.VERSION_CODES.LOLLIPOP_MR1)) {
            subscriptMan = ctx.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            hasSubscriptionManager = true
        }
        ctx.lifeScope.launch {
            keepUpdateCoroutineRunning = true
            withContext(Dispatchers.IO){
                while(keepUpdateCoroutineRunning){
                    try {
                        updateNetworkName(ctx)
                    }catch(_:Exception) {}
                    delay(1000L)
                }
            }
        }
    }

    override fun onDestroy() {
        keepUpdateCoroutineRunning = false
    }

    val operatorName : String get() {
        return when {
            isQuerying -> isQueryingStr
            isNetworkConnected -> {
                when {
                    isEthernet -> etherName
                    isWifi -> wifiName
                    isMobile -> simName
                    else -> ""
                }
            }
            else -> noInternet
        }
    }

    // Still using deprecated API, if there is newer API that can be used without library
    // Then please edit this code
    @Suppress("DEPRECATION")
    private fun updateNetworkName(ctx:Vsh){
        isQueryingStr = operatorName
        isQuerying= true
        val sb = StringBuilder()

        isWifi = false
        isEthernet = false
        isMobile = false

        fun useWifi(){
            isWifi = true
            if(ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED){
                val wifiMan= ctx.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo = wifiMan.connectionInfo
                wifiName = if(wifiInfo.ssid != WifiManager.UNKNOWN_SSID) {
                    wifiInfo.ssid.removePrefix("\"").removeSuffix("\"")
                }else{
                    ctx.getString(R.string.nettype_wifi)
                }
            }
        }

        fun useLan(){
            etherName = ctx.getString(R.string.nettype_ethernet)
            isEthernet = true
        }

        fun useSingleSim(){
            isMobile = true
            val teleMan = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            sb.append(teleMan.simOperatorName)
        }

        fun useMobile(){
            isMobile = true
            if(sdkAtLeast(Build.VERSION_CODES.LOLLIPOP_MR1) && hasSubscriptionManager){
                if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    val count = subscriptMan.activeSubscriptionInfoCount
                    if(count > 1){
                        for(i in 0 until count){
                            val isLast = (i == count - 1)
                            try{
                                val subInfo = subscriptMan.getActiveSubscriptionInfo(i)
                                sb.append(subInfo.displayName)
                                if(!isLast) sb.append(" | ")
                            }catch(_:Exception){ }
                        }
                    }else{
                        useSingleSim()
                    }
                }else{
                    useSingleSim()
                }
            }else{
                useSingleSim()
            }
            simName = sb.toString()
        }

        if(ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED){
            val connectMan = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = connectMan.activeNetworkInfo
            if(netInfo != null){
                if(netInfo.isConnected){
                    isNetworkConnected = true
                    when(netInfo.type){
                        ConnectivityManager.TYPE_WIFI -> useWifi()
                        ConnectivityManager.TYPE_MOBILE -> useMobile()
                        ConnectivityManager.TYPE_ETHERNET -> useLan()
                    }
                }
            }else{
                isNetworkConnected = false
            }
        }
        isQuerying= false
    }

}