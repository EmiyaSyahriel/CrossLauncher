package id.psw.vshlauncher

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Build
import id.psw.vshlauncher.VSH.Companion.ITEM_CATEGORY_SHORTCUT
import id.psw.vshlauncher.types.FileQuery
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.types.items.XMBShortcutItem


fun VSH.reloadShortcutList(){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Logger.d("SHORTCUT", "Getting shortcuts...")
        val apl = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        if(apl.hasShortcutHostPermission()){
            for(p in apl.profiles){
                val q = LauncherApps.ShortcutQuery()
                val ss = apl.getShortcuts(q, p)
                if( ss != null){
                    for(s in ss){
                        Logger.d("SHORTCUT", "${s.id} - ${s.`package`} - ${s.intent} - ${s.activity?.packageName}")
                    }
                }
            }
        }
    } else {
        // TODO("VERSION.SDK_INT < Q")
    }

    threadPool.execute {
        val h = addLoadHandle()
        val c = categories.find { it.id == ITEM_CATEGORY_SHORTCUT }!!

        for(i in c.content){
            if(i.icon != XMBItem.TRANSPARENT_BITMAP){
                i.icon.recycle()
            }
        }

        c.content.clear()

        val paths = FileQuery(VshBaseDirs.USER_DIR).atPath("shortcuts").execute(this)
        for(path in paths){
            if(path.exists()){
                val inis = path.listFiles { a, b ->
                    b.endsWith("ini", true)
                }
                if(inis != null){
                    for(ini in inis){
                        if(ini.exists()){
                            c.content.add(XMBShortcutItem(vsh, ini))
                        }
                    }
                }
            }
        }

        if(c.content.isNotEmpty()){
            if(hiddenCategories.contains(ITEM_CATEGORY_SHORTCUT)){
                hiddenCategories.remove(ITEM_CATEGORY_SHORTCUT)
            }
        }else{
            hiddenCategories.add(ITEM_CATEGORY_SHORTCUT)
        }

        setLoadingFinished(h)
    }
}