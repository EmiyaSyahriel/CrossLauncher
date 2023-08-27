import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Properties

abstract class ForgeButtonFont : DefaultTask() {
    companion object {
        private val fontForgeBins = arrayOf(
            "C:\\Program Files (x86)\\FontForgeBuilds\\bin\\fontforge.exe",
            "C:\\Program Files\\FontForgeBuilds\\bin\\fontforge.exe",
            "/usr/bin/fontforge"
        )
    }

    init {
        group = "resource generation"
        outputs.upToDateWhen {
            sourceFile.exists() && targetFile.exists() && targetFile.lastModified() > sourceFile.lastModified()
        }
    }

    @get:InputFile
    abstract var sourceFile : File

    @get:OutputFile
    abstract var targetFile: File

    private fun cmdLine(dir: String, vararg cmd : String) : Int {
        val mCmd = cmd.joinToString(separator = " ") {
            if(it.contains(" ") && !(it.startsWith("\"") || it.startsWith("\'"))){
                "\"$it\""
            }else{
                it
            }
        }

        println("Starting command line : $mCmd")

        val cProcess = ProcessBuilder(*cmd)
            .redirectErrorStream(true)

        if(Os.isFamily(Os.FAMILY_WINDOWS)){
            val ffRoot = File(dir).parent

            cProcess.directory(File(dir))

            var path = System.getenv("PATH")
            path = "\"$ffRoot\";\"$ffRoot/bin\";$path"
            cProcess.environment()["PATH"] = path
            println("Windows - setting PATH : $path")
        }

        val process = cProcess.start()

        try {
            process.inputStream.transferTo(System.err)
            process.errorStream.transferTo(System.err)
        }catch(_:Exception) {}

        return process.waitFor()
    }

    @TaskAction
    fun forge(){
        val prop = Properties()
        val cis = project.rootProject.file("local.properties").inputStream()
        prop.load(cis)
        val localFFPath = prop.getProperty("fontforge.path")
        val ffs = arrayOf(localFFPath, *fontForgeBins)

        var isRan = false
        for(bin in ffs){
            if(bin == null) continue
            val fl = File(bin)
            if(fl.exists()){
                val code = cmdLine(fl.parent, bin, "-lang=ff", "-c", "\"Open('${sourceFile.absolutePath.replace("\\","\\\\")}'); Generate('${targetFile.absolutePath.replace("\\","\\\\")}')\"")
                if(code != 0) {
                    cis.close()
                    throw Exception("FontForge exits with code $code")
                }else{
                    println("FontForge exits with code $code")
                }
                isRan = true
                break
            }
        }

        if(!isRan){
            cis.close()
            throw Exception("Cannot find FontForge in any standard directory, Please set `fontforge.path` property in local properties if your installation is not in standard path")
        }
        cis.close()
    }
}