package id.psw.vshlauncher.typography

import android.content.Context
import android.graphics.Typeface
import android.graphics.fonts.Font
import android.os.Build
import android.util.Log
import android.widget.Toast
import id.psw.vshlauncher.*
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
    lateinit var masterFont : Typeface
    /**
     * The Button symbol font `"/assets/vshbtn.ttf"` is partially compliant with how PS3 draw it's font,
     * though it's not colored like it originally was due to FontForge haven't supported it in TTF
     */
    lateinit var buttonFont : Typeface

    const val TAG = "fntmgr.self"
    private const val FONT_NAME = "VSH-CustomFont.ttf"

    fun init(ctx: VSH){
        buttonFont = Typeface.createFromAsset(ctx.assets, "vshbtn.ttf")
        val fontLocations = ctx.getAllPathsFor(VshBaseDirs.FLASH_DATA_DIR, "font")
        var isCustomFontFound = false
        fontLocations.forEach { dir ->
            if(!isCustomFontFound){
                dir.listFiles()?.forEach {
                    if(it.name.lowercase().contentEquals(FONT_NAME.lowercase()) && !isCustomFontFound){
                        try{
                            masterFont = Typeface.createFromFile(it)
                            isCustomFontFound = true
                        }catch(e:Exception){
                            ctx.postNotification(null, "Font Manager", ctx.getString(R.string.font_load_failed, e.message))
                        }
                    }
                }
            }
        }

        if(!isCustomFontFound){

            masterFont = Typeface.SANS_SERIF
        }
    }
}