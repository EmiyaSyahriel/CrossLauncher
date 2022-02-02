package id.psw.vshlauncher

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import android.os.Build
import id.psw.vshlauncher.types.XMBAppItem
import id.psw.vshlauncher.types.XMBItem

fun VSH.isAGame(rInfo: ResolveInfo): Boolean {
    val appInfo = packageManager.getApplicationInfo(rInfo.activityInfo.packageName, 0)
    var retval = false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        retval = retval || appInfo.flags hasFlag ApplicationInfo.FLAG_IS_GAME
    }
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        retval = retval || appInfo.category == ApplicationInfo.CATEGORY_GAME
    }
    retval = retval || gameFilterList.contains(rInfo.activityInfo.name)
    return retval
}

fun VSH.reloadAppList(){
    threadPool.execute {
        val intent = Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        packageManager.queryIntentActivities(intent, 0).forEach {
            val lh = addLoadHandle()
            val item = XMBAppItem(vsh, it)
            addToCategory(isAGame(it).select(VSH.ITEM_CATEGORY_GAME,VSH.ITEM_CATEGORY_APPS), item)
            setLoadingFinished(lh)
        }
    }
}