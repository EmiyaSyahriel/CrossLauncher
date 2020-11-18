package id.psw.vshlauncher

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import androidx.core.graphics.drawable.toBitmap

open class VshY {
    class VshOption {
        var name : String = ""
        var onClick : Runnable = EmptyRunnable
        val shouldSkip : Boolean get() = name.isEmpty()
        var enabled : Boolean = true
        constructor()
        constructor(name:String, onClick: Runnable){ this.name = name;this.onClick = onClick;enabled = true }
        constructor(name:String, onClick: Runnable, enabled :Boolean){ this.name = name;this.onClick = onClick; this.enabled = enabled }
    }

    class VshOptionsBuilder{
        private val internalList = arrayListOf<VshOption>()
        fun add(name:String, onClick : Runnable): VshOptionsBuilder{
            internalList.add(VshOption(name, onClick))
            return this
        }
        fun add(name:String, onClick : () -> Unit):VshOptionsBuilder{
            internalList.add(VshOption(name, Runnable(onClick)))
            return this
        }
        fun separator():VshOptionsBuilder{
            internalList.add(VshOption())
            return this
        }
        fun build(): ArrayList<VshOption> = internalList
    }

    companion object{
        val EmptyRunnable = Runnable { /**Do Nothing**/ }
        val transparentBitmap = ColorDrawable(Color.TRANSPARENT).toBitmap(1,1)
        val transparentDrawable = BitmapDrawable(transparentBitmap)
    }

    var itemID : Int = 0

    /** Tell the view to render the icon in 20:11 AR instead of 1:1 icon */
    open val matchPSContentIcon : Boolean = false

    open val selectedIcon : Bitmap = transparentBitmap
    open val unselectedIcon : Bitmap = transparentBitmap

    open val name : String = "VshVerticalIcon"
    open val hasDescription : Boolean = false
    open val description = "An icon"
    open val onLaunch : Runnable = EmptyRunnable
    open val hasOptions : Boolean = false
    open val options : ArrayList<VshOption> = arrayListOf()

    var isCoordinatelyVisible : Boolean = false
        set(value){
            if(field != value){
                if(value) onScreen() else onHidden()
            }
                field = value
        }

    var isSelected : Boolean = false
    set(value){
        if(field != value){
            if(value) onSelected() else onUnselected()
        }
        field = value
    }

    /** Called when this icon is shown into screen after being hidden */
    open fun onScreen(){}

    /** Called when this icon is hidden into screen after being shown */
    open fun onHidden(){}

    /** Called when is currently selected */
    open fun onSelected(){}

    /** Called when is no longer selected */
    open fun onUnselected(){}

    constructor()
    constructor(itemID:Int) { this.itemID = itemID  }
}
