package id.psw.vshlauncher

import id.psw.vshlauncher.views.asBytes
import org.junit.Test

import org.junit.Assert.*
import java.io.File

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testSize(){
        println(File("I:\\Builds\\PacManUnity.apk").length().asBytes(false))
    }

    @Test
    fun testApl(){
        fun Float.pingpong(a:Float, b:Float) : Float {
            val l = kotlin.math.abs(b - a)
            return (this % (l * 2)) / l
        }

        for(i in 0 .. 100){
            println(i.toFloat().pingpong(10.0f, 1.0f))
        }
    }
}
