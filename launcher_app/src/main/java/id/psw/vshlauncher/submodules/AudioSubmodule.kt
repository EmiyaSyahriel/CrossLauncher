package id.psw.vshlauncher.submodules

import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import id.psw.vshlauncher.Logger
import id.psw.vshlauncher.PrefEntry
import id.psw.vshlauncher.R
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.VshBaseDirs
import id.psw.vshlauncher.lerpFactor
import id.psw.vshlauncher.postNotification
import id.psw.vshlauncher.sdkAtLeast
import id.psw.vshlauncher.types.FileQuery
import id.psw.vshlauncher.types.XmbItem
import id.psw.vshlauncher.xmb
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import kotlin.Exception

class AudioSubmodule(private val ctx : Vsh) : IVshSubmodule {
    companion object {
        private const val TAG = "vsh_audio"
        const val PICKER_REQ_ID = 0x4FF + 0xCED
        const val MENU_BGM_FADE_TIME = 1000.0f
    }

    enum class Channel {
        Master,
        Sfx,
        Bgm,
        SystemBgm,
        MenuBgm
    }

    private var _master = 1.0f
    private var _sfx = 1.0f
    private var _bgm = 1.0f
    private var _systemBgm = 1.0f
    private var _menuBgm = 0.2f

    var onVolumeChange : ((Channel, Float) -> Unit)? = null
    private val _pref get() = ctx.M.pref

    private fun sendChange(channel: Channel){
        onVolumeChange?.invoke(channel, when(channel) {
            Channel.Bgm -> _master * _bgm
            Channel.Sfx -> _master * _sfx
            Channel.SystemBgm -> _master * _systemBgm
            Channel.MenuBgm -> _master * _menuBgm
            else -> _master
        })

        if(channel == Channel.MenuBgm){
            menuBgmPlayer.setVolume(_menuBgm * _master, _menuBgm * _master)
        }

        _pref.set(
            when(channel){
                Channel.Bgm -> PrefEntry.VOLUME_AUDIO_BGM
                Channel.Sfx -> PrefEntry.VOLUME_AUDIO_SFX
                Channel.SystemBgm -> PrefEntry.VOLUME_AUDIO_SYSBGM
                Channel.MenuBgm -> PrefEntry.VOLUME_AUDIO_MENUBGM
                else -> PrefEntry.VOLUME_AUDIO_MASTER
            },
            when(channel){
                Channel.Bgm -> _bgm
                Channel.Sfx -> _sfx
                Channel.SystemBgm -> _systemBgm
                Channel.MenuBgm -> _menuBgm
                else -> _master
            }
        )
    }

