package id.psw.vshlauncher.types

class Stack<T> {
    private val backingList = arrayListOf<T>()

    fun clear() = backingList.clear()
    operator fun get(idx:Int) = backingList[idx]
    fun push(item:T) = backingList.add(item)
    fun peek() : T = backingList.last()
    fun pull() : T = backingList.removeAt(backingList.size - 1)
    val size : Int get()= backingList.size
}