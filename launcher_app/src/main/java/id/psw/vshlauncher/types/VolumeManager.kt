package id.psw.vshlauncher.types

import android.content.SharedPreferences
import id.psw.vshlauncher.PrefEntry

class VolumeManager {
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
    private var _pref : SharedPreferences? = null

    private fun sendChange(channel:Channel){
        onVolumeChange?.invoke(channel, when(channel) {
            Channel.Bgm -> _master * _bgm
            Channel.Sfx -> _master * _sfx
            Channel.SystemBgm -> _master * _systemBgm
            else -> _master
        })


        _pref?.edit()?.putFloat(
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
        )?.apply()
    }

    var pref : SharedPreferences?
    get() = _pref
    set(v) { _pref = v; }

    fun readPreferences(){
        master = _pref?.getFloat(PrefEntry.VOLUME_AUDIO_MASTER, 1.0f) ?: 1.0f
        sfx = _pref?.getFloat(PrefEntry.VOLUME_AUDIO_SFX, 1.0f) ?: 1.0f
        bgm = _pref?.getFloat(PrefEntry.VOLUME_AUDIO_BGM, 1.0f) ?: 1.0f
        systemBgm = _pref?.getFloat(PrefEntry.VOLUME_AUDIO_SYSBGM, 1.0f) ?: 1.0f
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
}