    fun readPreferences(){
        master = _pref.get(PrefEntry.VOLUME_AUDIO_MASTER, 1.0f)
        sfx = _pref.get(PrefEntry.VOLUME_AUDIO_SFX, 1.0f)
        bgm = _pref.get(PrefEntry.VOLUME_AUDIO_BGM, 1.0f)
        systemBgm = _pref.get(PrefEntry.VOLUME_AUDIO_SYSBGM, 1.0f)
        menuBgm = _pref.get(PrefEntry.VOLUME_AUDIO_SYSBGM, 0.2f)
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
    var menuBgm : Float
        get() = _menuBgm
        set(value) { _menuBgm = value; sendChange(Channel.MenuBgm) }

    var preventPlayMedia = true
    val bgmPlayer = MediaPlayer()
    val systemBgmPlayer = MediaPlayer()
    var menuBgmPlayer = MediaPlayer()
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
            @Suppress("DEPRECATION") // For old Android version
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
        destroyMenuBgmPlayer()
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
            XmbItem.SILENT_AUDIO = file
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

                ctx.lifeScope.launch {
                    turnDownVolumeAndPauseMenuBgm()
                }

            }catch(e: Exception){
                Logger.e(Vsh.TAG, "BGM Player Failed : ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private suspend fun turnDownVolumeAndPauseMenuBgm(){
        val begin = System.currentTimeMillis()
        var now = begin
        while(now - begin < MENU_BGM_FADE_TIME){
            val t = (now - begin) / MENU_BGM_FADE_TIME
            val v = t.lerpFactor(menuBgm, 0.0f)
            menuBgmPlayer.setVolume(v,v)
            now = System.currentTimeMillis()
            delay(16L)
        }
        menuBgmPlayer.setVolume(0.0f, 0.0f)
        pauseMenuBgm()
    }

    private suspend fun resumeAndTurnVolumeUpMenuBgm(){
        val begin = System.currentTimeMillis()
        var now = begin
        resumeMenuBgm()
        while(now - begin < MENU_BGM_FADE_TIME){
            val t = (now - begin) / MENU_BGM_FADE_TIME
            val v = t.lerpFactor(0.0f, menuBgm)
            menuBgmPlayer.setVolume(v,v)
            now = System.currentTimeMillis()
            delay(16L)
        }
        menuBgmPlayer.setVolume(menuBgm, menuBgm)
    }

    fun removeAudioSource(){
        if(bgmPlayerActiveSrc != XmbItem.SILENT_AUDIO){
            if(bgmPlayer.isPlaying) bgmPlayer.stop()
            bgmPlayer.reset()
            bgmPlayerActiveSrc = XmbItem.SILENT_AUDIO

            // Continue menu BGM
            ctx.lifeScope.launch {
                resumeAndTurnVolumeUpMenuBgm()
            }
        }
    }

    fun loadSfxData(attach : Boolean = true){
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

    ///region Menu BGM Implementations

    private var _menuBgmIsReady = false
    private val _menuBgmFiles = arrayListOf<File>()
    private var _isMenuStarted = false

    fun openMenuBgmPicker(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "audio/*"
        val picker = Intent.createChooser(intent, ctx.getString(R.string.picker_pick_bgm))
        ctx.xmb.startActivityForResult(picker, PICKER_REQ_ID)
    }

    fun destroyMenuBgmPlayer(){
        menuBgmPlayer.reset()
        menuBgmPlayer.release()
    }

    fun initMenuBgm(){
        _menuBgmFiles.addAll(FileQuery(VshBaseDirs.VSH_RESOURCES_DIR)
            .withNames("menu_bgm")
            .withExtensions("bin")
            .onlyIncludeExists(false)
            .execute(ctx)
        )

        menuBgmPlayer.isLooping = true
        menuBgmPlayer.setOnPreparedListener {
            _menuBgmIsReady = true
            menuBgmPlayer.isLooping = true
            if(_isMenuStarted){
                menuBgmPlayer.start()
            }
        }
        menuBgmPlayer.setOnErrorListener { _, _, _ -> _menuBgmIsReady = false; true }
        menuBgmPlayer.setOnCompletionListener { _menuBgmIsReady = false }

        for(file in _menuBgmFiles){
            if(file.exists()){
                menuBgmPlayer.setDataSource(file.path)
                menuBgmPlayer.prepareAsync()
                break
            }
        }

    }

    fun deleteMenuBgm(){
        if(menuBgmPlayer.isPlaying){
            menuBgmPlayer.reset()
        }

        for(file in _menuBgmFiles){
            if(file.exists()){
                try {
                    file.delete()
                }catch (_:Exception){}
            }
        }
    }

    fun pauseMenuBgm(){
        if(_menuBgmIsReady && menuBgmPlayer.isPlaying){
            menuBgmPlayer.pause()
        }
    }

    fun resumeMenuBgm(){
        if(_menuBgmIsReady && !menuBgmPlayer.isPlaying){
            _isMenuStarted = true
            menuBgmPlayer.start()
        }
    }

    fun loadPickedMenuBgm(intent: Intent){
        val path = intent.data
        if(path == null){
            ctx.postNotification(
                R.drawable.ic_error,
                ctx.getString(R.string.error_common_header),
                ctx.getString(R.string.error_menubgm_load_nopath), 15.0f)
            return
        }

        try {
            val src = ctx.contentResolver.openInputStream(path)
            if(src == null){
                ctx.postNotification(
                    R.drawable.ic_error,
                    ctx.getString(R.string.error_common_header),
                    ctx.getString(R.string.error_menubgm_source_null), 15.0f)
                return
            }

            val f = _menuBgmFiles.firstOrNull()
            if(f == null) {
                ctx.postNotification(
                    R.drawable.ic_error,
                    ctx.getString(R.string.error_common_header),
                    ctx.getString(R.string.error_menubgm_copy_empty), 15.0f)
                return
            }

            if(!f.exists()){
                f.parentFile?.mkdirs()
                f.createNewFile()
            }

            val cOut = f.outputStream()
            src.copyTo(cOut)

            cOut.flush()
            cOut.close()
            src.close()

            // Should be finished by now, lets load
            if(menuBgmPlayer.isPlaying){
                menuBgmPlayer.stop()
            }
            menuBgmPlayer.reset()
            _isMenuStarted = true
            menuBgmPlayer.setDataSource(f.absolutePath)
            menuBgmPlayer.prepareAsync()
        }catch (e: FileNotFoundException){
            ctx.postNotification(
                R.drawable.ic_error,
                ctx.getString(R.string.error_common_header),
                ctx.getString(R.string.error_menubgm_copy_source_not_found), 15.0f)
        }
    }

    ///endregion
}