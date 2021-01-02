package id.psw.vshlauncher.typography

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import id.psw.vshlauncher.drawText

data class MultifontText(val font:Typeface, val text:String)

class MultifontSpan : ArrayList<MultifontText>() {
    fun add(font: Typeface, text:String) : MultifontSpan{
        this.add(MultifontText(font, text))
        return this
    }
}

fun Canvas.drawText(span: MultifontSpan, x:Float, y:Float, baseline:Float, paint: Paint){
    val tempPaint = TextPaint(paint)
    var yOffset = y
    var xOffset = x
    val tempRect = Rect(0,0,0,0)
    span.forEach { richText ->
        tempPaint.typeface = richText.font
        tempPaint.getTextBounds(richText.text,0, richText.text.length, tempRect)
        if(richText.text.contains("\n")){
            richText.text.split('\n').forEach { lines ->
                drawText(lines, xOffset, yOffset, tempPaint, baseline)
                yOffset += tempRect.height()
                xOffset = x
            }
        }else{
            drawText(richText.text, xOffset, yOffset, tempPaint, baseline)
            xOffset += tempRect.width()
        }
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
}