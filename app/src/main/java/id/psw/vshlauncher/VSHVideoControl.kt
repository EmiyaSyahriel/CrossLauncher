package id.psw.vshlauncher

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import kotlin.math.floor
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withClip
import java.io.File
import kotlin.math.roundToInt

/**
 * Video control panel for VSH
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

    var isVisible = true
    private var bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }

    private var highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 255
    }

    private var textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 32f
        setShadowLayer(10f, 0f,0f,Color.WHITE )
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
    private lateinit var iconBitmap : Bitmap
    private lateinit var highlightBitmap : Bitmap
    private lateinit var gradientBitmap : Bitmap
    private lateinit var progressBarBitmap : Bitmap
    private lateinit var formatBgBitmap : Bitmap
    private var selectedItem = Point(0,0)

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.VSHVideoControl, defStyle, 0
        )
        fitsSystemWindows = true

        a.recycle()
        scaledDensity = resources.displayMetrics.scaledDensity
        density = resources.displayMetrics.density

        val texSize = CurrentAppData.textureLoadSize
        iconBitmap = resources.getDrawable(R.drawable.miptex_videoplayer).toBitmap(texSize,texSize, Bitmap.Config.ARGB_8888)
        highlightBitmap = resources.getDrawable(R.drawable.miptex_videoplayer_highlight).toBitmap(texSize,texSize, Bitmap.Config.ARGB_8888)
        gradientBitmap = resources.getDrawable(R.drawable.t_videoplayer_gradient).toBitmap(texSize,texSize,Bitmap.Config.ARGB_8888)
        progressBarBitmap = resources.getDrawable(R.drawable.miptex_progressbar).toBitmap(texSize,texSize,Bitmap.Config.ARGB_8888)
        formatBgBitmap = resources.getDrawable(R.drawable.t_format_background).toBitmap(texSize,texSize,Bitmap.Config.ARGB_8888)
    }

    private var conPivotX = 240f
    private var conPivotY = 240f
    private var iconSize = 24
    private var iconOffset = 12
    private var currentTime = 0f

    private fun mUpdate(canvas:Canvas, deltaTime:Float){
        opacity = (deltaTime * 20).toLerp(opacity, if(isVisible) 1.0f else 0f)
        bitmapPaint.alpha = (opacity * 255).roundToInt()
        canvas.drawARGB((opacity*255*0.5f).toInt(), 0,0,0)

        selectedItem = activity?.selection ?: selectedItem

        updateScreenScale()
        drawControl(canvas)

        bitmapPaint.alpha = 255
        drawStatus(canvas)
        currentTime = (deltaTime + currentTime) % 6000f
        val alphaPlacement= currentTime.pingpong(3.0f, 1f)

        highlightPaint.alpha = (alphaPlacement * 255).toInt()
        textPaint.setShadowLayer(alphaPlacement * 10.0f, 0f,0f, Color.WHITE)
    }

    private fun updateScreenScale(){
        val isLandscape = width > height

        refScale = if(isLandscape) width / refScreenSize.x else height / refScreenSize.y
        canvasSize.set(width.toFloat(), height.toFloat())
        val xPivot = if(isLandscape) 0.3f else 0.5f
        conPivotX = width * xPivot
        conPivotY = height * 0.5f
        iconSize = if(isLandscape) s(32) else s(20)
        iconOffset = -(iconSize/2)

        if(iconSize * 14 > width){ // lets make them fit, at least
            iconSize = width / 14
        }

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

    private fun getVideoCurrentTime():Float{
        var retval = 0f
        if(activity != null){
            val act = activity!!
            retval = act.vp.currentPosition.toFloat() / act.vp.duration
        }
        return retval
    }

    private fun formatAsTime(timeInMS:Int):String{
        val asSecond = timeInMS / 1000
        val s = asSecond % 60
        val m = floor(asSecond / 60f).toInt()
        val h = floor(asSecond / 3600f).toInt()
        val strS = s.mp00()
        val strM = m.mp00()
        val strH = h.mp00()
        return "$strH:$strM:$strS"
    }

    private fun getVideoCurrentTimeStr():String{
        var currentTime = 0

        if(activity != null){
            val act = activity!!
            currentTime = act.vp.currentPosition
        }
        return formatAsTime(currentTime)
    }

    private fun getVideoDurationStr():String{
        var duration = 0

        if(activity != null){
            val act = activity!!
            duration = act.vp.duration
        }
        return formatAsTime(duration)
    }

    private fun drawStatus(canvas:Canvas){
        val lerpTime = getVideoCurrentTime()
        val lastAlpha = bitmapPaint.alpha
        val isLandscape = width > height
        val bgBarHeight = if(isLandscape) s(50) else s(30)

        // draw background
        // top
        canvas.drawSubImage(gradientBitmap, 1,2,0,0,0,0,width,bgBarHeight,bitmapPaint)
        // bottom
        canvas.drawSubImage(gradientBitmap, 1,2,0,1,0,height - bgBarHeight,width,bgBarHeight,bitmapPaint)

        val oriTextSize = textPaint.textSize

        var filePath = CurrentAppData.selectedVideoPath
        if(isInEditMode) filePath = "Editor Preview.mp4"
        val fileName = filePath.asPathGetFileName()
        val fileFormat = filePath.asPathGetFileExtension()
        val progress = ((activity?.vp?.currentPosition ?: 0 ) * 1f) / ((activity?.vp?.duration ?: 1) * 1f)

        // top status
        var yPos = bgBarHeight/2f
        textPaint.textSize = if(isLandscape) s(15f) else s(10f)
        textPaint.textAlign = Paint.Align.LEFT
        canvas.withClip(Rect(0,0,(width * 0.7).toInt(),bgBarHeight)){
            canvas.drawText(fileName, bgBarHeight * ( if(isLandscape) 1f else 0.25f), bgBarHeight/2f, textPaint)
        }
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawFormat(formatBgBitmap, fileFormat, Point((width * 0.75f).toInt(), bgBarHeight/2),refScale,bitmapPaint,textPaint)



        // bottom status
        textPaint.textAlign = Paint.Align.RIGHT
        val durText = getVideoDurationStr()
        val durOffset = if(isLandscape) width - bgBarHeight * 1f else width - (bgBarHeight * 0.25f)
        textPaint.getTextBounds(durText,0, durText.length, Temp.tempRect)
        val barOffset = if(isLandscape) {durOffset - Temp.tempRect.width() - textPaint.textSize} else width - (bgBarHeight * 0.25f)
        val barSize = if(isLandscape) s(200f) else width - (bgBarHeight * 0.5f)
        val barHeight = s(4f)
        yPos = height - (bgBarHeight/2f)
        val curOffset = if(isLandscape) barOffset - barSize - textPaint.textSize else bgBarHeight * 0.25f
        val textYOffset = if(isLandscape) -0.25f else 1f
        canvas.drawText(getVideoDurationStr(), durOffset, yPos, textPaint, textYOffset)
        if(!isLandscape) textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText(getVideoCurrentTimeStr(), curOffset, yPos, textPaint, textYOffset)

        // draw progress bar
        Temp.tempRectF.set(barOffset - barSize,yPos - barHeight, barOffset, yPos + barHeight)
        canvas.drawProgressBar34Sprite(progressBarBitmap, Temp.tempRectF, progress, bitmapPaint)

        textPaint.textSize = oriTextSize
    }

    private fun sel(x:Int,y:Int):Boolean{
        return x == selectedItem.x && y == selectedItem.y
    }

    private fun enbl(x:Int, y:Int):Boolean{
        var retval = false
        if(activity != null){
            val act = activity!!
            if(act.selectionControl.containsKey(Point(x,y))){
                retval = act.selectionControl[Point(x,y)]?.enabled ?: false
            }
        }
        return retval
    }

    private fun getSelectedObjectName() : String{
        var retval = ""
        if(activity!=null){
            val act = activity!!
            if(act.selectionControl.containsKey(selectedItem)){
                retval = context.getString(act.selectionControl[selectedItem]?.nameStrId ?: android.R.string.VideoView_error_button)
            }
        }

        return retval
    }

    private fun drawControl(canvas:Canvas){
        // Upper Row
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,0,0,getControlPosRect(-4.5f,-1f), bitmapPaint, highlightPaint, sel(-4,-1), enbl(-4,-1)) // Scene Select
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,1,0,getControlPosRect(-3.5f,-1f), bitmapPaint, highlightPaint, sel(-3,-1), enbl(-3,-1)) // PBC
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,2,0,getControlPosRect(-2.5f,-1f), bitmapPaint, highlightPaint, sel(-2,-1), enbl(-2,-1)) // Audio Channel
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,3,0,getControlPosRect(-1.5f,-1f), bitmapPaint, highlightPaint, sel(-1,-1), enbl(-1, -1)) // Subtitle
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,4,0,getControlPosRect(-0.5f,-1f), bitmapPaint, highlightPaint, sel( 0,-1), enbl(0, -1)) // Volume
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,0,1,getControlPosRect( 0.5f,-1f), bitmapPaint, highlightPaint, sel( 1,-1), enbl(1, -1)) // Display Setting
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,1,1,getControlPosRect( 1.5f,-1f), bitmapPaint, highlightPaint, sel( 2,-1), enbl(2, -1)) // Aspect Ratio
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,2,1,getControlPosRect( 2.5f,-1f), bitmapPaint, highlightPaint, sel( 3,-1), enbl(3, -1)) // Set Thumbnail
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,3,1,getControlPosRect( 3.5f,-1f), bitmapPaint, highlightPaint, sel( 4,-1), enbl(4, -1)) // Delete
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,4,1,getControlPosRect( 4.5f,-1f), bitmapPaint, highlightPaint, sel( 5,-1), enbl(5, -1)) // Player Display Status

        // Middle Row
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,0,2,getControlPosRect(-6f,0f), bitmapPaint, highlightPaint, sel(-6,0), enbl(-6,0)) // Prev
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,1,2,getControlPosRect(-5f,0f), bitmapPaint, highlightPaint, sel(-5,0), enbl(-5,0)) // Next
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,2,2,getControlPosRect(-4f,0f), bitmapPaint, highlightPaint, sel(-4,0), enbl(-4,0)) // FBwd
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,3,2,getControlPosRect(-3f,0f), bitmapPaint, highlightPaint, sel(-3,0), enbl(-3,0)) // FFwd
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,4,2,getControlPosRect(-2f,0f), bitmapPaint, highlightPaint, sel(-2,0), enbl(-2,0)) // Play
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,0,3,getControlPosRect(-1f,0f), bitmapPaint, highlightPaint, sel(-1,0), enbl(-1,0)) // Pause
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,1,3,getControlPosRect( 0f,0f), bitmapPaint, highlightPaint, sel( 0,0), enbl( 0,0)) // Stop
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,2,3,getControlPosRect( 1f,0f), bitmapPaint, highlightPaint, sel( 1,0), enbl( 1,0)) // Start
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,3,3,getControlPosRect( 2f,0f), bitmapPaint, highlightPaint, sel( 2,0), enbl( 2,0)) // End
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,0,4,getControlPosRect( 3f,0f), bitmapPaint, highlightPaint, sel( 3,0), enbl( 3,0)) // Slow
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,1,4,getControlPosRect( 4f,0f), bitmapPaint, highlightPaint, sel( 4,0), enbl( 4,0)) // Fast
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,2,4,getControlPosRect( 5f,0f), bitmapPaint, highlightPaint, sel( 5,0), enbl( 5,0)) // Step Frame Backward
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,3,4,getControlPosRect( 6f,0f), bitmapPaint, highlightPaint, sel( 6,0), enbl( 6,0)) // Step Frame Forward

        // Lower Row
        canvas.drawSubImage(iconBitmap, highlightBitmap, 5,5,4,3,getControlPosRect( 0f,1f), bitmapPaint, highlightPaint, sel( 0,1), enbl( 0, 1)) // Repeat Mode

        // Text
        val textPos = getControlPosRect(0f,2f)
        canvas.drawText(getSelectedObjectName(), textPos.centerX().toFloat(), textPos.centerY().toFloat(), textPaint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if(lastTime == 0L) lastTime = System.currentTimeMillis()
        val ms = System.currentTimeMillis()
        val deltaTime =  (ms - lastTime) * 0.001f
        mUpdate(canvas, deltaTime)
        lastTime = ms
        postInvalidate()
    }
}
