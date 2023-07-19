package id.psw.vshlauncher.submodules

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import id.psw.crosslauncher.xlib.PluginErrorReason
import id.psw.crosslauncher.xlib.XMBPlugin
import id.psw.vshlauncher.VSH
import org.xmlpull.v1.XmlPullParser
import java.security.InvalidParameterException

class PluginManager(private val vsh : VSH) {
    data class PluginInfo(
        val context : Context?,
        val err : PluginErrorReason,
        val exception : Exception?,
        val plugins : ArrayList<XMBPlugin>
    )

    private val pluginList = ArrayList<PluginInfo>()
    fun reloadPluginList(){
        val i = Intent().addCategory(XMBPlugin.CATEGORY_PLUGIN)
        val qs = vsh.packageManager.queryIntentActivities(i, 0)
        pluginList.clear()
        for(q in qs){
            val p = loadPlugin(q.activityInfo.packageName)
            pluginList.add(p)
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun loadPlugin(pkgName:String) : PluginInfo {
        val retval = arrayListOf<XMBPlugin>()
        var lastException : Exception? = null
        var err : PluginErrorReason = PluginErrorReason.None

        var pluginCtx : Context? = null
        // We need to load the plugin code to this process so that
        // the Launcher can execute the code
        run ldscope@ {
            pluginCtx = vsh.createPackageContext(pkgName, Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY)

            try{
                val pluginRes = pluginCtx!!.resources
                val pluginXml = pluginRes.getIdentifier("xmb_plugins", "xml", "")

                if(pluginXml == 0) {
                    err = PluginErrorReason.NoDefinition
                    return@ldscope
                }
                val parser = pluginRes.getXml(pluginXml)
                var eventType = parser.eventType
                val pluginClasses = arrayListOf<String>()

                while(eventType != XmlPullParser.END_DOCUMENT){
                    when(eventType){
                        XmlPullParser.END_TAG -> when(parser.name){
                            "plugin" -> {
                                pluginClasses.add(parser.getAttributeValue(null, "className"))
                            }
                            "cxlpluginlist" -> {
                                val ver = parser.getAttributeValue(null, "apiVersion")
                                if(ver != XMBPlugin.API_VERSION){
                                    throw InvalidParameterException("Plugin version mismatch : Loader ${XMBPlugin.API_VERSION} == Plugin $ver")
                                }
                            }
                        }
                    }
                    eventType =parser.next()
                }

                for(cName in pluginClasses){
                    try {
                        val pluginCls = pluginCtx!!.classLoader.loadClass(cName)
                        val pluginClsC = pluginCls.getConstructor(Context::class.java)
                        val pluginInst = pluginClsC.newInstance(pluginCtx!!)
                        if(pluginInst is XMBPlugin){
                            pluginInst.onStart()
                            retval.add(pluginInst)
                        }
                    }catch(xe:Exception){
                        lastException = xe
                        err = PluginErrorReason.PartialLoadException
                    }
                }
            }catch(e:Exception){
                lastException = e
                err = PluginErrorReason.TotalLoadException
            }
        }

        return PluginInfo(pluginCtx, err, lastException, retval)
    }
}