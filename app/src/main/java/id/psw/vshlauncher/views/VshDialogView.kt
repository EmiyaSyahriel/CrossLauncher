package id.psw.vshlauncher.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.*
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.minus
import androidx.core.view.*
import id.psw.vshlauncher.R
import id.psw.vshlauncher.choose
import id.psw.vshlauncher.getSystemPadding
import id.psw.vshlauncher.toLerp

class VshDialogView : View {

    data class Button(val text:String, val runnable: Runnable)

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
        Button("Close", Runnable{ if(context is IDialogBackable) (context as IDialogBackable).onDialogBack() }),
        Button("Placeholder", Runnable{} )
    )

    private var touchSlop = 1
    private var backgroundAlpha = 0
    private var renderableArea = Rect(0,0,0,0)

    private var paintText = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var paintFill = Paint(Paint.ANTI_ALIAS_FLAG)
    private var paintButtonUnselected = Paint(Paint.ANTI_ALIAS_FLAG)
    private var paintOutline = Paint(Paint.ANTI_ALIAS_FLAG)
    private var paintButton = Paint(Paint.ANTI_ALIAS_FLAG)
    private var availableWindowSize = Rect(0,0,0,0)
    private var debugPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(50,255,0,0)
    }
    private var selectedButtonIndex = 0
    private var confirmButton = KeyEvent.KEYCODE_A

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
            alpha = 255
        }
        paintFill.apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        paintButtonUnselected.apply {
            color = Color.argb(32,255,255,255)
            style = Paint.Style.FILL
        }
        paintButton.apply {
            color = Color.argb(64,255,255,255)
            style = Paint.Style.FILL
        }
    }

    private fun init(attrs: AttributeSet?, defStyle: Int){
        renderableArea = getSystemPadding()
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.VshDialogView, defStyle, 0
        )
        val prefs = context.getSharedPreferences("xRegistry.sys", Context.MODE_PRIVATE)
        confirmButton = prefs.getBoolean("xmb_PS3_FAT_CECHA00", false).choose(KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_BUTTON_B)

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
        val w = renderableArea.width()
        val h = renderableArea.bottom
        outlinePath.moveTo(-w * 0.25f, h * 0.20f)
        outlinePath.lineTo(w * 1.25f,  h * 0.20f)
        outlinePath.lineTo(w * 1.25f, h * 0.85f)
        outlinePath.lineTo( -w * 0.25f, h * 0.85f)
        outlinePath.close()

        outlineRect.set(
            -w * 0.25f, h * 0.20f,
            w * 1.25f, h * 0.85f
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        renderableArea = getSystemPadding()
        updatePath()
        Log.d(TAG, "Screen Size : $marginLeft $marginTop - $marginRight $marginBottom")
    }

    private var buttonRects = arrayListOf<RectF>()

    private fun Canvas.drawTextWithYOffset(text:String, x:Float, y:Float, paint:Paint, yOff : Float = 0.5f){
        val yCalc = y + (paint.textSize * yOff)
        this.drawText(text,x,yCalc,paint)
    }

    private fun mUpdate(canvas:Canvas){
        if(outlinePath.isEmpty) updatePath()
        recalculateButtonSize()

        backgroundAlpha = (backgroundAlpha + 5).coerceIn(0, 128)
        canvas.drawARGB(backgroundAlpha, 0,0,0)

        val height = renderableArea.bottom

        canvas.drawPath(outlinePath, paintOutline)

        paintText.textAlign = Paint.Align.LEFT

        val leftPadding = d(50f) + renderableArea.left

        canvas.drawText(titleText, leftPadding,(height * 0.20f) - d(20f),paintText)
        canvas.drawBitmap(iconBitmap, leftPadding - d(40f), (height * 0.20f) -d(10f) - d(32f), paintFill)

        paintText.textAlign = Paint.Align.CENTER
        buttonRects.forEachIndexed { index, rectF ->
            val btn = buttons[index]
            canvas.drawRect(rectF, (index == selectedButtonIndex).choose(paintButton, paintButtonUnselected))
            canvas.drawTextWithYOffset(btn.text, rectF.centerX(), rectF.centerY(), paintText )
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
                    val delta = d(touchEnd.y - touchStart.y) / (contentText.split("\n").size * paintText.textSize)
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
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                selectedButtonIndex = (selectedButtonIndex - 1).coerceIn(0, buttons.size - 1)
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                selectedButtonIndex = (selectedButtonIndex + 1).coerceIn(0, buttons.size - 1)
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, confirmButton -> {
                buttons[selectedButtonIndex].runnable.run()
            }
        }

        return retval || super.onKeyUp(keyCode, event)
    }

    private fun recalculateButtonSize(){
        buttonRects.clear()
        val btnWidth = renderableArea.width() / buttons.size
        val renderHeight = renderableArea.height()
        val paddingSize = d(3f)
        buttons.forEachIndexed { index, _ ->
            buttonRects.add(RectF(
                (btnWidth * index.toFloat()) + paddingSize + renderableArea.left,
                (renderHeight * 0.85f) + paddingSize + renderableArea.top,
                (btnWidth * (index + 1f)) - paddingSize + renderableArea.left,
                (renderHeight + renderableArea.top * 1f) - paddingSize
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