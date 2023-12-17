package id.psw.vshlauncher.submodules

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.util.Log
import id.psw.vshlauncher.AppItemSorting
import id.psw.vshlauncher.Consts
import id.psw.vshlauncher.R
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.addLoadHandle
import id.psw.vshlauncher.addToCategory
import id.psw.vshlauncher.combine
import id.psw.vshlauncher.hasFlag
import id.psw.vshlauncher.sdkAtLeast
import id.psw.vshlauncher.select
import id.psw.vshlauncher.setLoadingFinished
import id.psw.vshlauncher.types.items.XmbAppItem
import id.psw.vshlauncher.types.items.XmbItemCategory
import id.psw.vshlauncher.uniqueActivityName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

class AppListingSubmodule(private val vsh : Vsh) : IVshSubmodule {
    companion object {
        const val TAG = "AppListing"
    }

    override fun onCreate() {
    }

    override fun onDestroy() {
    }

    fun tryMigrateOldGameDirectory(){
        if(!vsh.runtimeTriageCheck(TAG)) return

        val storages = vsh.getExternalFilesDirs(null)
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

    private fun appCategorySortingName(it : XmbItemCategory) : String{
        return when(it.getProperty(Consts.XMB_KEY_APP_SORT_MODE, AppItemSorting.Name)){
            AppItemSorting.Name -> vsh.getString(R.string.app_sorting_name)
            AppItemSorting.UpdateTime -> vsh.getString(R.string.app_sorting_updtime)
            AppItemSorting.FileSize -> vsh.getString(R.string.app_sorting_filesize)
            AppItemSorting.PackageName -> vsh.getString(R.string.app_sorting_actname)
        }
    }

    private fun appCategorySorting(it: XmbItemCategory) {
        val newSort = when(it.getProperty(Consts.XMB_KEY_APP_SORT_MODE, AppItemSorting.FileSize)){
            AppItemSorting.Name -> AppItemSorting.PackageName
            AppItemSorting.PackageName -> AppItemSorting.FileSize
            AppItemSorting.FileSize -> AppItemSorting.UpdateTime
            AppItemSorting.UpdateTime -> AppItemSorting.Name
        }
        it.setSort(newSort)
        it.setProperty(Consts.XMB_KEY_APP_SORT_MODE, newSort)
    }

    private fun getAppInfoCompat(pkgName: String) : ApplicationInfo {
        return if(sdkAtLeast(33)){
            vsh.packageManager.getApplicationInfo(pkgName, PackageManager.ApplicationInfoFlags.of(0))
        }else{
            @Suppress("DEPRECATION") // Old application info
            vsh.packageManager.getApplicationInfo(pkgName, 0)
        }
    }

    private fun queryIntentCompat(intent:Intent) : List<ResolveInfo> {
        return if (sdkAtLeast(Build.VERSION_CODES.TIRAMISU)) {
            vsh.packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION") // API Below TIRAMISU
            vsh.packageManager.queryIntentActivities(intent, 0)
        }
    }

    fun isAGame(rInfo: ResolveInfo): Boolean {
        val appInfo = getAppInfoCompat(rInfo.activityInfo.packageName)

        var retval = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            @Suppress("DEPRECATION") // Or is a game
            retval = appInfo.flags hasFlag ApplicationInfo.FLAG_IS_GAME
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            retval = retval || appInfo.category == ApplicationInfo.CATEGORY_GAME
        }
        return retval
    }

    private fun appCategorySetSorting(it:XmbItemCategory, newSort:Any){
        if(newSort is AppItemSorting){
            it.content.sortBy { item ->
                if(item is XmbAppItem){
                    when(newSort){
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

    fun reloadAppList(){
        vsh.lifeScope.launch {
            withContext(Dispatchers.IO){
                XmbAppItem.showHiddenByConfig = false

                val gameCat = vsh.categories.find {it.id == Vsh.ITEM_CATEGORY_GAME }
                if(gameCat != null){
                    synchronized(gameCat){
                        gameCat.onSetSortFunc = { it, sort -> appCategorySetSorting(it, sort) }
                        gameCat.onSwitchSortFunc = { appCategorySorting(it) }
                        gameCat.getSortModeNameFunc = { appCategorySortingName(it) }
                    }
                }
                val appCat = vsh.categories.find{ it.id == Vsh.ITEM_CATEGORY_APPS }
                if(appCat != null){
                    synchronized(appCat){
                        appCat.onSetSortFunc = { it, sort -> appCategorySetSorting(it, sort) }
                        appCat.onSwitchSortFunc = { appCategorySorting(it) }
                        appCat.getSortModeNameFunc = { appCategorySortingName(it) }
                    }
                }

                val intent = Intent(Intent.ACTION_MAIN, null)
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                val lh = vsh.addLoadHandle()
                queryIntentCompat(intent).forEach {

                    // Wait until rendering ends to prevent ConcurrentModificationException
                    while(vsh.isNowRendering){
                        yield()
                    }

                    val item = XmbAppItem(vsh, it)
                    vsh.allAppEntries.add(item)
                    val cat = vsh.categories.find { cc -> cc.id == item.appCategory }
                    if(cat == null) {
                        val isGame = isAGame(it)
                        vsh.addToCategory(isGame.select(Vsh.ITEM_CATEGORY_GAME, Vsh.ITEM_CATEGORY_APPS), item)
                        isGame.select(gameCat, appCat)?.setSort(AppItemSorting.Name)
                    } else {
                        vsh.addToCategory(cat.id, item)
                        if(cat.id == Vsh.ITEM_CATEGORY_APPS) appCat?.setSort(AppItemSorting.Name)
                        if(cat.id == Vsh.ITEM_CATEGORY_GAME) gameCat?.setSort(AppItemSorting.Name)
                    }
                }

                vsh.setLoadingFinished(lh)
            }
        }
    }
}