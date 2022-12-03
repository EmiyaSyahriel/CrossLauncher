package id.psw.crosslauncher.xlib

import android.app.Service
import android.content.Intent
import android.os.IBinder

open class XMBStatusTextPluginService : Service() {
    protected lateinit var that : XMBStatusTextPluginService

    protected val boundData = object : IXMBStatusTextKey.Stub() {
        override fun getKeys(): MutableList<String> = that.getKeys()
        override fun getKeyDescription(key: String?): String = that.getKeyDescription(key)
        override fun format(key: String?, param: String?): String = that.format(key, param)
    }

    override fun onBind(p0: Intent?): IBinder? {
        that = this
        return boundData
    }

    open fun getKeys() : ArrayList<String> = arrayListOf()
    open fun getKeyDescription(s:String?)  : String = ""
    open fun format(key:String?, param:String?) : String = ""
}