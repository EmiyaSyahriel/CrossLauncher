package id.psw.vshlauncher.typography

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import androidx.annotation.StringRes
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.submodules.PadKey
import id.psw.vshlauncher.views.drawText
import kotlin.collections.ArrayList

data class MultifontText(val font:Typeface, val text:String)

fun String.parseEncapsulatedBracket() : ArrayList<String>{
    val retval = arrayListOf<String>()
    val src = this
    var nowStart = 0
    var nowEnd = 0
    while(nowStart < src.length && nowEnd < src.length){
        nowStart = src.indexOf("{", nowEnd)

        try{
            if(nowStart == -1){ // Last Item
                retval.add(src.substring(nowEnd))
                break
            }else{
                retval.add(src.substring(nowEnd, nowStart))
                if (src[nowStart + 1] != '{') {
                    nowEnd = src.indexOf("}", nowStart)
                    if (nowEnd == -1) nowEnd = src.length - 1
                    val codeName = src.substring(nowStart + 1, nowEnd)
                    retval.add(codeName)
                    nowEnd += 1
                } else {
                    retval.add("{")
                    nowEnd += 2;
                }
            }
        }catch (e:StringIndexOutOfBoundsException){
            e.printStackTrace()
            break
        }
    }

    return retval
}


class MultifontSpan : ArrayList<MultifontText>() {
    companion object {
        /**
         * Substituting names into console buttons
         * Names were encapsulated in braces (`{` & `}`), case and space-sensitive, `{{` will yield
         * single open brace and wouldn't need close bracket, and will change based on user's
         * selected console button display
         *
         * Codes :
         * (Format: `code` = PS / Xbox / NSw )
         * - `cross` = Cross / A / B
         * - `circle` = Circle / B / A
         * - `square` = Square / Y / X
         * - `triangle` = Triangle / X / Y
         * - `confirm` = Confirm Button (User-defined)
         * - `cancel` = Cancel Button (User-defined)
         * - `up`, `u` = D-Pad Up
         * - `left`, `d` = D-Pad Left
         * - `right`, `l` = D-Pad Right
         * - `down`, `r` = D-Pad Down
         * - `start` = Start / Menu / Plus
         * - `select` = Select / Select / Minus
         * - `l1` = L1 / LB / L
         * - `l2` = L2 / LT / ZL
         * - `l3` = L3 / LS-Click / LS-Click
         * - `r1` = R1 / RB / R
         * - `r2` = R2 / RT / ZR
         * - `r3` = R3 / RS-Click / RS-Click
         * - `ps` = PS Button / Xbox Button / Home Button
         * - **OTHER** = Skipped
        */
        fun createButtonMixedSpan(vsh:Vsh, src:String) : MultifontSpan {
            val retval = MultifontSpan()
            src.parseEncapsulatedBracket().forEachIndexed { i, s ->
                if(i % 2 == 0){
                    retval.add(FontCollections.masterFont, s)
                }else{
                    val keyCode = when(s){
                        "cross" -> PadKey.Cross
                        "circle" -> PadKey.Circle
                        "square" -> PadKey.Square
                        "triangle" -> PadKey.Triangle
                        "cancel" -> PadKey.Cancel
                        "confirm" -> PadKey.Confirm
                        "u", "up" -> PadKey.PadU
                        "d", "down" -> PadKey.PadD
                        "l", "left" -> PadKey.PadL
                        "r", "right" -> PadKey.PadR
                        "start" -> PadKey.Start
                        "select" -> PadKey.Select
                        "ps" -> PadKey.PS
                        "l1" -> PadKey.L1
                        "l2" -> PadKey.L2
                        "l3" -> PadKey.L3
                        "r1" -> PadKey.R1
                        "r2" -> PadKey.R2
                        "r3" -> PadKey.R3
                        else -> PadKey.None
                    }
                    if(keyCode != PadKey.None){
                        retval.add(vsh, keyCode)
                    }
                }
            }
            return retval
        }
    }

    fun add(font: Typeface, text:String) : MultifontSpan{
        this.add(MultifontText(font, text))
        return this
    }
    fun add(vsh: Vsh, k:PadKey) : MultifontSpan{
        this.add(MultifontText(FontCollections.buttonFont, vsh.M.gamepadUi.getGamepadChar(k).toString()))
        return this
    }
}

fun String.toButtonSpan(vsh:Vsh) : MultifontSpan {
    return MultifontSpan.createButtonMixedSpan(vsh, this)
}

fun Vsh.getButtonedString(@StringRes strId : Int) : MultifontSpan {
    return MultifontSpan.createButtonMixedSpan(this, this.getString(strId))
}

fun Vsh.getButtonedString(@StringRes strId : Int, vararg fmt:Any) : MultifontSpan {
    return MultifontSpan.createButtonMixedSpan(this, this.getString(strId, *fmt))
}



fun Canvas.drawText(span: MultifontSpan, x:Float, y:Float, baseline:Float, paint: Paint){
    val tempPaint = TextPaint(paint)
    var totalWidth = 0.0f
    span.forEach {
        tempPaint.typeface = it.font
        totalWidth += tempPaint.measureText(it.text)
    }

    var xOffset = when(paint.textAlign){
        Paint.Align.LEFT -> x
        Paint.Align.RIGHT -> x - totalWidth
        Paint.Align.CENTER -> x - (totalWidth/2.0f)
        else -> x
    }
    tempPaint.textAlign = Paint.Align.LEFT
    span.forEach { richText ->
        tempPaint.typeface = richText.font
        drawText(richText.text, xOffset, y, tempPaint, baseline)
        xOffset += tempPaint.measureText(richText.text)
    }
}

fun Canvas.getTextBound(span: MultifontSpan, paint: Paint, bounds:Rect){
    val tempPaint = TextPaint(paint)
    val tempRect = Rect(0,0,0,0)
    bounds.set(0,0,0,0)
    span.forEach { richText ->
        tempPaint.typeface = richText.font
        tempPaint.getTextBounds(richText.text,0, richText.text.length, tempRect)
        if(richText.text.contains("\n")){
            richText.text.split('\n').forEach { _ ->
                bounds.bottom += tempRect.height()
            }
        }else{
            bounds.right += tempRect.width()
        }
    }
    bounds.bottom += tempRect.height()
}
fun String.applyFontMacro(type:ButtonType, swapConfirm: Boolean) : String
{
    var retval = this
    (if(swapConfirm) swapConfirmFontMacro else confirmFontMacros)
        .entries.forEach {
            if(it.key.btnType == type){
                retval = retval.replace(it.key.macro, it.value)
            }
        }
    fontMacros.entries.forEach {
        if(it.key.btnType == type){
            retval = retval.replace(it.key.macro, it.value)
        }
    }
    return retval
}