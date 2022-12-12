package id.psw.vshlauncher.types

import java.io.File
import java.io.IOException

class INIFile(private var src:String, is_path:Boolean) {

    init {
        if(is_path){
            val f = File(src)
            src = f.readText(Charsets.UTF_8)
        }

        parse(src)
    }

    private lateinit var sections : Map<String, Map<String, String>>

    fun writeToFile(path:String){
        val f = File(path)
        if(!f.exists()) f.createNewFile()

        val o = f.outputStream().writer(Charsets.UTF_8)
        for(sect in sections){
            o.write("[${sect.key}]\n")
            for(keys in sect.value){
                o.write("${keys.key}=${keys.value}\n")
            }
            o.write("\n")
            o.flush()
        }
        o.flush()
        o.close()
    }

    private fun parse(src:String){
        val lines = src.lines()
        val sects = mutableMapOf<String, Map<String, String>>()
        var sname = ""
        var sect1 = mutableMapOf<String, String>()

        fun pushPrevious(){
            if(sname.isNotEmpty()){
                sects[sname] = sect1.toMap()
            }
            sect1 = mutableMapOf()
        }


        for (u_line in lines){
            val line = u_line.trim()
            if(line.startsWith('[') && line.endsWith(']')){
                pushPrevious()
                sname = line.substring(1, line.length - 2)
            }else if(line.length > 3 && line.contains('=', ignoreCase = true)){
                val spl = line.split('=', ignoreCase = true, limit = 2)
                if(spl.size >= 2){
                    val v = spl[1].replace("\\n","\n")
                    sect1[spl[0]] = v
                }
            }
        }
        pushPrevious()
        sections = sects
    }

    fun hasKey(section: String, key:String) : Boolean =
        if(sections.containsKey(section)) sections[section]?.containsKey(key) == true else false

    fun get(section: String, key:String, defVal:String) : String = sections[section]?.get(key) ?: defVal

    fun <T> get(section: String, key:String, defVal: T, cnv: (String) -> T) : T {
        return if(hasKey(section, key))
            cnv(get(section, key, ""))
        else defVal
    }
}