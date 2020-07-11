package id.psw.vshlauncher

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
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
    var scrollPadding = 0f
    var buttons = arrayListOf(
        Button("Close", arrayListOf(KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_BUTTON_B), Runnable{
        if(context is IDialogBackable) (context as IDialogBackable).onDialogBack()
    }),
        Button("Placeholder", arrayListOf(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_R), Runnable{
        })
    )
    private var touchSlop = 1
    private var backgroundAlpha = 0

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
        isFocusable = true
        density = resources.displayMetrics.density
        scaledDensity = resources.displayMetrics.scaledDensity
        generatePaint()
    }

    var outlinePath = Path()
    var outlineRect = RectF(0f,0f,0f,0f)
    private fun updatePath(){
        outlinePath.reset()
        outlinePath.moveTo(-width * 0.25f, height * 0.15f )
        outlinePath.lineTo(width * 1.25f, height * 0.15f)
        outlinePath.lineTo(width * 1.25f, height * 0.85f)
        outlinePath.lineTo( -width * 0.25f, height * 0.85f)
        outlinePath.close()

        outlineRect.set(
            -width * 0.25f, height * 0.15f,
            width * 1.25f, height * 0.85f
        )
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

        backgroundAlpha = (backgroundAlpha + 5).coerceIn(0, 128)

        canvas.drawARGB(backgroundAlpha, 0,0,0)

        paintText.textAlign = Paint.Align.LEFT

        val leftPadding = d(50f)

        canvas.drawText(titleText, leftPadding,(height * 0.15f) - d(20f),paintText)
        canvas.drawBitmap(iconBitmap, leftPadding - d(40f), (height * 0.15f) -d(10f) - d(32f), paintFill)

        paintText.textAlign = Paint.Align.CENTER
        buttonRects.forEachIndexed { index, rectF ->
            val btn = buttons[index]
            canvas.drawTextWithYOffset(btn.text, rectF.centerX(), rectF.centerY(), paintText)
            canvas.save()
            canvas.clipRect(rectF)
            canvas.drawARGB(64,255,255,255)
            canvas.clipRect(0,0,width,height)
            canvas.restore()
        }

        canvas.save()
        paintText.textAlign = Paint.Align.CENTER
        canvas.clipPath(outlinePath)
        val texts = contentText.split("\t", "\n", "\\n")
        val textSize = paintText.textSize + sd(5f)
        val isInBound = outlineRect.height() > textSize * texts.size
        val yPadding = isInBound
            .choose(0.5f, scrollPadding)
            .toLerp(
                outlineRect.top + 0f + paintText.textSize,
                outlineRect.top - ((textSize * texts.size) - outlineRect.height())
            )
        texts.forEachIndexed{
            i, it ->
            canvas.drawTextWithYOffset(it, width/2f, yPadding + (i * textSize), paintText)
        }
        canvas.restore()
        postInvalidate()
    }

    val touchStart = PointF(0f,0f)
    val touchEnd = PointF(0f,0f)
    var touchStartScrollPos = 0f
    val textContentSize = Rect(0,0,0,0)

    private val isTouch: Boolean
        get() {
            return ((touchStart - touchEnd).length() < touchSlop)
        }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var retval = false
        touchEnd.set(event.x, event.y)
        when(event.actionMasked){
            MotionEvent.ACTION_DOWN -> {
                touchStart.set(event.x, event.y)
                touchStartScrollPos = scrollPadding
                retval = true
            }
            MotionEvent.ACTION_UP ->{
                if(isTouch){
                    buttonRects.forEachIndexed { index, it ->
                        if(it.contains(touchEnd.x, touchEnd.y)){
                            buttons[index].runnable.run()
                        }
                    }
                }
                retval = true
            }
            MotionEvent.ACTION_MOVE ->{
                if(!isTouch){
                    val delta = d(touchEnd.y - touchStart.y) / outlineRect.height()
                    scrollPadding = (touchStartScrollPos - delta).coerceIn(0f, 1f)
                    postInvalidate()
                }
                retval = true
            }
        }
        return retval || super.onTouchEvent(event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        var retval = false
        buttons.forEach {
            if(it.correspondingKeys.any { key -> key == keyCode }){
                it.runnable.run()
                retval = true
                postInvalidate()
            }
        }

        paintText.getTextBounds("A", 0, 1, textContentSize)
        val spp = textContentSize.height() / outlineRect.height()

        when(keyCode){
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                scrollPadding = (scrollPadding + spp).coerceIn(0f,1f)
                retval = true
                postInvalidate()
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                scrollPadding = (scrollPadding - spp).coerceIn(0f,1f)
                retval = true
                postInvalidate()
            }
        }

        return retval || super.onKeyUp(keyCode, event)
    }

    private fun recalculateButtonSize(){
        buttonRects.clear()
        val btnWidth = width / buttons.size
        val paddingSize = d(3f)
        buttons.forEachIndexed { index, _ ->
            buttonRects.add(RectF(
                (btnWidth * index.toFloat()) + paddingSize,
                (height * 0.85f) + paddingSize,
                (btnWidth * (index + 1f)) - paddingSize,
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