package id.psw.vshlauncher.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import id.psw.vshlauncher.R

/**
 * TODO: document your custom view class.
 */
class VshServerTestView : View {

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
    }

    init {
    }

    override fun onDraw(canvas: Canvas?) {
        if(canvas != null){
            VshServer.draw(canvas)
        }
        postInvalidate()
    }

}