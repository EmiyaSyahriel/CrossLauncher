package id.psw.vshlauncher

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.minus

class VshDialogView : View {

    data class Button(val text:String, val correspondingKeys:ArrayList<Int>, val runnable: Runnable)

    interface IDialogBackable{
        fun onDialogBack(){

        }
    }

    private var TAG = "VshDialogView.self"
    var density = 1f
    var scaledDensity = 1f
    var iconBitmap = ColorDrawable(Color.TRANSPARENT).toBitmap(1,1)
    var titleText = "Dialog"
    var contentText = "Content Text"
    var buttons = arrayListOf(
        Button("Close", arrayListOf(KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_BUTTON_B), Runnable{
        if(context is IDialogBackable) (context as IDialogBackable).onDialogBack()
    }),
        Button("Placeholder", arrayListOf(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_R), Runnable{

        })
    )
    private var touchSlop = 1

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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private var buttonRects = arrayListOf<RectF>()

    private fun Canvas.drawTextWithYOffset(text:String, x:Float, y:Float, paint:Paint, yOff : Float = 0.5f){
        val yCalc = y + (paint.textSize * yOff)
        this.drawText(text,x,yCalc,paint)
    }

    private fun mUpdate(canvas:Canvas){
        if(outlinePath.isEmpty) updatePath()
        recalculateButtonSize()
        canvas.drawPath(outlinePath, paintOutline)

        paintText.textAlign = Paint.Align.LEFT

        val leftPadding = d(50f)

        canvas.drawText(titleText, leftPadding,(height * 0.15f) - d(5f),paintText)

        canvas.save()
        paintText.textAlign = Paint.Align.CENTER
        buttonRects.forEachIndexed { index, rectF ->
            val btn = buttons[index]
            canvas.drawTextWithYOffset(btn.text, rectF.centerX(), rectF.centerY(), paintText)
            canvas.clipRect(rectF)
            canvas.drawColor(Color.argb(64,255,255,255))
            canvas.clipRect(0,0,width,height)
        }
        canvas.restore()

        canvas.save()
        paintText.textAlign = Paint.Align.CENTER
        canvas.clipPath(outlinePath)
        val texts = contentText.split("\t", "\n", "\\n")
        val textSize = paintText.textSize + d(5f)
        val yPadding = height/2 - ((texts.size * textSize) / 2f)
        texts.forEachIndexed{
            i, it ->
            canvas.drawTextWithYOffset(it, width/2f, yPadding + (i * textSize), paintText)
        }
        canvas.restore()
    }

    val touchStart = PointF(0f,0f)
    val touchEnd = PointF(0f,0f)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var retval = false
        when(event.actionMasked){
            MotionEvent.ACTION_DOWN -> {
                touchStart.set(event.x, event.y)
                retval = true
            }
            MotionEvent.ACTION_UP ->{
                touchEnd.set(event.x, event.y)
                if((touchStart - touchEnd).length() < touchSlop){
                    buttonRects.forEachIndexed { index, it ->
                        if(it.contains(touchEnd.x, touchEnd.y)){
                            buttons[index].runnable.run()
                        }
                    }
                }
            }
        }
        return retval || super.onTouchEvent(event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        buttons.forEach {
            if(it.correspondingKeys.any { key -> key == keyCode }){
                it.runnable.run()
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun recalculateButtonSize(){
        buttonRects.clear()
        val btnWidth = width / buttons.size
        val paddingSize = d(10f)
        buttons.forEachIndexed { index, _ ->
            buttonRects.add(RectF(
                (btnWidth * index * 1f) + paddingSize,
                (height * 0.85f) + paddingSize,
                (btnWidth * (index + 1f))- paddingSize,
                (height * 1f)- paddingSize
            ))
        }
    }

    fun setButton(newButtons : ArrayList<Button>){
        buttons = newButtons
    }

    override fun onDraw(canvas: Canvas) {
        mUpdate(canvas)
        super.onDraw(canvas)
    }
}