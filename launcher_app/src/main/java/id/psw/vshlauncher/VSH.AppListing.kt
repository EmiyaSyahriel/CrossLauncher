package id.psw.vshlauncher

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.util.Log
import id.psw.vshlauncher.types.items.XMBAppItem
import id.psw.vshlauncher.types.items.XMBItemCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "GameCustomMigrate"
fun Vsh.isAGame(rInfo: ResolveInfo): Boolean {
    val appInfo = packageManager.getApplicationInfo(rInfo.activityInfo.packageName, 0)
    var retval = false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        retval = retval || appInfo.flags hasFlag ApplicationInfo.FLAG_IS_GAME
    }
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        retval = retval || appInfo.category == ApplicationInfo.CATEGORY_GAME
    }
    return retval
}

fun Vsh.appCategorySorting(it: XMBItemCategory) {
    val newSort = when(it.getProperty(Consts.XMB_KEY_APP_SORT_MODE, AppItemSorting.FileSize)){
            AppItemSorting.Name -> AppItemSorting.PackageName
            AppItemSorting.PackageName -> AppItemSorting.FileSize
            AppItemSorting.FileSize -> AppItemSorting.UpdateTime
            AppItemSorting.UpdateTime -> AppItemSorting.Name
        }
    it.setSort(newSort)
    it.setProperty(Consts.XMB_KEY_APP_SORT_MODE, newSort)
}

fun Vsh.appCategorySetSorting(it:XMBItemCategory, newSort:Any){
    if(newSort is AppItemSorting){
        it.content.sortBy { item ->
            if(item is XMBAppItem){
                when(newSort){
                    AppItemSorting.Name -> item.displayName
                    AppItemSorting.UpdateTime -> item.sortUpdateTime
                    AppItemSorting.FileSize -> item.fileSize
                    AppItemSorting.PackageName -> item.resInfo.uniqueActivityName
                    else -> item.displayName
                }
            }else{
                it.displayName
            }
        }

    }
}

fun Vsh.appCategorySortingName(it : XMBItemCategory) : String{
    return when(it.getProperty(Consts.XMB_KEY_APP_SORT_MODE, AppItemSorting.Name)){
        AppItemSorting.Name -> vsh.getString(R.string.app_sorting_name)
        AppItemSorting.UpdateTime -> vsh.getString(R.string.app_sorting_updtime)
        AppItemSorting.FileSize -> vsh.getString(R.string.app_sorting_filesize)
        AppItemSorting.PackageName -> vsh.getString(R.string.app_sorting_actname)
    }
}

fun Vsh.tryMigrateOldGameDirectory(){
    if(!runtimeTriageCheck(TAG)) return

    val storages = getExternalFilesDirs(null)
    for(storage in storages){
        val src = storage.combine("dev_hdd0","games")
        val dst = storage.combine("dev_hdd0","game")

        if(src == null || dst == null) continue

        if(!src.isDirectory) continue

        Log.w(TAG, "Found old directory of app customization, migrating...")
        if(dst.isDirectory) {
            Log.w(TAG, "Both new and old directory is present, Migration cancelled")
            continue
        }

        try{
            src.renameTo(dst)
        }catch (e:Exception){
            Log.e(TAG, "Migration error", e)
        }
    }
}

fun Vsh.reloadAppList(){
    vsh.lifeScope.launch {
        withContext(Dispatchers.Main){
            XMBAppItem.showHiddenByConfig = false
            val gameCat = categories.find {it.id == Vsh.ITEM_CATEGORY_GAME }
            if(gameCat != null){
                synchronized(gameCat){
                    gameCat.onSetSortFunc = { it, sort ->  appCategorySetSorting(it, sort) }
                    gameCat.onSwitchSortFunc = { appCategorySorting(it) }
                    gameCat.getSortModeNameFunc = { appCategorySortingName(it) }
                }
            }
            val appCat = categories.find{ it.id == Vsh.ITEM_CATEGORY_APPS }
            if(appCat != null){
                synchronized(appCat){
                    appCat.onSetSortFunc = { it, sort -> appCategorySetSorting(it, sort) }
                    appCat.onSwitchSortFunc = { appCategorySorting(it) }
                    appCat.getSortModeNameFunc = { appCategorySortingName(it) }
                }
            }

            val intent = Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val lh = addLoadHandle()
            if (sdkAtLeast(Build.VERSION_CODES.TIRAMISU)) {
                packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION") // API Below TIRAMISU
                packageManager.queryIntentActivities(intent, 0)
            }.forEach {
                val item = XMBAppItem(vsh, it)
                val isGame = isAGame(it)
                addToCategory(isGame.select(Vsh.ITEM_CATEGORY_GAME, Vsh.ITEM_CATEGORY_APPS), item)
                isGame.select(gameCat, appCat)?.setSort(AppItemSorting.Name)
            }

            setLoadingFinished(lh)
        }
    }
}