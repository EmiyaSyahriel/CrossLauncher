package id.psw.vshlauncher.views.dialogviews

import android.graphics.*
import android.text.TextPaint
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.select
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.views.drawText

class TextDialogView(vsh: VSH) : XmbDialogSubview(vsh) {
    override var hasNegativeButton: Boolean = true
    override var hasPositiveButton: Boolean = true
    private val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
        textSize = 25.0f
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }

    var onPositiveButton : ((TextDialogView) -> Unit)? = null
    var onNegativeButton : ((TextDialogView) -> Unit)? = null

    override var icon: Bitmap = XMBItem.TRANSPARENT_BITMAP
    override var title: String = "Dialog"

    var content = ""
    var textSpacing = 1.25f

    override var negativeButton: String = ""
    override var positiveButton: String = ""

    override fun onDraw(ctx: Canvas, drawBound: RectF) {
        textPaint.textSize = isPSP.select(30.0f, 25.0f)
        val lines = content.lines()
        var y = -(lines.size * 0.5f) * (textPaint.textSize * textSpacing)
        lines.forEach {
            ctx.drawText(it, drawBound.centerX(), drawBound.centerY() + y, textPaint, 0.5f)
            y += textPaint.textSize * textSpacing
        }
    }

    override fun onDialogButton(isPositive: Boolean) {
        if(isPositive){
            onPositiveButton?.invoke(this)
        }else{
            onNegativeButton?.invoke(this)
        }
    }

    fun setNegative(text:String, func:((TextDialogView) -> Unit)?) : TextDialogView{
        hasNegativeButton = func != null
        negativeButton = text
        onNegativeButton = func
        return this
    }
    fun setPositive(text:String, func:((TextDialogView) -> Unit)?) : TextDialogView{
        hasPositiveButton = func != null
        positiveButton = text
        onPositiveButton = func
        return this
    }
    fun setData(icon:Bitmap?, title:String, content:String): TextDialogView {
        this.icon = icon?: XMBItem.TRANSPARENT_BITMAP
        this.title = title
        this.content = content
        return this
    }
    fun setLineSpacing(space:Float) : TextDialogView{
        textSpacing = space
        return this
    }
}