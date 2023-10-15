package id.psw.vshlauncher.types

import android.os.Build
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.select
import java.io.File

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
    private var onDirectoryCreation : ((File, Boolean) -> Unit)? = null

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

    fun createParentDirectory(create:Boolean, onCreate : ((File, Boolean) -> Unit)? = null) : FileQuery{
        mkDir = create
        onDirectoryCreation = onCreate
        return this
    }

    fun onlyIncludeExists(exists:Boolean) : FileQuery {
        onlyExists = exists
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

    fun withExtensionArray(extensionList: Array<String>) : FileQuery {
        this.extensions.addAll(extensionList)
        return this
    }

    fun withExtensions(vararg extensionList: String) : FileQuery {
        this.extensions.addAll(extensionList)
        return this
    }

    fun execute(vsh:Vsh) : ArrayList<File>{
        val files = arrayListOf<File>()

        val bStorages = arrayListOf<File>()

        if(isBaseAbsolute){
            bStorages.add(File(baseDir))
        }else{
            val storages = arrayListOf<File>().apply {

                // Add Android/data - Other app needs SAF to modify this app starting from Android 10
                addAll(vsh.getExternalFilesDirs(null))

                // Add Android/media - File managers are allowed to access without SAF
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    addAll(vsh.externalMediaDirs)
                }
            }
            for (storage in storages){
                var baseFile = File(storage, baseDir)
                for(path in paths){
                    baseFile = File(baseFile, path)
                }
                bStorages.add(baseFile)
            }
        }

        for(bStorage in bStorages){
            if(mkDir){
                if(!bStorage.isDirectory){
                    val success = bStorage.mkdirs()
                    onDirectoryCreation?.invoke(bStorage, success)
                }
            }

            if(names.isNotEmpty()){
                for(name in names){
                    if(extensions.isEmpty()){
                        val file = File(bStorage, name)
                        if(onlyExists.select(file.exists() || file.isFile, true)){
                            files.add(file)
                        }
                    }else{
                        for(ext in extensions) {
                            val file = File(bStorage, "$name.$ext")
                            if(onlyExists.select(file.exists() || file.isFile, true)){
                                files.add(file)
                            }
                        }
                    }
                }
            }else{
                if(onlyExists.select(bStorage.exists() || bStorage.isDirectory, true)){
                    files.add(bStorage)
                }
            }
        }

        return files
    }

}