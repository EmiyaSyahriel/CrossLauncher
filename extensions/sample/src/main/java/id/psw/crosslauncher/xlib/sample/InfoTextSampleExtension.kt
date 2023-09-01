package id.psw.crosslauncher.xlib.sample

import android.content.Context
import id.psw.crosslauncher.xlib.XmbStatusTextExtension

class InfoTextSampleExtension(host: Context, self:Context) : XmbStatusTextExtension(host, self) {
    companion object {
        private val _keys = arrayOf("sample_hello", "sample_foobar")
    }

    override val pluginId: String
        get() = "InfoTextSample"

    override val keys: Array<out String>
        get() = _keys

    override fun getString(key: String): String? {
        return when(key){
            _keys[0] -> "Hello World"
            _keys[1] -> "Foo Bar"
            else -> "(Not Supported)"
        }
    }
}