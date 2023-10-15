package id.psw.vshlauncher

import android.os.Build
import android.util.Log
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime

class AndroidVersionTest {
    private fun printAllFields(clz : Class<*>){
        val fields = clz.fields
        for(f in fields){
            var dat : String? = null
            when (f.type){
                Int::class.java ->{
                    dat = f.getInt(null).toString()
                }
                String::class.java -> {
                    val rtv = f.get(null)
                    if(rtv is String){
                        dat = rtv
                    }
                }
            }

            Log.i("BuildVerTest", "${clz.name}#${f.name} - [${f.type.packageName}] $dat")
        }
    }

    @Test
    fun printBuildVersion(){
        printAllFields(Build::class.java)
        printAllFields(Build.VERSION::class.java)
        printAllFields(Build.VERSION_CODES::class.java)
        printAllFields(Build.Partition::class.java)
    }

    @Test
    fun dateParseTest(){
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss Z")
        val now = Instant.ofEpochMilli(System.currentTimeMillis())
        now.minusSeconds(2 * 3600)
        val nowMs = now.toEpochMilli()
        arrayOf(
            "2023-10-03T07:33:12+07:00",
            "2023-12-03T07:12:01Z",
            "2023-07-02T16:20:39Z"
        ).forEach {
            val odt = Instant.parse(it)
            val inst = odt.toEpochMilli()
            if(nowMs < inst){
                Log.i("DateParse", "Hey, We got some update!")
            }
            Log.i("DateParse", "\"$it\" (Instant) -> \"$inst\"")
        }
    }
}