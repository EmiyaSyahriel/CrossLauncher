package id.psw.vshlauncher.views

import android.graphics.Paint
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.makeTextPaint
import id.psw.vshlauncher.select

class XmbSideMenu(ctx:VSH) {
    data class Item(val name:String, val action: () -> Unit, val close : Boolean = true)

    // Display
    var isDisplayed = false
    var displayFactor = 0.0f

    // Menu Indexing
    var activeIndex = 0
    var items = arrayListOf<Item?>()

    // UI
    val menuTextPaint = ctx.makeTextPaint(
        size = 10.0f,
        align = Paint.Align.LEFT
    )

    fun open(items:ArrayList<Item?>, index:Int = 0){
        activeIndex = index
        isDisplayed = true
        this.items.addAll(items)
    }

    fun close(){
        isDisplayed = false
    }

    fun navigate(down:Boolean){
        val i = down.select(1, -1)
        while(activeIndex > 0 && activeIndex < items.size - 1 && items[activeIndex + i] == null){
            activeIndex += i
        }
    }

    fun execute(){
        val item = items[activeIndex] ?: return
        item.action.invoke()
        if(item.close){
            isDisplayed = false
        }
    }
}