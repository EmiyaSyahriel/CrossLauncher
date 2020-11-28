package id.psw.vshlauncher.customtypes

import android.text.InputFilter
import android.text.Spanned
import java.util.*


class HexInputFilter : InputFilter {
    companion object{
        private val hexItems = "#0123456789ABCDEFabcdef"
    }

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence {

        val sb = StringBuilder()

        for (i in start until end) {
            val chr = (source?.get(i) ?: ' ').toUpperCase()
            val validHex = hexItems.contains(chr)
            if(validHex) sb.append(chr)
        }
        return sb.toString().toUpperCase(Locale.ROOT)
    }
}