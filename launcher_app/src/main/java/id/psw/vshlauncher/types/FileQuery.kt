package id.psw.vshlauncher.types

import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.select
import java.io.File
import kotlin.math.abs

/**
 * Used to find files in in storages
 */
class FileQuery {
    private val baseDir : String
    private val isBaseAbsolute : Boolean
    private val paths = ArrayList<String>()
    private val extensions = ArrayList<String>()
    private val names = ArrayList<String>()
    private var mkDir = false
    private var onlyExists = false

    /**
     * @param baseDir Base Directory for search
     * @param absolute Don't append this baseDir to storage paths
     * */
    constructor (baseDir:String, absolute: Boolean = false){
        this.baseDir = baseDir
        isBaseAbsolute = absolute
    }

    constructor(baseDir:File){
        this.baseDir = baseDir.absolutePath
        isBaseAbsolute = true
    }

    fun createParentDirectory(create:Boolean) : FileQuery{
        mkDir = create
        return this
    }

    fun onlyIncludeExists(exists:Boolean) : FileQuery {
        onlyExists = true
        return this
    }

    fun atPath(vararg dirs:String) : FileQuery{
        this.paths.addAll(dirs)
        return this
    }

    fun withNames(vararg fileName:String) : FileQuery {
        this.names.addAll(fileName)
        return this
    }

    fun withExtensions(vararg exts: String) : FileQuery {
        this.extensions.addAll(exts)
        return this
    }

    fun execute(vsh:VSH) : ArrayList<File>{
        val files = arrayListOf<File>()

        val bStorages = arrayListOf<File>()

        if(isBaseAbsolute){
            bStorages.add(File(baseDir))
        }else{
            val storages = vsh.getExternalFilesDirs(null)
            for (storage in storages){
                var bdir = File(storage, baseDir)
                for(path in paths){
                    bdir = File(bdir, path)
                }
                bStorages.add(bdir)
            }
        }

        for(bStorage in bStorages){
            if(mkDir){
                if(!bStorage.isDirectory){
                    bStorage.mkdirs()
                }
            }

            for(name in names){
                for(ext in extensions) {
                    val file = File(bStorage, "$name.$ext")
                    if(onlyExists.select(file.exists() || file.isFile, true)){
                        files.add(file)
                    }
                }
            }
        }

        return files
    }

}