package id.psw.vshlauncher

import id.psw.vshlauncher.types.XMBItem

fun Vsh.addToCategory(categoryId:String, item:XMBItem) : Boolean {
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

fun Vsh.getItemFromCategory(categoryId: String, itemId:String) : XMBItem? {
    synchronized(categories) {
        return categories.find { it.id == categoryId }?.content?.find { it.id == itemId }
    }
}

