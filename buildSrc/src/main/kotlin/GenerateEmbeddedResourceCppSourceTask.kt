import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
This build script is licensed under CC0 1.0 (Non-copyrighted Public Domain).
Therefore, you can copy, modify and use this build script to fulfill your need
without giving any credit to me (as "EmiyaSyahriel"). I have no responsibility
on any damage caused by this script in any way.

For additional info, Please refer to "https://creativecommons.org/publicdomain/zero/1.0/"
 */

/**
 * Usage :
 * ```kotlin
 *  val task = tasks.create<EmbedNativeResTask>() {
 *      inputDir.set(file("cpp/gltest/res/shaders"))
 *      inputExtensions = arrayOf("frag","vert")
 *      outputSourceFile.set(file("cpp/gltest/source/shaders.cpp"))
 *      outputSourceFile.set(file("cpp/gltest/source/shaders.hpp"))
 *      cppNamespace = "resources"
 *      cppDelimiter = "CHIHAYA"
 *  }
 *  ```
 *  for example having file named "cutboard.vert" will generate
 *  #### shaders.cpp
 *  ```cxx
 *  //// AUTO-GENERATED ////
 *  namespace resources {
 *      const char* const cutboard_vert = R"CHIHAYA(#version 240 es
 *  attribute vec3 v_vpos;
 *  attribute vec2 v_texcoord;
 *  uniform mat4 v_mvp;
 *  varying vec2 f_texcoord;
 *
 *  void main(){
 *      gl_Position = v_vpos * v_mvp;
 *      f_texcoord = v_texcoord;
 *  }
 *  )CHIHAYA";
 *  ...
 *  }
 *  ```
 */
abstract class GenerateEmbeddedResourceCppSourceTask : DefaultTask() {

    companion object {
        const val AUTO_GEN_TEMPLATE = """/**
 * File auto-generated using Gradle Task at `buildSrc/src/main/kotlin/EmbedNativeResTask.kt`
 */"""

        const val VAR_NAME_REGEX = "[^A-Za-z0-9]"
        const val VAR_NAME_PREFIX_REGEX = "[^A-Za-z]"
    }

    init {
        this.group = "code generation"
    }

    @get:InputDirectory
    var inputDir : File? = null
    @get:Input
    var inputExtensions : Array<String> = arrayOf()

    @get:OutputFile
    var outputHeaderFile : File? = null
    @get:OutputFile
    var outputSourceFile : File? = null

    @get:Input
    var cppNamespace : String = ""
    @get:Input
    var cppIncludes : Array<String> = arrayOf()
    @get:Input
    var cppDelimiter : String = "R"

    private fun getAllFiles() : ArrayList<File> {
        val inputDir = this.inputDir ?: throw Exception("Please set `inputDir` to source resource file directory")
        if(inputExtensions.isEmpty()) throw Exception("`inputExtensions` is empty, please set input resources file extension here")

        val files = inputDir.listFiles { _, name ->
            inputExtensions.any { name.endsWith(".$it") }
        }

        val retval = ArrayList<File>()
        if(files != null) retval.addAll(files)
        return retval
    }

    @TaskAction
    fun generateSource(){

        val outputSourceFile = this.outputSourceFile ?: throw Exception("Please set `outputSourceFile` - Where the generated C++ source file is located")
        val outputHeaderFile = this.outputHeaderFile ?: throw Exception("Please set `outputHeaderFile` - Where the generated C++ header file is located")

        val rg1 = Regex(VAR_NAME_REGEX)
        val rg0 = Regex(VAR_NAME_PREFIX_REGEX)

        val srcFiles = getAllFiles()
        println("\tWork dir : ${System.getProperty("user.dir")}")
        println("\tTarget source path : $outputSourceFile / $outputHeaderFile")
        println("\tEmbedding ${srcFiles.size} files into one file")

        val cpp = outputSourceFile.outputStream().bufferedWriter(Charsets.UTF_8)
        val hpp = outputHeaderFile.outputStream().bufferedWriter(Charsets.UTF_8)

        val includeGuard = "RES_${outputHeaderFile.nameWithoutExtension.uppercase()}_HPP"

        val useNamespace = cppNamespace.isNotBlank()
        val useMultilineLimiter = cppDelimiter.isNotBlank()
        var lineLimitPre = ""
        var lineLimitSuf = ""

        if(useNamespace){
            println("\tUsing Namespace : $cppNamespace")
        }

        hpp.appendLine("#pragma once")
        hpp.appendLine("#ifndef $includeGuard")
        hpp.appendLine("#define $includeGuard")

        for(inc in cppIncludes){
            if(inc.startsWith("<")){
                cpp.appendLine("#include $inc")
            }else{
                cpp.appendLine("#include \"$inc\"")
            }
        }

        hpp.appendLine(AUTO_GEN_TEMPLATE)
        cpp.appendLine(AUTO_GEN_TEMPLATE)
        hpp.append("\n")
        cpp.append("\n")

        if(useNamespace){
            cpp.appendLine("namespace $cppNamespace {")
            hpp.appendLine("namespace $cppNamespace {")
        }

        if(useMultilineLimiter){
            lineLimitPre = cppDelimiter
            lineLimitSuf = cppDelimiter
        }

        fun makeVarName(path:File) : String {
            val name = path.name
            val ch1 = name.substring(0, 1).replace(rg0, "_")
            val ch2= name.substring(1).replace(rg1, "_")
            return "$ch1$ch2"
        }

        for(file in srcFiles){
            val varName = makeVarName(file)
            val content = file.readText(Charsets.UTF_8)
            println("\t\t $cppNamespace::$varName -> ${file.absolutePath}")
            if(useNamespace){
                hpp.append(("\t"))
                cpp.append(("\t"))
            }

            hpp.appendLine("extern const char* const $varName;")
            cpp.appendLine("const char* const $varName = R\"$lineLimitPre($content)$lineLimitSuf\";")
            hpp.flush()
            cpp.flush()
        }

        if(useNamespace){
            cpp.appendLine("}")
            hpp.appendLine("}")
        }

        hpp.appendLine("#endif // $includeGuard")

        hpp.flush()
        cpp.flush()
        hpp.close()
        cpp.close()
    }
}