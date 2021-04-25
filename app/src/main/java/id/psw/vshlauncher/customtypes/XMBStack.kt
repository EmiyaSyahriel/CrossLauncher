package id.psw.vshlauncher.customtypes

import android.util.Log
import kotlin.collections.ArrayList

class XMBStack<T> {
    companion object{
        private const val TAG : String = ""
    }
    private var internalList : ArrayList<T> = arrayListOf()

    fun push(item:T){ internalList.add(item) }
    fun push(item:Collection<T>){ item.forEach{ internalList.add(it) } }

    fun peek(defVal : T? = null) : T? {
        var retval : T? = defVal
        if(internalList.size > 0) retval = internalList.last()
        return retval
    }

    val count : Int
    get() = internalList.size

    operator fun get(index:Int) : T { return internalList[index] }

    fun pop(defVal : T? = null) : T?{
        var retval : T? = defVal

        if(internalList.size > 0){
            retval = internalList.last()
            internalList.removeAt(internalList.lastIndex)
        }

        Log.d(TAG, "Popped item : ${retval ?: "Unknown"}")
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

    operator fun set(t: Int, value: T) { internalList[t] = value }
}