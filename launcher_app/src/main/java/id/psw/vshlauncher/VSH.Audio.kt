package id.psw.vshlauncher

import id.psw.vshlauncher.types.XMBItem
import java.io.File
import java.io.FileDescriptor
import java.lang.Exception

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
            Logger.d(VSH.TAG, "Changing BGM Player Source to ${newSrc.absolutePath}")
        }catch(e: Exception){
            Logger.e(VSH.TAG, "BGM Player Failed : ${e.message}")
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

fun VSH.loadSfxData(attach : Boolean = true){
    var that = this

    if(attach){
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
    }

    for(i in sfxIds){
        sfxPlayer.unload(i.value)
    }
    sfxIds.clear()

    val types = arrayListOf(
        SFXType.Selection to "select",
        SFXType.Confirm to "confirm",
        SFXType.Cancel to "cancel",
    )

    for(it in types)
    {

        var found = false
        val files = arrayListOf<File>().apply {
            addAll(getAllPathsFor(VshBaseDirs.VSH_RESOURCES_DIR, "sfx", "${it.second}.ogg"))
            addAll(getAllPathsFor(VshBaseDirs.VSH_RESOURCES_DIR, "sfx", "${it.second}.wav"))
            addAll(getAllPathsFor(VshBaseDirs.VSH_RESOURCES_DIR, "sfx", "${it.second}.mp3"))
        }

        for(file in files){
            found = file.isFile
            Logger.d(TAG, " ${it.first}::\"${file.absolutePath}\" found ? $found")
            if(found){
                val i = sfxPlayer.load(file.absolutePath, 0)
                sfxIds[it.first] = i
                break
            }
        }

    }
}

fun VSH.playSfx(type:SFXType){
    if(sfxIds.containsKey(type)){
        val sid = sfxIds[type]
        if(sid != null){
            val v = volume.sfx * volume.master
            sfxPlayer.play(sid, v, v, 0, 0, 1.0f)
        }
    }
}