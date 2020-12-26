package id.psw.vshlauncher.customtypes

import id.psw.vshlauncher.VshY
import kotlin.collections.ArrayList

class XMBStack<T> {
    private var internalList : ArrayList<T> = arrayListOf()

    fun push(item:T){ internalList.add(item) }
    fun push(item:Collection<T>){ item.forEach{ internalList.add(it) } }

    fun peek(defVal : T? = null) : T? {
        var retval : T? = defVal
        if(internalList.size > 0) retval = internalList.last()
        return retval
    }

    var count : Int = internalList.size

    fun pop(defVal : T? = null) : T?{
        var retval : T? = defVal
        if(internalList.size > 0){
            retval = internalList.last()
            internalList.removeAt(internalList.lastIndex)
        }
        return retval
    }

    fun firstData() : T? = internalList[0]

    fun isEmpty(): Boolean = internalList.isEmpty()
    fun hasContent() : Boolean = internalList.isNotEmpty()

    fun tidyContent(){
        val newList = arrayListOf<T>()
        internalList.forEach { if (it != null) newList.add(it) }
        internalList = newList
    }
}