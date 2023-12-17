package id.psw.vshlauncher.services

import android.app.job.JobInfo
import android.app.job.JobInfo.TriggerContentUri
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import id.psw.vshlauncher.BuildConfig
import id.psw.vshlauncher.sdkAtLeast
import id.psw.vshlauncher.vsh

@RequiresApi(Build.VERSION_CODES.M)
class MediaListingJobService : JobService() {
    companion object {
        private const val ID_MEDIA_LISTING_JOB = 0x70BA1
        const val TAG = "MediaListingJobService"
        private var job : JobInfo? = null

        fun schedule(ctx: Context){
            val oJob = job
            val man = ctx.getSystemService(JobScheduler::class.java)
            if(oJob != null){
                man.schedule(oJob)
                return
            }

            val jib = JobInfo.Builder(ID_MEDIA_LISTING_JOB,
                ComponentName(BuildConfig.APPLICATION_ID, MediaListingJobService::class.java.name)
            )

            if(sdkAtLeast(Build.VERSION_CODES.N)){
                val flag = TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS
                jib.addTriggerContentUri(TriggerContentUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, flag))
                jib.addTriggerContentUri(TriggerContentUri(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, flag))
                jib.setTriggerContentMaxDelay(500)
            }
            val nJob = jib.build()
            job = nJob
            man.schedule(nJob)
        }
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        // Return if Parameter is null or API is not available yet
        if(params == null || !sdkAtLeast(Build.VERSION_CODES.N)) return true

        // Return if No Authority or no Uri
        val uris = params.triggeredContentUris
        val auths = params.triggeredContentAuthorities
        if(auths == null || uris == null) return true
        val handler = Handler(mainLooper)
        for(uri in uris){
            Log.d(TAG, "Media Added -> $uri")
        }

        jobFinished(params, true)
        schedule(this)

        return true
    }


    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

}