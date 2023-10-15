package id.psw.vshlauncher.submodules

import android.content.Context
import android.content.SharedPreferences
import id.psw.vshlauncher.Logger
import id.psw.vshlauncher.Vsh
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

typealias PreferenceChangeCallback = (key:String, isNew:Boolean, prevValue:Any, newValue:Any) -> Unit

class PreferenceSubmodule(private val ctx: Vsh) : IVshSubmodule {
    companion object {
        private const val PREF_NAME = "xRegistry.sys"
        private const val PREF_PUSH_CHECK_EVERY = 30L
        private const val PREF_PUSH_DELAY = 300L
        private const val TAG = "xRegistry"
    }

    private lateinit var pref : SharedPreferences
    private lateinit var edit : SharedPreferences.Editor
    private val onChanges = arrayListOf<PreferenceChangeCallback>()
    private var hasChange = false
    private val mutex = Mutex(false)
    private var pushCheckDelay = 0L
    private var keepPusherThread = true

    override fun onCreate() {
        pref = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        edit = pref.edit()
        ctx.lifeScope.launch {
            keepPusherThread = true
            withContext(Dispatchers.IO) {
                while(keepPusherThread){
                    delay(PREF_PUSH_CHECK_EVERY)
                    if(pushCheckDelay > 0){
                        pushCheckDelay -= PREF_PUSH_CHECK_EVERY
                        if(pushCheckDelay <= 0)  push()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        keepPusherThread = false
    }

    fun registerOnChange(onChange : PreferenceChangeCallback) {
        if(!onChanges.contains(onChange)){
            onChanges.add(onChange)
        }
    }

    fun unregisterOnChange(onChange : PreferenceChangeCallback){
        if(onChanges.contains(onChange)){
            onChanges.remove(onChange)
        }
    }

    fun get(id: String, def: Int = 0): Int =
        synchronized(mutex) { return pref.getInt(id, def) }
    fun get(id: String, def: String = ""): String =
        synchronized(mutex) { return pref.getString(id, def) ?: def }
    fun get(id: String, def: Set<String>? = null): Set<String>? =
        synchronized(mutex) { return pref.getStringSet(id, def) ?: def}
    fun get(id: String, def: Boolean = false): Boolean =
        synchronized(mutex) { return pref.getBoolean(id, def)}
    fun get(id: String, def: Float = 0.0f): Float =
        synchronized(mutex) { return pref.getFloat(id, def)}
    fun get(id: String, def: Long = 0L): Long =
        synchronized(mutex) { return pref.getLong(id, def)}

    private fun <T> set(
        id: String,
        v: T,
        checkContains: (String) -> Boolean,
        getter: (String, T) -> T,
        setter: (String, T) -> Unit
    ): PreferenceSubmodule {
        synchronized(mutex) {
            val prev = getter(id, v)
            setter(id, v)
            val isNew = checkContains(id) || prev == null
            onChanges.forEach { it.invoke(id, isNew, prev as Any, v as Any) }
            hasChange = true
            pushCheckDelay = PREF_PUSH_DELAY
        }
        return this
    }

    fun push(){
        synchronized(mutex){
            if(hasChange){
                edit.apply()
                hasChange = false
                Logger.d(TAG, "Preference saved")
            }
        }
    }

    fun set(id:String, v:Int) : PreferenceSubmodule =         set(id, v, pref::contains, pref::getInt, edit::putInt)
    fun set(id:String, v:String) : PreferenceSubmodule =      set(id, v, pref::contains, pref::getString, edit::putString)
    fun set(id:String, v:Set<String>) : PreferenceSubmodule = set(id, v, pref::contains, pref::getStringSet, edit::putStringSet)
    fun set(id:String, v:Boolean) : PreferenceSubmodule =     set(id, v, pref::contains, pref::getBoolean, edit::putBoolean)
    fun set(id:String, v:Float) : PreferenceSubmodule =       set(id, v, pref::contains, pref::getFloat, edit::putFloat)
    fun set(id:String, v:Long) : PreferenceSubmodule =        set(id, v, pref::contains, pref::getLong, edit::putLong)
    operator fun contains(id:String) : Boolean = synchronized(mutex) { return pref.contains(id) }
    fun remove(id:String) : PreferenceSubmodule = synchronized(mutex) { edit.remove(id); return this }
}