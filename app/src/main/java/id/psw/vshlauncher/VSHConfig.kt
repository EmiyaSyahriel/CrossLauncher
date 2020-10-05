package id.psw.vshlauncher

import android.content.Context
import java.io.File


class VSHConfig(val context: Context) {
    companion object{
        var current : VSHConfig? = null
        fun getCurrent(context: Context) : VSHConfig {
            return current ?: VSHConfig(context)
        }
    }

    // Allocate 64KB for binary settings
    private var byteData = ByteArray(65536)
    private var autoSave = false
    private var configFile = File(context.getExternalFilesDir(""), "xRegistry.sys")

    init {
        fillZero()
        safeLoad()

        autoSave = getBool(Offsets.SAVINGS, false)
    }

    /**
     * Try to find the config file
     * if it exist, load. else save new file
     **/
    fun safeLoad(){
        if(configFile.exists()) load() else save()
    }

    private fun load(){
        val ins = configFile.inputStream()
        ins.read(byteData)
        ins.close()
    }

    private fun save(){
        val outs = configFile.outputStream()
        outs.write(byteData)
        outs.close()
    }

    private fun fillZero(){
        for(i in 0 until byteData.size){
            byteData[0] = 0
        }
    }

    fun getInt(offset:Int) : Int{
        var retval = 0
        if(offset + 4 < byteData.size){
            retval =
                (byteData[offset].toInt() shl 24) or
                (byteData[offset+1].toInt() shl 16) or
                (byteData[offset+2].toInt() shl 8) or
                (byteData[offset+3].toInt())
        }
        return retval
    }

    fun getBool(packOffset: Int, bitIndex:Int, default:Boolean = false) : Boolean{
        var retval = default
        if(packOffset >= 0 && packOffset < byteData.size && bitIndex < 8){
            retval = byteData[packOffset].toInt() shr bitIndex and 1 == 1
        }
        return retval
    }

    /**
     * Get Boolean from Config
     *
     * Offset Pack Formula = offset | ((bitIndex & 1) << 4)
     * @param offsetPack Packed offset and bit index
     * @param default default value
     **/
    fun getBool(offsetPack : Long, default:Boolean = false) : Boolean{
        var retval = default
        val packOffset = (offsetPack and 4).toInt()
        val bitIndex = ((offsetPack shr 4) and 1).toInt()
        if(packOffset >= 0 && packOffset < byteData.size && bitIndex < 8){
            retval = byteData[packOffset].toInt() shr bitIndex and 1 == 1
        }
        return retval
    }

    fun getBool(offset : BoolPackOffset, default:Boolean = false) : Boolean{
        var retval = default
        if(offset.byteOffset >= 0 && offset.byteOffset < byteData.size && offset.byteOffset < 8){
            retval = byteData[offset.byteOffset].toInt() shr offset.bitindex and 1 == 1
        }
        return retval
    }

    /**
     * Contains configuration byte offset, It's hardcoded anyway
     **/
    object Offsets {
        /**Bool Pack : Saving**/
        val SAVINGS = BoolPackOffset(0,0)
    }
}