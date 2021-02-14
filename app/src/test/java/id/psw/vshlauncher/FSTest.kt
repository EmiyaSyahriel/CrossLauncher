package id.psw.vshlauncher

import org.junit.Test
import java.io.File

class FSTest {
    companion object
    {
        private const val PATH_CREATE_TEST = "I:\\Music\\Nokia 5300 XpressMusic\\TMP00\\"
        private const val FILE_CREATE_TEST = "I:\\Music\\Nokia 5300 XpressMusic\\TMP00"
    }

    @Test
    fun fsTest_01(){
        combinedPathDirExistenceTest(PATH_CREATE_TEST)
        combinedPathDirExistenceTest(FILE_CREATE_TEST)
    }

    fun combinedPathDirExistenceTest(source:String){
        println("Start Combined Path Directory Test atr $source")

        val fd = File(source)
        val isCreated = fd.mkdirs()
        println("Is Created : $isCreated")
        val isExist = fd.exists()
        println("Is Exists : $isExist (Will be deleted on 5 seconds)")

        Thread.sleep(5000)
        val isDeleted = fd.delete()
        println("Is Deleted : $isDeleted")
    }
}