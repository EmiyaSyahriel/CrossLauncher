package id.psw.vshlauncher

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.charset.Charset

@RunWith(AndroidJUnit4::class)
class CharsetTest {
    @Test
    fun getCharset(){
        Log.d("CSTest8", "Starting available charset")
        for(cs in Charset.availableCharsets()){
            Log.d("CSTest8", "KEY : ${cs.key} - ${cs.value.displayName()}")
        }
    }
}