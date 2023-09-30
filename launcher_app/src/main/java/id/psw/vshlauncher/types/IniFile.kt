package id.psw.vshlauncher.types

import java.io.File
import java.io.OutputStreamWriter

class IniFile() {

    private var _path = ""
    val path get()=_path

    private var sections = mutableMapOf<String, MutableMap<String, String>>()

    fun write(path:String?){
        val p = path ?: _path

        val f = File(p)
        if(!f.exists()) f.createNewFile()

        val o = f.outputStream().writer(Charsets.UTF_8)
        write(o)
        o.flush()
        o.close()
    }

    fun write(o : OutputStreamWriter){
        for(sect in sections){
            o.write("[${sect.key}]\n")
            for(keys in sect.value){
                o.write("${keys.key}=${keys.value}\n")
            }
            o.write("\n")
            o.flush()
        }
    }

    fun parseFile(path: String?){
        val p = path ?: _path

        val f = File(p)
        parse(f.readText(Charsets.UTF_8))

        if(path != null){
            _path = path
        }
    }


    fun parse(src:String){
        for(cSect in sections){
            cSect.value.clear()
        }
        sections.clear()

        val lines = src.lines()
        val sects = mutableMapOf<String, MutableMap<String, String>>()
        var sname = ""
        var sect1 = mutableMapOf<String, String>()

        fun pushPrevious(){
            if(sname.isNotEmpty()){
                sects[sname] = sect1
            }
            sect1 = mutableMapOf()
        }


        for (u_line in lines){
            val line = u_line.trim()
            if(line.startsWith('[') && line.endsWith(']')){
                pushPrevious()
                sname = line.substring(1, line.length - 1)
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

    fun remove(section:String, key:String) = sections[section]?.remove(key)
    fun count(section: String) : Int = sections[section]?.size ?: -1
    fun count() : Int = sections.size

    val sectionNames : Array<String> get() {
        val lst = Array<String>(sections.size) { "" }
        var i = 0
        for(s in sections){
            lst[i] = s.key
            i++
        }
        return lst
    }

    operator fun get(sect:String, key:String): String? {
        return sections[sect]?.get(key)
    }

    operator fun set(section: String, key:String, value: String){
        if(!sections.containsKey(section)){
            sections[section] = mutableMapOf()
        }

        sections[section]?.set(key, value)
    }

    fun hasKey(section: String, key:String) : Boolean =
        if(sections.containsKey(section)) sections[section]?.containsKey(key) == true else false

    fun <T> get(section: String, key:String, defVal: T, cnv: (String) -> T) : T {
        return if(hasKey(section, key))
            cnv(this[section, key] ?: "")
        else defVal
    }
}