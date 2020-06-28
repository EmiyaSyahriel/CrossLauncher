package id.psw.vshlauncher

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
        System.out.println(File("I:\\Builds\\PacManUnity.apk").length().toSize())
    }

    @Test
    fun marqueeTest(){
        System.out.println("soi".marquee(3,3,5))
    }
}
