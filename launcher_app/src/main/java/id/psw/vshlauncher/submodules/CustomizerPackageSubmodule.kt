package id.psw.vshlauncher.submodules

import id.psw.vshlauncher.R
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.postNotification
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.reflect.Parameter
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class CustomizerPackageSubmodule {
    data class Parameters(
        var packageName:String,
        var name:String,
        var description:String,
        var artAuthor:String,
        var packageAuthor:String,
        var isGame:Boolean
    )

    companion object {
        const val PACKAGE_PARAMETER_INFO = "PARAM.INI"
        val PACKAGE_STATIC_ICON_FILENAMES = arrayOf("ICON0.PNG","ICON0.JPG")
        val PACKAGE_ANIMATED_ICON_FILENAMES = arrayOf("ICON1.APNG","ICON1.WEBP","ICON1.GIF")
        val PACKAGE_BACKDROP_FILENAMES = arrayOf("PIC1.PNG","PIC1.JPG")
        val PACKAGE_OVERLAY_BACKDROP_FILENAMES = arrayOf("PIC0.PNG","PIC0.JPG")
        val PACKAGE_PORT_BACKDROP_FILENAMES = arrayOf("PIC1_P.PNG","PIC1_P.JPG")
        val PACKAGE_PORT_OVERLAY_BACKDROP_FILENAMES = arrayOf("PIC0_P.PNG","PIC0_P.JPG")
        val PACKAGE_BACK_SOUND_FILENAMES = arrayOf("SND0.AAC","SND0.MP3")
    }

    fun readPackageInfo(vsh: VSH, file: File, param: Parameter) : Boolean{
        var retval = false
        try {
            ZipInputStream(file.inputStream()).use { zip ->
                var entry = zip.nextEntry
                while (entry is ZipEntry) {
                    if (entry.name.uppercase() == PACKAGE_PARAMETER_INFO && !entry.isDirectory) {
                        val all = zip.bufferedReader(Charsets.UTF_8).readText()
                        retval = true
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }catch(e:IOException){
            vsh.postNotification(null, vsh.getString(R.string.error_common_header), e.message ?: vsh.getString(R.string.unknown))
        }
        return retval
    }
}