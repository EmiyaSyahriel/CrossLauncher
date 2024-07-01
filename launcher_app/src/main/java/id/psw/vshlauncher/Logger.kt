package id.psw.vshlauncher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Redirector for Android Log.xxx() call
 * Used to write every call to both logcat and log file (if DEBUG build)
 */
object Logger {
    private lateinit var log : OutputStreamWriter
    private lateinit var sdf : SimpleDateFormat

    fun init(ctx: Context){
        if(BuildConfig.DEBUG){
            sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val date = sdf.format(Calendar.getInstance().time)
            val file = File(ctx.getExternalFilesDir(null), "logs.txt")

            if(!file.exists()){ file.createNewFile() }

            // append only if the file is less than 4MB, otherwise truncate
            val append = file.length() < 4 * 1024 * 1024;

            log = FileOutputStream(file, append).writer(Charsets.UTF_8)

            log.write("\n\n========= SESSION! @ $date =========\n");

            val handler = Thread.UncaughtExceptionHandler { t, e ->
                exc("CRASH!", t, e)
            }
            Thread.setDefaultUncaughtExceptionHandler(handler)
        }
    }

    private val wLock = Mutex(false)

    fun exportLog(ctx:Context, uri: Uri)
    {
        try {
            implExportLog(ctx, uri)
        }catch (e:Exception)
        {
            ctx.vsh.postNotification(R.drawable.ic_error, "Failed to Export Log", e.localizedMessage ?: "No message")
            e.printStackTrace()
        }
    }

    private fun implExportLog(ctx:Context, uri: Uri)
    {
        val kstr = try {
            ctx.contentResolver.openOutputStream(uri)
        }
        catch (_:Exception)
        {
            w("Logger", "Failed to use content resolver, using normal File API")
            null
        }

        val wstrm = if(kstr == null)
        {
            val fl = File(uri.path)
            if(!fl.exists()) fl.createNewFile()
            fl.outputStream()
        } else kstr

        val strm = wstrm.writer(Charsets.UTF_8)

        ctx.contentResolver.openOutputStream(uri)?.use {
            val file = File(ctx.getExternalFilesDir(null), "logs.txt")
            if(!file.exists()) {
                Toast.makeText(ctx, "Log empty or not exists", Toast.LENGTH_SHORT).show()
                return
            }
            file.readLines().forEach {
                strm.write(it)
            }
            strm.flush()
        }
        strm.close()
    }

    private fun getLogFileNameFormat() : String
    {
        val fmt = SimpleDateFormat("yyyy'_'MM'_'ss'__'hh'_'mm'_'ss", Locale.getDefault())
            .format(Calendar.getInstance().time)
        return "CrossLauncher_Debug_$fmt.log"
    }

    fun createExportIntent() : Intent
    {

        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, getLogFileNameFormat())
        }
    }

    fun shareLogIntent(ctx: Context)
    {
        val title = getLogFileNameFormat()
        val uri = FileProvider.getUriForFile(
            ctx,
            "id.psw.vshlauncher.fileprovider",
            File(ctx.getExternalFilesDir(null),
                "logs.txt"), title)
        val sIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, uri)
            type= "text/plain"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(Intent.EXTRA_TITLE, title)
        }

        val li = Intent.createChooser(sIntent, ctx.getString(R.string.dbg_export_log_name))
        if(li != null)
        {
            try {
                li.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ctx.startActivity(li)
            }catch(e:Exception){
                ctx.vsh.postNotification(R.drawable.ic_error, "Share failed", e.localizedMessage ?: "No info")
                e.printStackTrace()
            }
        }
    }

    private fun write(sev:Char, tag:String, str:String, logFn : ((String, String) -> Unit)){
        if(BuildConfig.DEBUG){
            runBlocking {
                wLock.withLock {
                    val tid = Thread.currentThread().id
                    val date = sdf.format(Calendar.getInstance().time)
                    log.write("$date $tid $sev/$tag : $str\n")
                    log.flush()
                }
            }
        }
        logFn.invoke(tag, str)
    }

    private val stb = StringBuilder()
    private val dLock = Mutex(false)

    private fun write(sev:Char, tag:String, t: Thread, e:Throwable, logFn : ((String, String) -> Unit)){
        runBlocking {
            dLock.withLock {
                stb.appendLine("Exception Message : ${e.message}\nStack Trace : \n ${Log.getStackTraceString(e)}")
                write(sev, tag, stb.toString(), logFn)
            }
        }
    }

    fun d(tag : String, str: String) =   write('D', tag, str, Log::d)
    fun e(tag : String, str: String) =   write('E', tag, str, Log::e)
    fun w(tag : String, str: String) =   write('W', tag, str, Log::w)
    fun i(tag : String, str: String) =   write('I', tag, str, Log::i)
    fun v(tag : String, str: String) =   write('V', tag, str, Log::v)
    fun wtf(tag : String, str: String) = write('!', tag, str, Log::wtf)
    fun exc(tag:String, t : Thread, e:Throwable) = write('X', tag, t, e, Log::e)
}