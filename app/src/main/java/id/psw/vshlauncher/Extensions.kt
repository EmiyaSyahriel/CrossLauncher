package id.psw.vshlauncher

import android.content.Context

/**
 * Cast get instance of VSH, either from the application context or the calling context
 */
val Context.vsh : VSH
    get() {
            // Check if current context is an VSH App Context
        if(this is VSH) return this as VSH
            // Check if current context's application is an VSH App Context
        if(this.applicationContext is VSH) return this.applicationContext as VSH
        return this as VSH // TODO: Do additional checking
    }

fun <T> Boolean.select(a:T, b:T) : T =  if(this) a else b