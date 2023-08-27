package id.psw.vshlauncher

import org.junit.Test

class RegexTest {
    @Test
    fun regexTest(){
        val regex = "[^A-Za-z][^A-Za-z0-9]*"
        val src1 = "kaguya100.png"
        val dst1 = "kaguya100_png"
        val src2 = "7oyuriko.png"
        val dst2 = "_oyuriko_png"

        println("$src1 -> ${src1.replace(regex, "_", true)} == $dst1")
        println("$src2 -> ${src2.replace(regex, "_", true)} == $dst2")
    }
}