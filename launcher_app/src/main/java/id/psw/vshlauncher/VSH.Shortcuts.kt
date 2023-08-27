package id.psw.vshlauncher

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Build
import id.psw.vshlauncher.VSH.Companion.ITEM_CATEGORY_SHORTCUT
import id.psw.vshlauncher.types.FileQuery
import id.psw.vshlauncher.types.items.XMBShortcutItem
import java.io.File

/**
 * Shortcut will be located at `(Cross Launcher Data)/files/dev_hdd0/shortcuts/`
 * Each shortcut is it's own directory, each with randomized id (to mitigate apps that uses unacceptable id and package name)
 * Contains :
 * - `SHORTCUT.INI` - Main Shortcut Definition
 * - `ICON0.PNG` - Shortcut Icon
 *
 * Shortcut uses same customization directory structure as Apps
 */
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

        c.content.clear()
        System.gc()

        val paths = FileQuery(VshBaseDirs.SHORTCUTS_DIR).execute(this)
        for(path in paths){
            if(path.isDirectory){
                val scDirs = path.listFiles { dir, _ -> dir.isDirectory } ?: continue
                for(sc in scDirs){
                    val ini = File(sc, "SHORTCUT.INI")
                    if(ini.exists() || ini.isFile){
                        val app = XMBShortcutItem(vsh, ini)
                        addToCategory(ITEM_CATEGORY_SHORTCUT, app)
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