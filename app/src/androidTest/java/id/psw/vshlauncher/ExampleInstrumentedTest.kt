package id.psw.vshlauncher

import android.content.pm.PackageManager
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("id.psw.vshlauncher", appContext.packageName)
    }

    @Test
    fun getExceptionCode(){
    }

    @Test
    fun getPaths(){
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        Log.d("PathTest","External Files Dir : ${context.getExternalFilesDir("config")}")
        Log.d("PathTest","Obb Dir : ${context.obbDir}")
        Log.d("PathTest","Cache Dir : ${context.cacheDir}")
        Log.d("PathTest","Files Dir : ${context.filesDir}")
        Log.d("PathTest","Data Dir : ${context.dataDir}")
    }
}
