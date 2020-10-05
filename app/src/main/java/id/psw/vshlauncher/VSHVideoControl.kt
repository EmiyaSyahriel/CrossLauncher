package id.psw.vshlauncher

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.widget.VideoView
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.roundToInt

/**
 * TODO: document your custom view class.
 */
class VSHVideoControl : View {

    var activity : XMBVideoPlayer? = null

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

    private var isVisible = true
    private var bitmapPaint = Paint().apply {
        color = Color.WHITE
    }

    private var textPaint = TextPaint().apply {
        color = Color.WHITE
        textSize = 32f
    }
    private var opacity = 1.0f
    private var lastTime = 0L
    private var density = 1f
    private var scaledDensity = 1f
    private var refScale = 1f
    private val refScreenSize = PointF(848f,480f)
    private fun s(f:Float):Float = f*refScale
    private fun s(i:Int):Int = (i*refScale).roundToInt()
    private fun d(f:Float):Float = f*density
    private fun d(i:Int):Int = (i*density).roundToInt()
    private fun sd(f:Float):Float = f*scaledDensity
    private fun sd(i:Int):Int = (i*scaledDensity).roundToInt()
    private val controlPos = PointF(0.3f, 0.5f)
    private val controlSSPos = PointF(240f,240f)
    private val canvasSize = PointF(848f,480f)
    private lateinit var bitmap : Bitmap

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.VSHVideoControl, defStyle, 0
        )
        a.recycle()
        scaledDensity = resources.displayMetrics.scaledDensity
        density = resources.displayMetrics.density

        val texSize = CurrentAppData.textureLoadSize
        bitmap = resources.getDrawable(R.drawable.miptex_videoplayer).toBitmap(texSize,texSize, Bitmap.Config.ARGB_8888)
    }

    private var conPivotX = 240f
    private var conPivotY = 240f
    private var iconSize = 24
    private var iconOffset = 12

    private fun mUpdate(canvas:Canvas, deltaTime:Float){
        opacity = (deltaTime * 20).toLerp(opacity, if(isVisible) 1.0f else 0f)
        bitmapPaint.alpha = (opacity * 255).roundToInt()
        canvas.drawARGB((opacity*255*0.5f).toInt(), 0,0,0)
        updateScreenScale()
        drawControl(canvas)
    }

    private fun updateScreenScale(){
        refScale = if(width > height) width / refScreenSize.x else height / refScreenSize.y
        canvasSize.set(width.toFloat(), height.toFloat())
        conPivotX = width * 0.3f
        conPivotY = height * 0.5f
        iconSize = s(24)
        iconOffset = -(iconSize/2)
        textPaint.textSize = (iconSize / 2).toFloat()
        textPaint.textAlign = Paint.Align.CENTER
    }

    private fun getControlPosRect(x:Float, y:Float) : Rect{
        val left =(conPivotX + (x * iconSize) + (iconOffset)).toInt()
        val top = (conPivotY + (y * iconSize) + (iconOffset)).toInt()
        val right = left + iconSize
        val bottom = top + iconSize
        return Rect(
            left,
            top,
            right,
            bottom
        )
    }

    private fun drawControl(canvas:Canvas){
        // Upper Row
        canvas.drawSubImage(bitmap, 5,5,0,0,getControlPosRect(-4.5f,-1f), bitmapPaint, false) // Scene Select
        canvas.drawSubImage(bitmap, 5,5,1,0,getControlPosRect(-3.5f,-1f), bitmapPaint, false) // PBC
        canvas.drawSubImage(bitmap, 5,5,2,0,getControlPosRect(-2.5f,-1f), bitmapPaint, false) // Audio Channel
        canvas.drawSubImage(bitmap, 5,5,3,0,getControlPosRect(-1.5f,-1f), bitmapPaint) // Subtitle
        canvas.drawSubImage(bitmap, 5,5,4,0,getControlPosRect(-0.5f,-1f), bitmapPaint) // Volume
        canvas.drawSubImage(bitmap, 5,5,0,1,getControlPosRect( 0.5f,-1f), bitmapPaint) // Display Setting
        canvas.drawSubImage(bitmap, 5,5,1,1,getControlPosRect( 1.5f,-1f), bitmapPaint) // Aspect Ratio
        canvas.drawSubImage(bitmap, 5,5,2,1,getControlPosRect( 2.5f,-1f), bitmapPaint, false) // Set Thumbnail
        canvas.drawSubImage(bitmap, 5,5,3,1,getControlPosRect( 3.5f,-1f), bitmapPaint, true) // Delete
        canvas.drawSubImage(bitmap, 5,5,4,1,getControlPosRect( 4.5f,-1f), bitmapPaint) // Player Display Status

        // Middle Row
        canvas.drawSubImage(bitmap, 5,5,0,2,getControlPosRect(-6f,0f), bitmapPaint) // Prev
        canvas.drawSubImage(bitmap, 5,5,1,2,getControlPosRect(-5f,0f), bitmapPaint) // Next
        canvas.drawSubImage(bitmap, 5,5,2,2,getControlPosRect(-4f,0f), bitmapPaint) // FBwd
        canvas.drawSubImage(bitmap, 5,5,3,2,getControlPosRect(-3f,0f), bitmapPaint) // FFwd
        canvas.drawSubImage(bitmap, 5,5,4,2,getControlPosRect(-2f,0f), bitmapPaint) // Play
        canvas.drawSubImage(bitmap, 5,5,0,3,getControlPosRect(-1f,0f), bitmapPaint) // Pause
        canvas.drawSubImage(bitmap, 5,5,1,3,getControlPosRect(0f,0f), bitmapPaint) // Stop
        canvas.drawSubImage(bitmap, 5,5,2,3,getControlPosRect(1f,0f), bitmapPaint) // Start
        canvas.drawSubImage(bitmap, 5,5,3,3,getControlPosRect(2f,0f), bitmapPaint) // End
        canvas.drawSubImage(bitmap, 5,5,0,4,getControlPosRect(3f,0f), bitmapPaint) // Slow
        canvas.drawSubImage(bitmap, 5,5,1,4,getControlPosRect(4f,0f), bitmapPaint) // Fast
        canvas.drawSubImage(bitmap, 5,5,2,4,getControlPosRect(5f,0f), bitmapPaint) // Step Frame Backward
        canvas.drawSubImage(bitmap, 5,5,3,4,getControlPosRect(6f,0f), bitmapPaint) // Step Frame Forward

        // Lower Row
        canvas.drawSubImage(bitmap, 5,5,4,3,getControlPosRect(0f,1f), bitmapPaint) // Repeat Mode

        // Text
        val textPos = getControlPosRect(0f,2f)
        canvas.drawText("Pause", textPos.centerX().toFloat(), textPos.centerY().toFloat(), textPaint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if(lastTime == 0L) lastTime = System.currentTimeMillis()
        val ms = System.currentTimeMillis()
        val deltaTime =  ms - lastTime * 0.001f
        mUpdate(canvas, deltaTime)
        lastTime = ms
    }
}
