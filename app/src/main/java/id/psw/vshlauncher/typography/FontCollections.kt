package id.psw.vshlauncher.typography

import android.content.Context
import android.graphics.Typeface
import android.graphics.fonts.Font
import android.util.Log
import android.widget.Toast
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.VshDirs
import id.psw.vshlauncher.formatDirPathAndCreate
import id.psw.vshlauncher.getFilesPath
import java.lang.Exception

object FontCollections {
    /**
     * This app will try load font at path `"Internal Storage/Android/data/id.psw.vshlauncher/vsh/resource/font.ttf"`
     *
     * Fonts can be dumped from either a CFW/HFW-ed PS3, a Firmware Update (PUP) Extractor or a PS3 Emulator.
     * Located at `"dev_flash/data/font/"`
     *
     * PS3 Font Names :
     * | Font Name (Metadata) | Font File Name     | PS3 Name    | Style      |
     * |----------------------|--------------------|-------------|------------|
     * | SCE-PS3 Mantissa     | SCE-PS3-MT-R-LATIN | Serif       | Serif      |
     * | SCE-PS3 NewRodin     | SCE-PS3-NR-R-JPN   | PSP Default | Sans-Serif |
     * | SCE-PS3 Rodin        | SCE-PS3-RD-R-LATIN | Default     | Sans-Serif |
     * | SCE-PS3 Seurat       | SCE-PS3-SR-R-JPN   | Pop         | Sans-Serif |
     * | VAGRundschriftDLig   | SCE-PS3-VR-R-LATIN | Rounded     | Sans-Serif |
     *
     * IDK about licenses, but it should be covered at [PS3 System Software License Agreement v1.4](https://doc.dl.playstation.net/doc/ps3-eula/ps3_eula_en.html) Point 2
     *
     * But as long as you dump it from your own hardware, it should be alright. since it's forbidden to redistribute the original file from the PS3 itself
     */
    var masterFont : Typeface? = null
    /**
     * The Button symbol font `"/assets/vshbtn.ttf"` is partially compliant with how PS3 draw it's font,
     * though it's not colored like it originally was due to FontForge haven't supported it in TTF
     */
    lateinit var buttonFont : Typeface

    fun init(ctx: VSH){
        try {
            val customFontPath = ctx.formatDirPathAndCreate(VshDirs.SYSTEM_DIR, ctx.getFilesPath()).listFiles()
            if(customFontPath != null && customFontPath.isNotEmpty()){
                customFontPath.forEach {
                    Log.d("fntmgr.self", "Found file : ${it.absolutePath}")
                    if(it.name.equals("font.ttf", true)){
                        masterFont = Typeface.createFromFile(it)
                        Log.d("fntmgr.self", "Loaded custom font at ${it.absolutePath}")
                    }
                }
            }else{
                Log.d("fntmgr.self", "Cannot find custom font")
            }
        }catch(e:Exception){
            Toast.makeText(ctx,
                "Cannot load custom font : ${e.message}",
                Toast.LENGTH_SHORT).show()
        }
        buttonFont = Typeface.createFromAsset(ctx.assets, "vshbtn.ttf")
    }
}