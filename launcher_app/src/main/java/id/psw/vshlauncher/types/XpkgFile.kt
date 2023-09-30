package id.psw.vshlauncher.types

import java.util.zip.ZipFile

class XpkgFile(val zip:ZipFile) {

    companion object {
        const val INI_PKG_METADATA = "PKG_METADATA"
    }

    enum class ItemType {
        PLUGIN,
        SYSTEM,
        GAME,
        APPS
    }

    data class FolderInfo (
        val titleId : String = "",
        val root : String = ""
            )

    val fileNames = arrayListOf<String>()
    val info = IniFile()
    private var _loadProgress = 0.0f
    val loadProgress get() = _loadProgress

    val title get() = info[INI_PKG_METADATA, "TITLE"] ?: "[No Title]"
    val author get() = info[INI_PKG_METADATA, "AUTHOR"] ?: "[Anonymous]"
    val packager get() = info[INI_PKG_METADATA, "PACKAGER"] ?: "[Anonymous]"
    val description get() = info[INI_PKG_METADATA, "DESCRIPTION"] ?: "- no description -"
    val checkInstalls get() = info[INI_PKG_METADATA, "CHECK_INSTALL"] ?: ""
    val iconPath get() = info[INI_PKG_METADATA, "ICON"] ?: ""

    init {
        val ie = zip.entries()
        var i = 0.0f
        val c = zip.size()
        while(ie.hasMoreElements()){
            val e = ie.nextElement()
            fileNames.add(e.name)

            if(e.name.equals("PARAM.INI", true)){
                val zis = zip.getInputStream(e)
                info.parse(zis.reader(Charsets.UTF_8).readText())
                zis.close()
            }

            _loadProgress = i / c
            i += 1.0f
        }
    }
}
