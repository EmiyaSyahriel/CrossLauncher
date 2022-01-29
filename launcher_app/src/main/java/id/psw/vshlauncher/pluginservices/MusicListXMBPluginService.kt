package id.psw.vshlauncher.pluginservices

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import id.psw.vshlauncher.IXMBIconListProvider
import id.psw.vshlauncher.R
import id.psw.vshlauncher.types.ExternalXMBItem
import id.psw.vshlauncher.types.ExternalXMBItemHandle
import id.psw.vshlauncher.types.ExternalXMBItemMenu

class MusicListXMBPluginService : Service() {

    companion object{
        const val TAG = "music.sprx"
        const val VERSION = "1.0.0_25012022"
    }

    private class IMusicListIBinder(private var ctx: MusicListXMBPluginService) : IXMBIconListProvider.Stub() {

        override fun getName(): String = ctx.getString(R.string.plugin_name_musiclist)
        override fun getDescription(): String = ctx.getString(R.string.plugin_desc_musiclist)
        override fun getVersionString(): String = VERSION

        override fun shouldUpdateCategory(categoryId: String?): Boolean {
            TODO("Not yet implemented")
        }

        override fun shouldUpdateItem(id: String?, subId: Int): Boolean {
            TODO("Not yet implemented")
        }

        override fun getCategories(): MutableList<ExternalXMBItemHandle> {
            TODO("Not yet implemented")
        }

        override fun getItemsAtCategory(categoryId: String?): MutableList<ExternalXMBItemHandle> {
            TODO("Not yet implemented")
        }

        override fun getChildOf(id: String?, subId: Int): MutableList<ExternalXMBItemHandle> {
            TODO("Not yet implemented")
        }

        override fun getCategoryData(id: String?): ExternalXMBItem {
            TODO("Not yet implemented")
        }

        override fun getItemData(id: String?, subId: Int): ExternalXMBItem {
            TODO("Not yet implemented")
        }

        override fun getMenu(id: String?, subId: Int): MutableList<ExternalXMBItemMenu> {
            TODO("Not yet implemented")
        }

        override fun run(id: String?, subId: Int) {
            TODO("Not yet implemented")
        }

        override fun runMenu(id: String?, subId: Int, menuId: Int) {
            TODO("Not yet implemented")
        }

    }

    private var __plginInterface : IMusicListIBinder? = null

    private fun getCachedPluginInterface(): IMusicListIBinder {
        if(__plginInterface == null) __plginInterface = IMusicListIBinder(this)
        return __plginInterface!!
    }

    override fun onCreate() {
        Log.d(TAG, "Music List Module Started")
    }

    override fun onBind(intent: Intent): IBinder = getCachedPluginInterface()
}