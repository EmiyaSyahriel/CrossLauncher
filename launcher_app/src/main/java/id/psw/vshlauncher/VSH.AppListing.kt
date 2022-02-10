package id.psw.vshlauncher

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import android.os.Build
import id.psw.vshlauncher.types.items.XMBAppItem

fun VSH.isAGame(rInfo: ResolveInfo): Boolean {
    val appInfo = packageManager.getApplicationInfo(rInfo.activityInfo.packageName, 0)
    var retval = false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        retval = retval || appInfo.flags hasFlag ApplicationInfo.FLAG_IS_GAME
    }
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        retval = retval || appInfo.category == ApplicationInfo.CATEGORY_GAME
    }
    retval = retval || (gameFilterList.indexOfFirst { rInfo.activityInfo.name.lowercase().contains(it.lowercase()) } >= 0)
    return retval
}

fun VSH.reloadAppList(){
    threadPool.execute {
        // categories.find {it.id == VSH.ITEM_CATEGORY_GAME }
        val intent = Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val lh = addLoadHandle()
        packageManager.queryIntentActivities(intent, 0).forEach {
            val item = XMBAppItem(vsh, it)
            addToCategory(isAGame(it).select(VSH.ITEM_CATEGORY_GAME,VSH.ITEM_CATEGORY_APPS), item)
        }
        Thread.sleep(5000)
        setLoadingFinished(lh)
    }
}