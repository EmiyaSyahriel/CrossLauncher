package id.psw.vshlauncher.submodules

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import id.psw.vshlauncher.Logger
import id.psw.vshlauncher.PrefEntry
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.VshBaseDirs
import id.psw.vshlauncher.sdkAtLeast
import id.psw.vshlauncher.types.FileQuery
import id.psw.vshlauncher.types.XMBItem
import java.io.File
import java.lang.Exception

class AudioSubmodule(private val ctx : Vsh) : IVshSubmodule {
    companion object {
        private const val TAG = "vsh_audio"
    }

    enum class Channel {
        Master,
        Sfx,
        Bgm,
        SystemBgm
    }

    private var _master = 1.0f
    private var _sfx = 1.0f
    private var _bgm = 1.0f
    private var _systemBgm = 1.0f

    var onVolumeChange : ((Channel, Float) -> Unit)? = null
    private val _pref get() = ctx.M.pref

    private fun sendChange(channel: Channel){
        onVolumeChange?.invoke(channel, when(channel) {
            Channel.Bgm -> _master * _bgm
            Channel.Sfx -> _master * _sfx
            Channel.SystemBgm -> _master * _systemBgm
            else -> _master
        })

        _pref.set(
            when(channel){
                Channel.Bgm -> PrefEntry.VOLUME_AUDIO_BGM
                Channel.Sfx -> PrefEntry.VOLUME_AUDIO_SFX
                Channel.SystemBgm -> PrefEntry.VOLUME_AUDIO_SYSBGM
                else -> PrefEntry.VOLUME_AUDIO_MASTER
            },
            when(channel){
                Channel.Bgm -> _bgm
                Channel.Sfx -> _sfx
                Channel.SystemBgm -> _systemBgm
                else -> _master
            }
        )
    }

    fun readPreferences(){
        master = _pref.get(PrefEntry.VOLUME_AUDIO_MASTER, 1.0f)
        sfx = _pref.get(PrefEntry.VOLUME_AUDIO_SFX, 1.0f)
        bgm = _pref.get(PrefEntry.VOLUME_AUDIO_BGM, 1.0f)
        systemBgm = _pref.get(PrefEntry.VOLUME_AUDIO_SYSBGM, 1.0f)
    }

    var master : Float
        get() = _master
        set(value) { _master = value; sendChange(Channel.Master) }

    var sfx : Float
        get() = _sfx
        set(value) { _sfx = value; sendChange(Channel.Sfx) }

    var bgm : Float
        get() = _bgm
        set(value) { _bgm = value; sendChange(Channel.Bgm) }
    var systemBgm : Float
        get() = _systemBgm
        set(value) { _systemBgm = value; sendChange(Channel.SystemBgm) }

    var preventPlayMedia = true
    val bgmPlayer = MediaPlayer()
    val systemBgmPlayer = MediaPlayer()
    lateinit var bgmPlayerActiveSrc : File
    var bgmPlayerDoNotAutoPlay = false
    lateinit var sfxPlayer : SoundPool
    val sfxIds = mutableMapOf<SfxType, Int>()

    override fun onCreate(){
        if(sdkAtLeast(21)){
            val attr = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_MEDIA)

            if(sdkAtLeast(29)){
                attr.setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_ALL)
            }

            sfxPlayer = SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(attr.build())
                .build()
        }else{
            sfxPlayer = SoundPool(6, AudioManager.STREAM_MUSIC, 0)
        }

        readPreferences()
        preparePlaceholderAudio()
        bgmPlayer.setOnPreparedListener {
            it.isLooping = true
            if(!bgmPlayerDoNotAutoPlay) it.start()
        }
        systemBgmPlayer.setOnPreparedListener {
            it.isLooping = false
            it.start()
        }
        loadSfxData()
    }

    override fun onDestroy() {
        sfxPlayer.release()
        bgmPlayer.release()
        systemBgmPlayer.release()
    }

    fun preparePlaceholderAudio(){
        ctx.assets.open("silent.aac").use { ins ->
            val bArray = ByteArray(Vsh.COPY_DATA_SIZE_BUFFER)
            val file = File.createTempFile("silent",".aac")
            file.deleteOnExit()
            file.outputStream().use { outs ->
                var readSize = ins.read(bArray, 0, Vsh.COPY_DATA_SIZE_BUFFER)
                while(readSize > 0){
                    outs.write(bArray, 0, readSize)
                    readSize = ins.read(bArray, 0, Vsh.COPY_DATA_SIZE_BUFFER)
                }
            }
            XMBItem.SILENT_AUDIO = file
            bgmPlayerActiveSrc = file
        }
    }

    fun setSystemAudioSource(newSrc: File){
        if(systemBgmPlayer.isPlaying) systemBgmPlayer.stop()
        systemBgmPlayer.reset()
        systemBgmPlayer.setDataSource(newSrc.absolutePath)
        systemBgmPlayer.prepareAsync()
    }

    fun setAudioSource(newSrc: File, doNotStart : Boolean = false){
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
                Logger.d(Vsh.TAG, "Changing BGM Player Source to ${newSrc.absolutePath}")
            }catch(e: Exception){
                Logger.e(Vsh.TAG, "BGM Player Failed : ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun removeAudioSource(){
        if(bgmPlayerActiveSrc != XMBItem.SILENT_AUDIO){
            if(bgmPlayer.isPlaying) bgmPlayer.stop()
            bgmPlayer.reset()
            bgmPlayerActiveSrc = XMBItem.SILENT_AUDIO
        }
    }

    fun loadSfxData(attach : Boolean = true){
        var that = this

        if(attach){
            sfxPlayer.setOnLoadCompleteListener { _, id, status ->
                if(status != 0){
                    val ks = arrayListOf<Pair<SfxType, Int>>()
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
            SfxType.Selection to "select",
            SfxType.Confirm to "confirm",
            SfxType.Cancel to "cancel",
        )

        for(it in types)
        {
            val files = FileQuery(VshBaseDirs.VSH_RESOURCES_DIR)
                .atPath("sfx")
                .withNames(it.second)
                .onlyIncludeExists(true)
                .withExtensions("ogg", "wav", "mp3")
                .execute(ctx)

            for(file in files){
                val found = file.isFile
                Logger.d(TAG, " ${it.first}::\"${file.absolutePath}\" found ? $found")
                if(found){
                    val i = sfxPlayer.load(file.absolutePath, 0)
                    sfxIds[it.first] = i
                    break
                }
            }

        }
    }

    fun playSfx(type:SfxType){
        if(sfxIds.containsKey(type)){
            val sid = sfxIds[type]
            if(sid != null){
                val v = sfx * master
                sfxPlayer.play(sid, v, v, 0, 0, 1.0f)
            }
        }
    }
}