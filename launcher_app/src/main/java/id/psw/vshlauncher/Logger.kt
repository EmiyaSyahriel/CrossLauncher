package id.psw.vshlauncher

import android.content.Context
import android.os.Build.VERSION_CODES.P
import android.util.Log
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

    private fun write(sev:Char, lv: Int, tag:String, str:String, alog : ((String, String) -> Unit)?){
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
        alog?.invoke(tag, str)
    }

    private val stb = StringBuilder()
    private val dLock = Mutex(false)

    private fun write(sev:Char, tag:String, t: Thread, e:Throwable, alog : ((String, String) -> Unit)?){
        runBlocking {
            dLock.withLock {
                stb.appendLine("Exception Message : ${e.message}\nStack Trace : \n ${Log.getStackTraceString(e)}")
                write(sev, Log.ERROR, tag, stb.toString(), alog)
            }
        }
    }

    fun d(tag : String, str: String) =   write('D', Log.DEBUG, tag, str) { t, s -> Log.d(t,s) }
    fun e(tag : String, str: String) =   write('E', Log.ERROR, tag, str) { t, s -> Log.e(t,s) }
    fun w(tag : String, str: String) =   write('W', Log.WARN, tag, str) { t, s -> Log.w(t,s) }
    fun i(tag : String, str: String) =   write('I', Log.INFO, tag, str) { t, s -> Log.i(t,s) }
    fun v(tag : String, str: String) =   write('V', Log.VERBOSE, tag, str) { t, s -> Log.v(t,s) }
    fun wtf(tag : String, str: String) = write('!', Log.ASSERT, tag, str) { t, s -> Log.wtf(t,s) }
    fun exc(tag:String, t : Thread, e:Throwable) = write('X', tag, t, e) { tg, s -> Log.e(tg,s) }
}