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
        const val GIT_ACTION_URL = "https://api.github.com/repos/EmiyaSyahriel/CrossLauncher/releases?per_page=1&page=1"
        const val APK_DEBUG_NAME = "-debug.apk";
        const val APK_RELEASE_NAME = "-release.apk";
        const val APK_DEVELOPMENT_NAME = "-development.apk";

        // https://github.com/EmiyaSyahriel/CrossLauncher/suites/${suiteId}/artifacts/${artifactId}
        private fun generateActionDownloadUri(suiteId: Long, artifactId: Long) : String {
            return "https://github.com/EmiyaSyahriel/CrossLauncher/suites/${suiteId}/artifacts/${artifactId}";
        }
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

    data class GitHubActionArtifactInfo(
        val id : Long,
        val name : String,
        val size_in_bytes : Long
    )

    @Suppress("ArrayInDataClass")
    data class GitHubActionArtifactList(
        val total_count : Int,
        val artifacts : Array<GitHubActionArtifactInfo>
    )

    @Suppress("ArrayInDataClass")
    data class GithubReleaseInfo(
        val url : String,
        val name : String,
        val created_at : String,
        val body : String,
        val assets : Array<GithubReleaseAssetInfo>
    )

    data class GithubActionRunInfo(
        val id : Int,
        val url : String,
        val name : String,
        val workflow_id: Long,
        val check_suite_id : Long,
        val created_at : String,
        val artifacts_url : String
    )

    private class GitHubReleaseInfoArray : ArrayList<GithubReleaseInfo>() {}
    private data class GitHubActionRunInfoArray (
        val total_count: Int,
        val workflow_runs : ArrayList<GithubActionRunInfo>
        )

    private fun updateFromReleasePage(ins:String, apkDate :Instant){
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
                        vsh.getString(R.string.updater_found_update_title),
                        vsh.getString(R.string.updater_found_update_desc))
                    updatedAt = dat.created_at
                    newRelName = dat.name
                    updateInfo = dat.body
                }
            }
        }
    }

    private fun listActionWorkflowRuns(ins:String, apkDate: Instant){
        val dats = gson.fromJson(ins, GitHubActionRunInfoArray::class.java)
        for(workflows in dats.workflow_runs){
            val relDate = Instant.parse(workflows.created_at)

            if(relDate > apkDate){ // Release is newer than this APK + 30 min
                val uri = URL(workflows.artifacts_url)
                val cn = uri.openConnection() as HttpsURLConnection
                cn.requestMethod = "GET"
                cn.setRequestProperty("Accept", "application/vnd.github+json")
                cn.setRequestProperty("User-Agent", httpUserAgent)
                cn.doInput = true
                val artfJson = cn.inputStream.bufferedReader().readText()
                val artfs = gson.fromJson(artfJson, GitHubActionArtifactList::class.java)
                for(artf in artfs.artifacts){

                }
            }
        }
    }

    private fun checkThreadFn(){
        isChecking = true
        val uri =  if(BuildConfig.IS_DEVELOPMENT) URL(GIT_ACTION_URL) else URL(GIT_RELEASE_URL)
        var cn : HttpsURLConnection? = null
        val apkDate = Instant.parse(BuildConfig.BUILD_DATE).plusSeconds(30 * 60) // 30 Minutes after building
        try {
            cn = uri.openConnection() as HttpsURLConnection
            cn.requestMethod = "GET"
            cn.setRequestProperty("Accept", "application/vnd.github+json")
            cn.setRequestProperty("User-Agent", httpUserAgent)

            cn.doInput = true
            val ins = cn.inputStream.bufferedReader().readText()
            if(BuildConfig.IS_DEVELOPMENT){ // Download from Action instead
                listActionWorkflowRuns(ins, apkDate)
            }else{
                updateFromReleasePage(ins, apkDate)
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
            vsh.postNotification(R.drawable.ic_error,
                vsh.getString(R.string.updater_download_failed_title),
                e.message ?: vsh.getString(R.string.updater_download_failed_unknown_error))
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