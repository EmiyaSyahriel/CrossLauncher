package id.psw.vshlauncher.submodules

import android.os.Build
import com.google.gson.Gson
import id.psw.vshlauncher.BuildConfig
import id.psw.vshlauncher.Logger
import id.psw.vshlauncher.R
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.postNotification
import id.psw.vshlauncher.views.asBytes
import org.threeten.bp.Instant
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class UpdateCheckSubmodule(private val vsh: Vsh) : IVshSubmodule {

    companion object {
        const val GIT_RELEASE_URL = "https://api.github.com/repos/EmiyaSyahriel/CrossLauncher/releases?per_page=1&page=1"
        const val APK_DEBUG_NAME = "-debug.apk";
        const val APK_RELEASE_NAME = "-release.apk";
    }

    /** This User-Agent is intended to be unique, Update only if this UA is banned by GitHub */
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
    private var _updateInfo = ""

    data class GithubReleaseAssetInfo (
        val browser_download_url : String,
        val name : String,
        val size : Long
            )

    @Suppress("ArrayInDataClass")
    data class GithubReleaseInfo(
        val url : String,
        val name : String,
        val created_at : String,
        val body : String,
        val assets : Array<GithubReleaseAssetInfo>
    )

    private class GitHubReleaseInfoArray : ArrayList<GithubReleaseInfo>() {}

    private fun checkThreadFn(){
        _isChecking = true
        val uri = URL(GIT_RELEASE_URL)
        var cn : HttpsURLConnection? = null
        val apkDate = Instant.parse(BuildConfig.BUILD_DATE).plusSeconds(30 * 60) // 30 Minutes after building
        try {
            cn = uri.openConnection() as HttpsURLConnection
            cn.requestMethod = "GET"
            cn.setRequestProperty("Accept", "application/vnd.github+json")
            cn.setRequestProperty("User-Agent", httpUserAgent)

            cn.doInput = true
            val ins = cn.inputStream.bufferedReader().readText()
            val dats = gson.fromJson(ins, GitHubReleaseInfoArray::class.java)
            for(dat in dats){
                val relDate = Instant.parse(dat.created_at)
                if(relDate > apkDate){ // Release is newer than this APK + 30 min
                    for(apk in dat.assets){
                        val apkSuffix = if(BuildConfig.DEBUG){
                            APK_DEBUG_NAME
                        } else{
                            APK_RELEASE_NAME
                        }
                        _hasUpdate = apk.name.endsWith(apkSuffix)
                        if(_hasUpdate){
                            _apkUrl = apk.browser_download_url
                            _updateSize = apk.size.asBytes()
                            break
                        }
                    }
                    if(_hasUpdate){
                        vsh.postNotification(R.drawable.ic_sync_loading, "System Update Found!", "You can check and download the update at Settings > System Update\nor you can open the GitHub Release page and download from there")
                        _updatedAt = dat.created_at
                        _newRelName = dat.name
                        _updateInfo = dat.body
                    }
                }
            }
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