package id.psw.vshlauncher

import android.media.AudioAttributes
import android.media.AudioManager
import android.util.Log
import id.psw.vshlauncher.types.XMBItem
import java.io.File
import java.lang.Exception
import android.media.SoundPool

private const val TAG = "vsh_audio"

fun VSH.preparePlaceholderAudio(){
    assets.open("silent.aac").use { ins ->
        val bArray = ByteArray(VSH.COPY_DATA_SIZE_BUFFER)
        val file = File.createTempFile("silent",".aac")
        file.deleteOnExit()
        file.outputStream().use { outs ->
            var readSize = ins.read(bArray, 0, VSH.COPY_DATA_SIZE_BUFFER)
            while(readSize > 0){
                outs.write(bArray, 0, readSize)
                readSize = ins.read(bArray, 0, VSH.COPY_DATA_SIZE_BUFFER)
            }
        }
        XMBItem.SILENT_AUDIO = file
        bgmPlayerActiveSrc = file
    }
}

fun VSH.setSystemAudioSource(newSrc: File){
    if(systemBgmPlayer.isPlaying) systemBgmPlayer.stop()
    systemBgmPlayer.reset()
    systemBgmPlayer.setDataSource(newSrc.absolutePath)
    systemBgmPlayer.prepareAsync()
}



fun VSH.setAudioSource(newSrc: File, doNotStart : Boolean = false){
    if(preventPlayMedia) return
    if(newSrc.absolutePath != bgmPlayerActiveSrc.absolutePath){
        try{
            bgmPlayerActiveSrc = newSrc
            if(bgmPlayer.isPlaying){
                bgmPlayer.stop()
            }
            bgmPlayer.reset()
            bgmPlayer.setDataSource(newSrc.absolutePath)
            bgmPlayer.prepareAsync()
            bgmPlayerDoNotAutoPlay = doNotStart
            Log.d(VSH.TAG, "Changing BGM Player Source to ${newSrc.absolutePath}")
        }catch(e: Exception){
            Log.e(VSH.TAG, "BGM Player Failed : ${e.message}")
            e.printStackTrace()
        }
    }
}

fun VSH.removeAudioSource(){
    if(bgmPlayerActiveSrc != XMBItem.SILENT_AUDIO){
        if(bgmPlayer.isPlaying) bgmPlayer.stop()
        bgmPlayer.reset()
        bgmPlayerActiveSrc = XMBItem.SILENT_AUDIO
    }
}
enum class SFXType {
    Selection,
    Confirm,
    Cancel
}

fun VSH.loadSfxData(){
    var that = this

    sfxPlayer.setOnLoadCompleteListener { _, id, status ->
        if(status != 0){
            val ks = arrayListOf<Pair<SFXType, Int>>()
            sfxIds.forEach {
                if(it.value == id) ks.add(it.key to it.value)
            }
            if(ks.size == 1){
                sfxIds.remove(ks[0].first)
            }
        }
    }
    arrayListOf<Pair<SFXType, String>>(
        SFXType.Selection to "select",
        SFXType.Confirm to "confirm",
        SFXType.Cancel to "cancel",
    ).forEach {
        arrayListOf<File>().apply {
            addAll(getAllPathsFor(VshBaseDirs.VSH_RESOURCES_DIR, "sfx", "${it.second}.ogg"))
            addAll(getAllPathsFor(VshBaseDirs.VSH_RESOURCES_DIR, "sfx", "${it.second}.wav"))
            addAll(getAllPathsFor(VshBaseDirs.VSH_RESOURCES_DIR, "sfx", "${it.second}.mp3"))
        }.find { it.exists() }.apply {
            if(this != null){
                val sfxId = sfxPlayer.load(absolutePath, 0)
                sfxIds[it.first] = sfxId
                Log.d(TAG, "SFX Player : $absolutePath -> $sfxId")
            }
        }
    }
}

fun VSH.playSfx(type:SFXType){
    if(sfxIds.containsKey(type)){
        val sid = sfxIds[type]
        if(sid != null){
            sfxPlayer.play(sid, 1.0f, 1.0f, 0, 0, 1.0f)
        }
    }
}