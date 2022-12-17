package id.psw.vshlauncher.types

import java.util.zip.ZipFile

class XPKGFile(zip:ZipFile) {

    companion object {
        const val INI_PKG_METADATA = "PKG_METADATA"
        const val INI_CUSTOMIZATION = "CUSTOMIZATION"
    }

    val fileNames = arrayListOf<String>()
    val info = INIFile()

    init {
        val ie = zip.entries()
        while(ie.hasMoreElements()){
            val e = ie.nextElement()
            fileNames.add(e.name)

            if(e.name.equals("PARAM.INI", true)){
                val zis = zip.getInputStream(e)
                info.parse(zis.reader(Charsets.UTF_8).readText())
            }
        }
    }
}