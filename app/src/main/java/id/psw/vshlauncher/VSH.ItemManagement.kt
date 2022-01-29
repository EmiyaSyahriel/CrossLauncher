package id.psw.vshlauncher

import id.psw.vshlauncher.types.XMBItem

fun VSH.addToCategory(categoryId:String, item:XMBItem) : Boolean {
    val category = categories.find { it.id == categoryId }
    if(category != null){
        category.addItem(item)
        return true
    }
    return false
}

fun VSH.removeFromCategory(categoryId:String, itemId:String) : Boolean {
    return categories.find {it.id == categoryId}?.content?.removeAll { it.id == itemId } ?: false
}

fun VSH.getItemFromCategory(categoryId: String, itemId:String) : XMBItem? {
    return categories.find { it.id == categoryId }?.content?.find { it.id == itemId }
}

