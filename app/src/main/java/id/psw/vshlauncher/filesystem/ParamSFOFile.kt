package id.psw.vshlauncher.filesystem

import java.io.File

class ParamSFOFile {

    companion object{
        private const val MAGIC : Int = 0x00878380
    }

    constructor(file: File){
        val str = file.inputStream()

        str.reset()
        if(str.readInt() == MAGIC) {

        }
        str.close()
    }
}