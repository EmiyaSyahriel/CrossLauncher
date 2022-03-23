package id.psw.vshlauncher.typography

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import id.psw.vshlauncher.views.drawText
import kotlin.collections.ArrayList

data class MultifontText(val font:Typeface, val text:String)

class MultifontSpan : ArrayList<MultifontText>() {
    fun add(font: Typeface, text:String) : MultifontSpan{
        this.add(MultifontText(font, text))
        return this
    }
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