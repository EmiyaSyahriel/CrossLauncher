package id.psw.crosslauncher.xlib

open class Property<T> {
    open var content: T? = null
    open val has: Boolean get() = content != null
    open val get: T get() = content!!
}