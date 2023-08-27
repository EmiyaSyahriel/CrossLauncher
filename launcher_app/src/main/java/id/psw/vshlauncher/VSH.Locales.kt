@file:Suppress("SpellCheckingInspection")

package id.psw.vshlauncher

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.annotation.StringRes
import java.util.*

val VSH.supportedLocaleList: ArrayList<Locale?>
    get() = arrayListOf(
        null,
        Locale("en"),
        Locale("in","ID"),
        Locale("ms","MY"),
        Locale("ja","JP"),
        /* Ngoko (to lower age) Tulungagung Javanese at Indonesia */
        Locale("jv","ID","ltlagung"),
        /* Malang (to lower age) Javanese at Indonesia */
        Locale("jv","ID","jvlmalang"),
    )

fun VSH.createLocalizedContext(locale: Locale?) : Context {
    val cfg = Configuration(resources.configuration)
    cfg.setLocale(locale ?: Locale.getDefault())
    return createConfigurationContext(cfg)
}

fun VSH.getStringLocale(locale: Locale?, @StringRes resId: Int) : String {
    return createLocalizedContext(locale).getText(resId).toString()
}

fun VSH.setActiveLocale(locale:Locale?){
    val cfg = Configuration(resources.configuration)
    if(locale != null){
        cfg.setLocale(locale)
    }else{
        cfg.setLocale(Locale.getDefault())
    }
    resources.updateConfiguration(cfg, resources.displayMetrics)

    M.pref.set(PrefEntry.SYSTEM_LANGUAGE, createSerializedLocale(locale))
}

fun VSH.getStringLocale(locale:Locale, @StringRes resId:Int, vararg fmt:Any) : String {
    return String.format(createLocalizedContext(locale).getText(resId).toString(), fmt)
}