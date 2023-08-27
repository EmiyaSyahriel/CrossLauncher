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
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.vsh
import kotlin.concurrent.timer

class NetworkSubmodule(private val ctx: VSH) : IVshSubmodule {
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

    override fun onCreate() {
        noInternet = ctx.getString(R.string.nettype_no_connection)
        timer("vsh_mobile_net_update", false, 0L, 1000L){
            updateNetworkName(ctx)
        }
    }

    override fun onDestroy() {
        // TODO : Nothing, stop the timer maybe?
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

    private fun updateNetworkName(ctx:VSH){
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

        fun useLan(netMan : ConnectivityManager){
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
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1){
                if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    val subscriptMan = ctx.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                    val count = subscriptMan.activeSubscriptionInfoCount
                    if(count > 1){
                        for(i in 0 until count){
                            val isLast = (i == count - 1)
                            try{
                                val subInfo = subscriptMan.getActiveSubscriptionInfo(i)
                                sb.append(subInfo.displayName)
                                if(!isLast) sb.append(" | ")
                            }catch(e:Exception){ }
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
                        ConnectivityManager.TYPE_ETHERNET -> useLan(connectMan)
                    }
                }
            }else{
                isNetworkConnected = false
            }
        }
        isQuerying= false
    }

}