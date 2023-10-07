package id.psw.vshlauncher

import id.psw.vshlauncher.types.XmbItem

fun Vsh.addToCategory(categoryId:String, item:XmbItem) : Boolean {
    synchronized(categories){
        val category = categories.find { it.id == categoryId }
        if(category != null){
            category.addItem(item)
            return true
        }
        return false
    }
}

fun Vsh.removeFromCategory(categoryId:String, itemId:String) : Boolean {
    synchronized(categories){
        return categories.find {it.id == categoryId}?.content?.removeAll { it.id == itemId } ?: false
    }
}

fun Vsh.swapCategory(itemId:String, categoryFrom:String, categoryTo:String) : Boolean {
    synchronized(categories){
        val srcCat = categories.find { it.content.find { itm -> itm.id == itemId } != null }

        val item = srcCat?.content?.find { it.id == itemId }
        var dstCat = categories.find {it.id == categoryTo }
        // Handle default
        if(categoryTo.isEmpty() || dstCat == null ){
            val entry = vsh.allAppEntries.find { it.id == itemId }
            if(entry != null){
                val id = isAGame(entry.resInfo).select(Vsh.ITEM_CATEGORY_GAME, Vsh.ITEM_CATEGORY_APPS)
                dstCat = categories.find { it.id == id }
            }
        }
        if(srcCat != null && item != null && dstCat != null){
            dstCat.addItem(item)
            srcCat.content.removeAll{ it.id == itemId }
            dstCat.onSwitchSortFunc
            return true
        }else{
            vsh.postNotification(R.drawable.ic_error,
                vsh.getString(R.string.error_category_switch_failed_title),
                vsh.getString(R.string.error_category_switch_failed_desc).format(itemId, categoryFrom, categoryTo),
                5.0f
            )
        }
    }
    return false
}

fun Vsh.getItemFromCategory(categoryId: String, itemId:String) : XmbItem? {
    synchronized(categories) {
        return categories.find { it.id == categoryId }?.content?.find { it.id == itemId }
    }
}

