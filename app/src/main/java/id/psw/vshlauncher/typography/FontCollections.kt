package id.psw.vshlauncher.typography

import android.content.Context
import android.graphics.Typeface
import android.graphics.fonts.Font
import android.util.Log
import android.widget.Toast
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.getVshCustomRsrcDir
import java.lang.Exception

object FontCollections {
    /**
     * Fonts can be dumped from either a CFW-ed PS3, a Firmware Update Extractor or PS3 Emulator
     * PS3 Font Names :
     * - SCE-PS3 Mantissa (SCE-PS3-MT-R-LATIN / Serif / serif)
     * - SCE-PS3 NewRodin (SCE-PS3-NR-R-JPN / PSP Default / sans-serif)
     * - SCE-PS3 Rodin (SCE-PS3-RD-R-LATIN / Default / sans-serif)
     * - SCE-PS3 Seurat (SCE-PS3-SR-R-JPN / Pop / sans-serif)
     * - VAGRundschriftDLig (SCE-PS3-VG-R-LATIN / Rounded / sans-serif)
     */
    var masterFont : Typeface? = null
    lateinit var buttonFont : Typeface

    fun init(ctx: VSH){
        try {
            val rsrc = ctx.getVshCustomRsrcDir()
            if(rsrc != null){
                Log.d("fntmgr.self", "Loading custom font from ${rsrc.absolutePath}")
            }
            val customFontPath = ctx.getVshCustomRsrcDir()?.listFiles()
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