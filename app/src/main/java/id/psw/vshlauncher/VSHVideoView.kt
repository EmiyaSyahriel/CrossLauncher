package id.psw.vshlauncher

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.VideoView

class VSHVideoView : VideoView {

    constructor(context: Context): super(context){ }
    constructor(context: Context,attributeSet: AttributeSet): super(context, attributeSet){ }
    constructor(context: Context, attributeSet: AttributeSet, defStyle:Int):super(context, attributeSet, defStyle){}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
    }
}