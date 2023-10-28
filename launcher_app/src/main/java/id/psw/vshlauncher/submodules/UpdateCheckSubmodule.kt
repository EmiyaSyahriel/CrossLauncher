package id.psw.vshlauncher.submodules

import android.content.Intent
import android.net.Uri
import android.os.Build
import com.google.gson.Gson
import id.psw.vshlauncher.BuildConfig
import id.psw.vshlauncher.R
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.postNotification
import id.psw.vshlauncher.views.asBytes
import kotlinx.coroutines.sync.Mutex
import org.threeten.bp.Instant
import java.io.File
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
        apkFile = File(vsh.externalCacheDir ?: vsh.cacheDir, "CXLUPDAT.APK")
    }

    fun beginCheck(){
        vsh.threadPool.execute (::checkThreadFn)
    }

    var isChecking = false
        private set
    var hasUpdate = false
        private set
    var apkUrl = ""
        private set
    var newRelName = ""
        private set
    var updatedAt = ""
        private set
    var updateSize = ""
        private set
    var updateInfo = ""
        private set

    var isDownloading = false
        private set
    var downloadProgressCurrent = 0L
        private set
    var downloadProgressMax = 0L
        private set
    var downloadComplete = false
        private set
    var apkFile = File("", "CXLUPDAT.APK")
        private set

    var locker = Mutex()

    val downloadProgressF : Float get() = downloadProgressCurrent.toFloat() / downloadProgressMax.toFloat()


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
        isChecking = true
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
                        hasUpdate = apk.name.endsWith(apkSuffix)
                        if(hasUpdate){
                            apkUrl = apk.browser_download_url
                            downloadProgressMax = apk.size
                            updateSize = apk.size.asBytes()
                            break
                        }
                    }
                    if(hasUpdate){
                        vsh.postNotification(
                            R.drawable.ic_sync_loading,
                            "System Update Found!",
                            "You can check and download the update at Settings > System Update\nor you can open the GitHub Release page and download from there")
                        updatedAt = dat.created_at
                        newRelName = dat.name
                        updateInfo = dat.body
                    }
                }
            }
        }catch(e:Exception){
            e.printStackTrace()
        }finally {
            cn?.disconnect()
        }
        isChecking = false
    }

    override fun onDestroy() {

    }

    fun beginDownload() {
        //vsh.threadPool.execute (::downloadThreadFn)
        try {
            val act = Intent(Intent.ACTION_VIEW)
            act.data = Uri.parse(apkUrl)
            act.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            vsh.startActivity(act)
        }catch(e:Exception){
            vsh.postNotification(R.drawable.ic_error, "Failed to download update", "${e.message}")
        }
    }

    // Disabled unstable, for now call browser
/*
    private fun downloadThreadFn(){
        isDownloading = true
        val uri = URL(apkUrl)
        if(!apkFile.exists()){
            apkFile.createNewFile()
        }
        val apkStr = apkFile.outputStream()
        apkStr.channel.position(0L)

        var cn : HttpsURLConnection? = null
        try {
            downloadProgressCurrent = 0
            cn = uri.openConnection() as HttpsURLConnection
            cn.requestMethod = "GET"
            cn.setRequestProperty("Accept", "application/vnd.android.package-archive")
            cn.setRequestProperty("User-Agent", httpUserAgent)

            cn.doInput = true
            val ins = cn.inputStream
            val chrArr = ByteArray(1024)
            var rd = ins.read(chrArr, 0, chrArr.size)
            while(rd > -1) {
                apkStr.write(chrArr, 0, rd)
                apkStr.flush()
                synchronized(locker){
                    downloadProgressCurrent += rd
                }
                rd = ins.read(chrArr, 0, chrArr.size)
            }
            apkStr.flush()
            apkStr.close()

            vsh.openFileByDefaultApp(apkFile)
        }catch(e:Exception){
            e.printStackTrace()
        }finally{
            cn?.disconnect()
        }
        isDownloading = false
    }*/
}