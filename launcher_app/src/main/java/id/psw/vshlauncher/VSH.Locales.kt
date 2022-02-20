@file:Suppress("SpellCheckingInspection")

package id.psw.vshlauncher

import java.util.*

val VSH.supportedLocaleList: ArrayList<Locale>
    get() = arrayListOf(
        Locale.getDefault(),
        Locale("id"),
        /* Ngoko Tulungagung variant of Javanese at Indonesia */
        Locale("jv","ID","ngokotulungagung"),
        Locale("jv","ID","malang"),
    )
