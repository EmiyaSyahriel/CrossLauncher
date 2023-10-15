package id.psw.vshlauncher.submodules

import android.os.Build
import com.google.gson.Gson
import id.psw.vshlauncher.BuildConfig
import id.psw.vshlauncher.Vsh
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class UpdateCheckSubmodule(private val vsh: Vsh) : IVshSubmodule {

    companion object {
        const val GIT_RELEASE_URL = "https://api.github.com/repos/EmiyaSyahriel/CrossLauncher/releases?per_page=1&page=1"
    }

    private val httpUserAgent get()= "CrossLauncher/${BuildConfig.VERSION_NAME} (PLAYSTATION 3; Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}))"
    private val gson = Gson()

    override fun onCreate() {
        beginCheck()
    }

    fun beginCheck(){
        vsh.threadPool.execute (::checkThreadFn)
    }

    private var _isChecking = false
    private var _hasUpdate = false
    private var _apkUrl = ""
    private var _newRelName = ""
    private var _updatedAt = ""
    private var _updateSize = ""

    data class GithubReleaseAssetInfo (
        val browser_download_url : String,
        val name : String,
        val size : Long
            )

    @Suppress("ArrayInDataClass")
    data class GithubReleaseInfo(
        val url : String,
        val name : String,
        val publishedAt : String,
        val assets : Array<GithubReleaseAssetInfo>
    )

    private fun checkThreadFn(){
        _isChecking = true
        val uri = URL(GIT_RELEASE_URL)
        var cn : HttpsURLConnection? = null
        try {
            cn = uri.openConnection() as HttpsURLConnection
            cn.requestMethod = "GET"
            cn.setRequestProperty("Accept", "application/vnd.github+json")
            cn.setRequestProperty("User-Agent", httpUserAgent)

            cn.doInput = true
            val ins = cn.inputStream.bufferedReader()
            val dat = gson.fromJson(ins, GithubReleaseInfo::class.java)
            dat.publishedAt
        }catch(e:Exception){
            e.printStackTrace()
        }finally {
            cn?.disconnect()
        }
        _isChecking = false
    }

    override fun onDestroy() {

    }
}