package id.psw.vshlauncher

import android.app.Dialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.ColorDrawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.drawable.toBitmap

class VshDialogView : View {


    private var TAG = "VshDialogView.self"
    var density = 1f
    var scaledDensity = 1f
    var iconBitmap = ColorDrawable(Color.TRANSPARENT).toBitmap(1,1)
    var titleText = "Dialog"
    var contentText = "Content Text"

    private var paintText = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var paintFill = Paint(Paint.ANTI_ALIAS_FLAG)
    private var paintOutline = Paint(Paint.ANTI_ALIAS_FLAG)

    fun d(i:Float):Float{return i * density}
    fun d(i:Int):Int{return (i * density).toInt()}
    fun sd(i:Float):Float{return i * density}
    fun sd(i:Int):Int{return (i * scaledDensity).toInt()}

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }


    private fun generatePaint(){
        paintText.apply {
            color = Color.WHITE
            textSize = sd(15f)
            textAlign = Paint.Align.LEFT
        }
        paintOutline.apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = d(1.5f)
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        paintFill.apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
    }

    private fun init(attrs: AttributeSet?, defStyle: Int){
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.VshDialogView, defStyle, 0
        )
        a.recycle()

        density = resources.displayMetrics.density
        scaledDensity = resources.displayMetrics.scaledDensity
        generatePaint()
    }

    var outlinePath = Path()
    private fun updatePath(){
        outlinePath.reset()
        outlinePath.moveTo(-width * 0.25f, height * 0.15f )
        outlinePath.lineTo(width * 1.25f, height * 0.15f)
        outlinePath.lineTo(width * 1.25f, height * 0.85f)
        outlinePath.lineTo( -width * 0.25f, height * 0.85f)
        outlinePath.close()
    }

    private fun mUpdate(canvas:Canvas){
        if(outlinePath.isEmpty) updatePath()
        canvas.drawPath(outlinePath, paintOutline)

        paintText.textAlign = Paint.Align.LEFT

        val leftPadding = ((1280f) / (width / density)) * width

        canvas.drawText(titleText, leftPadding,(height * 0.15f) - d(5f),paintText)
        paintText.textAlign = Paint.Align.CENTER
        canvas.drawText(contentText, width/2f, height/2f, paintText)
    }


    override fun onDraw(canvas: Canvas) {
        mUpdate(canvas)
        super.onDraw(canvas)
    }
}