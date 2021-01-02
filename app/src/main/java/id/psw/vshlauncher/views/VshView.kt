package id.psw.vshlauncher.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRectF
import id.psw.vshlauncher.*
import id.psw.vshlauncher.customtypes.XMBStack
import id.psw.vshlauncher.typography.*
import java.io.File
import java.lang.Exception
import java.lang.Math.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.ConcurrentModificationException
import kotlin.collections.ArrayList
import kotlin.math.roundToInt


/**
 * The XMB Screen (PS3 Home Screen)
 */
class VshView : View {

    companion object {
        const val Deg2Rad = PI / 180f
        const val Rad2Deg = 180f / PI
        var padding = RectF(0f,0f,0f,0f)
        private var padOffset = RectF(0f,0f,0f,0f)
        private var transparentBitmap = ColorDrawable(Color.argb(0,0,0,0)).toBitmap(1,1)
        var showFPSMeter = false
        // Debug only purpose
        var launchTapArea = RectF(0f,0f,0f,0f)
        // to launch option right in it's location
        var optionLaunchArea = RectF(0f,0f,0f,0f)
        var customTypeface = Typeface.SANS_SERIF
        var deltaTime = 0.016f
        var hideClock = false
        var descriptionSeparator = false
        var menuBackgroundColor = Color.argb(32,0,0,0)
        val alphaColor = Color.argb(0,0,0,0)
    }

    /// region Variable
    private var TAG = "VshView.cpp"
    private var frame = 0f
    private var frameStart = 0L
    private var paintIconSelected = Paint(Paint.ANTI_ALIAS_FLAG)
    private var paintMenuTextSelected = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var paintTextSelected = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var paintSubtextSelected = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private var paintIconUnselected = Paint(Paint.ANTI_ALIAS_FLAG)
    private var paintTextUnselected = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var paintSubtextUnselected = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private var paintStatusBoxOutline = Paint(Paint.ANTI_ALIAS_FLAG)
    private var paintMisc = Paint(Paint.ANTI_ALIAS_FLAG)
    private var paintFill = Paint(Paint.ANTI_ALIAS_FLAG)
    private var paintStatusBoxFill = Paint(Paint.ANTI_ALIAS_FLAG)
    private var paintStatusText = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var debugPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(64,0,255,0) }
    private var renderableArea = Rect(0,0,0,0)

    var selectedXf = 0f
    var selectedYf = 0f
    var selectedX = 0
    var selectedY = 0
    var menuIndex = 0
    var lastInteractionTime = 0L

    var category : ArrayList<VshX> = arrayListOf()
    var hideMenu = false
    var backgroundAlpha = 1f
    var usePspStyle = true

    var density = 1f
    var scaledDensity = 1f

    var subcontentStack = XMBStack<VshY>()

    /// endregion Variable

    fun d(i:Float):Float{return i * density}
    fun d(i:Int):Int{return (i * density).toInt()}
    fun sd(i:Float):Float{return i * density}
    fun sd(i:Int):Int{return (i * scaledDensity).toInt()}

    private fun generatePaint(){
        paintTextSelected = TextPaint(Paint.ANTI_ALIAS_FLAG).apply{
            color = Color.WHITE
            textSize = sd(18f)
            textAlign = Paint.Align.LEFT
            setShadowLayer(sd(5f), 0f,0f, Color.argb(255,255,255,255))
        }
        paintTextUnselected = TextPaint(paintTextSelected).apply {
            alpha = 128
            textSize = sd(15f)
            setShadowLayer(sd(2f), 0f,0f, Color.argb(128,0,0,0))
        }
        paintSubtextSelected = TextPaint(paintTextSelected).apply{
            textSize = sd(12f)
        }
        paintSubtextUnselected = TextPaint(paintTextSelected).apply{
            alpha = 128
            textSize = sd(10f)
            setShadowLayer(sd(2f), 0f,0f, Color.argb(128,0,0,0))
        }
        paintMenuTextSelected = TextPaint(paintTextSelected).apply {
            textSize = sd(15f)
            textAlign = Paint.Align.CENTER
            setShadowLayer(0f,0f,0f,Color.BLACK)
        }
        paintStatusText = TextPaint(paintMenuTextSelected).apply {
            textAlign = Paint.Align.RIGHT
            textSize = sd(15f)
        }
        paintStatusBoxFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(64,255,255,255)
            style = Paint.Style.FILL
        }

        paintStatusBoxOutline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(128,255,255,255)
            style = Paint.Style.STROKE
            strokeWidth = d(1.5f)
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        paintIconUnselected = TextPaint(paintIconSelected).apply { alpha = 128 }
        paintFill.apply {
            color = Color.WHITE
        }
    }

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

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.VshView, defStyle, 0
        )
        a.recycle()
        renderableArea = getSystemPadding()
        density = resources.displayMetrics.density
        scaledDensity = resources.displayMetrics.scaledDensity
        generatePaint()
        if(FontCollections.masterFont != null){
            xmbFont = FontCollections.masterFont!!
        }
        fillCategory()

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun getDrawable(r:Int): Drawable {
        return resources.getDrawable(r);
    }

    @Suppress("DEPRECATION")
    fun fillCategory(){
        val home = VshX("HOME", context.getString(R.string.category_home), getDrawable(R.drawable.category_home), density)
        category.add(home)
        category.add(VshX("SETT", context.getString(R.string.category_settings), getDrawable(R.drawable.category_setting), density))
        category.add(VshX("SONG", context.getString(R.string.category_music),  getDrawable(R.drawable.category_music), density))
        category.add(VshX("FILM", context.getString(R.string.category_videos), getDrawable(R.drawable.category_video), density))
        category.add(VshX("APPS", context.getString(R.string.category_apps), getDrawable(R.drawable.category_apps), density))
        category.add(VshX("GAME", context.getString(R.string.category_games),  getDrawable(R.drawable.category_games), density))
    }

    // Draw text from it's top instead of baseline like JS does :P
    private fun Canvas.drawTextU(text:String, x:Float, y:Float, paint:TextPaint){
        text.split("\n").forEachIndexed { index, s ->
            val ym = y + paint.textSize + (paint.textSize * index)
            drawText(s, x, ym, paint)
        }
    }

    private fun Canvas.drawTextC(text:String, x:Float, y:Float, paint:TextPaint){
        text.split("\n").forEachIndexed { index, s ->
            val ym = y + (paint.textSize/2f) + (paint.textSize * index)
            drawText(s, x, ym, paint)
        }
    }

    /// region Size Change

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if(isInEditMode){
            // Just call super and return...
            super.onSizeChanged(w, h, oldw, oldh)
            return
        }
        renderableArea = getSystemPadding()
        super.onSizeChanged(w, h, oldw, oldh)
    }
    /// endregion

    /// region Status Bar Rendering
    private var upperClockBg = RectF(0f,0f,0f,0f)
    var clockExpandInfo = ""
    var operatorName = ""
    var use24Format = false
    private var lastClockExpandInfo = "-"
    private var clockPath = Path()
    private var clockHand = PointF(0f,0f)
    var clockAsLoadingIndicator = false
    private var clockStr = "10:43"
    val cal = Calendar.getInstance()
    var xmbFont : Typeface = customTypeface
        set(value) {
            field = value
            customTypeface = value
            paintStatusText.typeface = value
            paintTextSelected.typeface = value
            paintTextUnselected.typeface = value
            paintMenuTextSelected.typeface = value
            paintSubtextSelected.typeface = value
            paintSubtextUnselected.typeface = value
        }
    var xmbSystemFont : Typeface = FontCollections.buttonFont

    private fun calculateHandRotation(t : Float, r : Float){
        // Do rotating the clock by frame instead of clock
        if(clockAsLoadingIndicator){
            clockHand.x = cos( ( ((frame % 60 / 60f) * 360) - 90 ) * Deg2Rad).toFloat() * r
            clockHand.y = sin( ( ((frame % 60 / 60f) * 360) - 90 ) * Deg2Rad ).toFloat() * r
        }else{
            clockHand.x = cos( ( (t * 360) - 90 ) * Deg2Rad).toFloat() * r
            clockHand.y = sin( ( (t * 360) - 90 ) * Deg2Rad ).toFloat() * r
        }
    }

    private fun updateClock(x:Float, y:Float){
        cal.timeInMillis = System.currentTimeMillis()
        clockStr = if(use24Format){
            SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(cal.time)
        }else{
            SimpleDateFormat("dd/MM hh:mm aa", Locale.getDefault()).format(cal.time)
        }

        val hour = cal.get(Calendar.HOUR)
        val minute = cal.get(Calendar.MINUTE)

        clockPath.reset()
        clockPath.addCircle(x,y, sd(10f), Path.Direction.CW)

        // Animated Clock
        if(clockAsLoadingIndicator){
            val tt = frame % 60f / 60f
            clockPath.addCircle(x,y,sd(tt * 12f), Path.Direction.CW)
        }

        clockPath.moveTo(x,y)

        // Draw hour hand
        calculateHandRotation((hour % 12 ) / 12f, 5f)
        clockPath.lineTo(x + d(clockHand.x), y + d(clockHand.y))
        clockPath.lineTo(x,y)

        // Draw minute hand
        calculateHandRotation((minute % 60)/60f, 7f)
        clockPath.lineTo(x + d(clockHand.x ), y + d(clockHand.y))
        clockPath.lineTo(x,y)
        clockPath.close()
    }

    fun recalculateClockRect(){
        val renderHeight = renderableArea.height()
        val renderWidth = renderableArea.width()
        val yPivot = renderableArea.top + renderHeight * 0.1f
        val l = renderWidth - (d(300f) + (renderWidth * 0.1f))
        val u = yPivot - sd(15f)
        val r = renderWidth * 2f
        val height = if(clockExpandInfo.isEmpty()) 10f else 25f
        val d = yPivot + sd(height)
        upperClockBg = RectF(l,u,r,d)
        val infoStatusX = renderableArea.right.toFloat() - d(30f)
        expandInfoClipRect = RectF(l,u,infoStatusX,d)
        fullCanvasRect = RectF(0f,0f,renderWidth.toFloat(), this.height.toFloat())
    }

    private var lastWidth = 0
    private var lastHeight = 0
    private var expandInfoClipRect = RectF(0f,0f,0f,0f)
    private var fullCanvasRect = RectF(0f,0f,0f,0f)
    private var expandInfoTextRect = Rect(0,0,0,0)

    //TODO: update this function to support both PS3-style and PSP-style XMB clock
    private fun lClock(canvas: Canvas){
        if(hideClock) return
        if(clockExpandInfo != lastClockExpandInfo || lastWidth != width || lastHeight != height || expandInfoClipRect.isEmpty){
            recalculateClockRect()
        }

        canvas.drawRoundRect(upperClockBg, 5f * density, 5f * density, paintStatusBoxFill)
        canvas.drawRoundRect(upperClockBg, 5f * density, 5f * density, paintStatusBoxOutline)

        val statusText = "$operatorName     $clockStr"
        var statusX = renderableArea.right.toFloat() - d(30f)

        updateClock(renderableArea.right.toFloat() - d(15f), upperClockBg.centerY())

        paintStatusBoxFill.color = Color.argb(64,0,0,0)
        paintStatusBoxOutline.color = Color.WHITE

        canvas.drawPath(clockPath, paintStatusBoxFill)
        canvas.drawPath(clockPath, paintStatusBoxOutline)

        paintStatusBoxFill.color = Color.argb(64,255,255,255)
        paintStatusBoxOutline.color = Color.argb(128,255,255,255)

        lastClockExpandInfo = clockExpandInfo

        if(clockExpandInfo.isEmpty()){
            canvas.drawTextC(statusText , statusX, upperClockBg.centerY() - sd(2f), paintStatusText)
        }else{
            canvas.drawText(statusText, statusX, upperClockBg.centerY() - sd(2f), paintStatusText)

            paintStatusText.getTextBounds(clockExpandInfo, 0, clockExpandInfo.length, expandInfoTextRect)

            if(expandInfoClipRect.width() < expandInfoTextRect.width()){
                canvas.save()
                canvas.clipRect(expandInfoClipRect)

                statusX -= (frame * 2) % (expandInfoTextRect.width()*2f)

                paintStatusText.textAlign = Paint.Align.LEFT
                canvas.drawTextU(clockExpandInfo, statusX, upperClockBg.centerY() - sd(2f), paintStatusText)
                paintStatusText.textAlign = Paint.Align.RIGHT
                canvas.restore()
            }else{

                // Just render it as-is
                canvas.drawTextU(clockExpandInfo, statusX, upperClockBg.centerY() - sd(2f), paintStatusText)
            }
        }
    }
    /// endregion

    private fun lHorizontalMenu(canvas:Canvas){
        val pivotX = width * 0.3f
        val pivotY = height * 0.3f
        category.forEachIndexed{ index, data ->

            val iconPaint = if(index == selectedX){paintIconSelected}else{paintIconUnselected}
            val icon = if(index == selectedX){data.selectedIcon}else{data.unselectedIcon}

            val screenX = (pivotX + ((index - selectedXf) * d(100))) - (icon.width / 2f)
            // Don't render item outside outside
            if(screenX > -icon.width && screenX < width+icon.width){
                canvas.drawBitmap( icon ,screenX, pivotY - (icon.height/2f), iconPaint)
                if(index == selectedX){
                    canvas.drawTextC(data.name, screenX + (icon.width / 2f), pivotY + (icon.height / 2f), paintMenuTextSelected )
                }
            }
        }
    }

    private fun lVerticalItems(canvas:Canvas){
        val pivotX = (width * 0.3f) - ((selectedX - selectedXf) * d(-100))
        val pivotY = height * 0.3f

        val items : ArrayList<VshY>? = if(subcontentStack.hasContent()) subcontentStack.peek()?.subContent else category[selectedX].items

        // Don't do any rendering if empty
        if(items?.isEmpty() != false) return

        try{
            items.forEachIndexed { index, data ->
                val isSelected = index == selectedY
                val iconPaint = if (isSelected) {
                    paintIconSelected
                } else {
                    paintIconUnselected
                }
                val textPaint = if (isSelected) {
                    paintTextSelected
                } else {
                    paintTextUnselected
                }
                val subtextPaint = if (isSelected) {
                    paintSubtextSelected
                } else {
                    paintSubtextUnselected
                }

                val icon = if (isSelected) {
                    data.selectedIcon
                } else {
                    data.unselectedIcon
                }
                var centerY = pivotY + ((index - selectedYf) * d(60f)) + d(100f)
                var screenY =
                    pivotY + ((index - selectedYf) * d(60f)) - (icon.height / 2f) + d(100f)
                if (index - selectedY < 0 && !subcontentStack.hasContent()) {
                    screenY -= d(110f)
                    centerY -= d(110f)
                }
                if (index - selectedY > 0) {
                    screenY += d(30f)
                    centerY += d(30f)
                }

                // don't render offscreen and set the visibility to call the onScreen and onHidden
                val isOnScreen = centerY > -icon.height && centerY < height + icon.height
                data.isCoordinatelyVisible = isOnScreen
                data.isSelected = isSelected
                if (isOnScreen) {
                    // 80f : Preserve space for XMB-sized (20:11) icons
                    val x = pivotX + (d(80f))
                    val y = screenY + (icon.height / 2f)
                    canvas.drawBitmap(icon, pivotX - (icon.width / 2f), screenY, iconPaint)
                    if(data.hasDescription){
                        if(isSelected && descriptionSeparator){
                            canvas.drawLine(x, y, width * 1f, y, paintStatusBoxOutline)
                        }
                        canvas.drawText(data.name, x, y - sd(2f), textPaint)
                        canvas.drawTextU(data.description, x, y + sd(2f), subtextPaint)
                    }else{
                        canvas.drawTextC(data.name, x, y, textPaint )
                    }
                }
            }
        }catch(cmfce:ConcurrentModificationException){
            cmfce.printStackTrace()
        }
    }

    private var subContentOffset = 1.0f
    private var arrowIcon : Bitmap? = null

    private fun lSubContentItem(canvas:Canvas){
        val pivotX = (width * 0.3f) - ((selectedX - selectedXf) * d(-100))
        val pivotY = height * 0.3f

        if(arrowIcon == null){
            arrowIcon = getDrawable(R.drawable.miptex_arrow).toBitmap(d(16),d(16))
        }

        try{
            val folder = subcontentStack.peek()
            if(folder != null){
                val arrowX = (pivotX - subContentOffset.toLerp(d(50f), 0f))
                val arrowY = pivotY + d(100f)
                val arrow = arrowIcon ?: transparentBitmap
                canvas.drawBitmap(arrow, arrowX - arrow.width/2f, arrowY - arrow.height/2f, paintIconSelected)

                val folderIcon = folder.selectedIcon
                val screenX = (pivotX - subContentOffset.toLerp(d(100f), 0f))

                canvas.drawBitmap(folderIcon, screenX - (folderIcon.width/2f), pivotY - (folderIcon.height/2f) + d(100f), paintIconUnselected)

                // Draw the main category over current folder if is the first directory
                if(subcontentStack.count == 1){
                    val cat = category[selectedX]
                    val catIcon = cat.selectedIcon
                    canvas.drawBitmap(catIcon, screenX - (catIcon.width/2f), pivotY - (catIcon.height/2f), paintIconUnselected)
                }
            }

        }catch(e:Exception){

        }
    }

    /// region Options Popup
    var isOnOptions = false
    var optionsRect = RectF(0f,0f,0f,0f)
    var optionSelectedIndex = 0
    var optionArrowPath = Path()
    var optionXOffset = 0.0f

    private val optionTextBound = Rect()
    private fun lOptions(canvas:Canvas){
        updateOptionPopupShape()
        canvas.drawRect(optionsRect, paintStatusBoxFill)
        canvas.drawRect(optionsRect, paintStatusBoxOutline)

        // Draw contents
        if(isSelectionValid){
            val item = if(subcontentStack.hasContent()) subcontentStack.peek() else category[selectedX].items[selectedY]
            if(item != null){
                val options = item.options
                paintTextSelected.getTextBounds("M",0,1, optionTextBound)
                val charHeight = optionTextBound.height() * 1.5f
                val yOffset = (optionsRect.height() / 2f) + ((options.size * -charHeight)  /2f)
                val xTextOffset = optionTextBound.width() * 2
                options.forEachIndexed { index, vshOption ->
                    val isSelected = index == optionSelectedIndex
                    val paint = if(isSelected) paintTextSelected else paintTextUnselected
                    val yPos = (index * charHeight)+ yOffset
                    if(isSelected){
                        // Update triangle
                        val triR = optionTextBound.height() /2f
                        val leftPos = optionsRect.left + (0.25f * xTextOffset)
                        val rightPos = optionsRect.left + (0.75f * xTextOffset)
                        with(optionArrowPath){
                            reset()
                            moveTo(leftPos, yPos); lineTo(leftPos, yPos - triR)
                            lineTo(rightPos, yPos); lineTo(leftPos, yPos + triR)
                            lineTo(leftPos, yPos); close()
                        }
                        canvas.drawPath(optionArrowPath, paintFill)
                        canvas.drawPath(optionArrowPath, paintStatusBoxOutline)

                        optionLaunchArea.set(
                            leftPos, yPos - triR,
                            renderableArea.right.toFloat(), yPos +triR
                        )
                    }

                    if(!vshOption.shouldSkip){
                        canvas.drawText(vshOption.name, optionsRect.left + xTextOffset, yPos, paint, -0.5f)
                    }
                }
            }
        }
    }

    fun updateOptionPopupShape(){
        optionXOffset = (0.2f).toLerp(optionXOffset, if(isOnOptions) 1.0f else 0.0f)
        val intrinsicWidth = (d(200f) + padOffset.right)
        val width = optionXOffset.toLerp(d(-10f), intrinsicWidth)
        val furtherWidth = optionXOffset.toLerp(intrinsicWidth, d(10f))
        optionsRect.left = this.width - width
        optionsRect.top = -d(20f)
        optionsRect.bottom = height + d(30f)
        optionsRect.right = this.width + furtherWidth
    }

    fun switchOptionPopupVisibility(){
        setOptionPopupVisibility(!isOnOptions)
    }

    private val isSelectionValid : Boolean get() {
        var retval = false
        if(selectedX < category.size){
            retval = selectedY < if(subcontentStack.hasContent()){
                subcontentStack.peek()?.subContent?.size ?: 0
            }else{
                category[selectedX].items.size
            }
        }
        return retval
    }

    fun setOptionPopupVisibility(shown:Boolean){
        if(isSelectionValid){
            val item = if(subcontentStack.hasContent()) subcontentStack.peek() else category[selectedX].items[selectedY]
            if( item != null){
                isOnOptions = shown && item.hasOptions
                // reset option position when the item opens up
                if(isOnOptions) optionSelectedIndex = 0
            }
        }
    }

    /// endregion

    private fun mUpdate(){
        selectedYf = 0.75f.toLerp(selectedY.toFloat(), selectedYf)
        selectedXf = 0.75f.toLerp(selectedX.toFloat(), selectedXf)
        backgroundAlpha = 0.1f.toLerp(backgroundAlpha, hideMenu.choose(0f, 1f))
        frame++

        if(FontCollections.masterFont != null){
            xmbFont = FontCollections.masterFont!!
        }

        subContentOffset = (0.75f).toLerp(subContentOffset, 0f)

        if(frame > Float.MAX_VALUE /2f ) frame = 0f
    }

    ///region FPS Meter
    private var fpsRect = RectF(0f,0f,0f,0f)
    private fun lFPSMeter(canvas:Canvas){
        val ms = System.currentTimeMillis() - frameStart
        if(fpsRect.isEmpty || lastWidth != width || lastHeight != height){
            fpsRect.left = sd(20f)
            fpsRect.top = (height * 0.9f) - (sd(12f))
            fpsRect.right = sd(200f)
            fpsRect.bottom = fpsRect.top + sd(25f)
        }

        deltaTime = ms / 1000f

        val fps = (1000f / ms).roundToInt()
        canvas.drawRoundRect(fpsRect, 10f,10f,paintStatusBoxFill)
        canvas.drawRoundRect(fpsRect, 10f,10f,paintStatusBoxOutline)
        canvas.drawTextC("$fps FPS / $ms ms", fpsRect.centerX(), fpsRect.centerY(), paintMenuTextSelected)
    }
    /// endregion


    private fun mLateUpdate(){

    }

    /// region Debug Info
    var showDebugInfo = false
    private fun lDebugInfo(canvas:Canvas){
        arrayListOf(
            frame,
            frameStart,
            selectedX,
            selectedXf,
            paintStatusBoxOutline
        ).forEachIndexed{
                index, any ->
                canvas.drawText(any.toString(), d(10f), d(75f) + (d(15f)* index), paintTextSelected)
            }
        debugPaint.color = Color.argb(64,0,255,0)
        canvas.drawRect(renderableArea, debugPaint)
        debugPaint.color = Color.argb(64,255,0,0)
        canvas.drawRect(launchTapArea, debugPaint)
    }
    /// endregion Debug Info

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mUpdate()
        val backgroundColor = backgroundAlpha.toLerpColor(alphaColor, menuBackgroundColor)
        canvas.drawColor(backgroundColor)
        if(!hideMenu){
            lVerticalItems(canvas)
            if(subcontentStack.hasContent()){
                lSubContentItem(canvas)
            }else{
                lHorizontalMenu(canvas)
            }
        }
        lClock(canvas)
        mLateUpdate()
        lOptions(canvas)
        lButtonGuide(canvas)
        if(showFPSMeter) lFPSMeter(canvas)
        if(showDebugInfo) lDebugInfo(canvas)
        frameStart = System.currentTimeMillis()
        postInvalidate()

        lastWidth = width
        lastHeight = height
    }

    /// region Input
    fun setSelection(x:Int, y:Int){
        if(hideMenu) return

        if(subcontentStack.hasContent()){
            if(x != 0){
                if(x < 0){
                    subcontentStack.pop()
                    reassignYPos(false)
                }
                return
            }
        }

        if(isOnOptions && isSelectionValid){
            val selectedItem = subcontentStack.peek() ?: category[selectedX].items[selectedY]
            val max = selectedItem.options.size -1
            if(max < 0) return

            val selectedOption = selectedItem.options[optionSelectedIndex]

            optionSelectedIndex = (optionSelectedIndex + y).coerceIn(0, max)
            while((!selectedOption.enabled || selectedOption.shouldSkip) && optionSelectedIndex < max){
                optionSelectedIndex = (optionSelectedIndex + y).coerceIn(0, max)
            }
            return
        }

        // Set selected icon to be no longer selected and no longer onScreen then Save currently selected icon before changing
        if(x != 0){
            val selectedItem = subcontentStack.peek()?.subContent ?: category[selectedX].items
            if(selectedItem.size > selectedY){
                category[selectedX].items[selectedY].isSelected = false
            }
            selectedItem.forEach { it.isCoordinatelyVisible = false }
            category[selectedX].itemY = selectedY
        }

        selectedX = (selectedX + x).coerceIn(0, category.size - 1)

        if(category[selectedX].items.isNotEmpty()){
            val selectedItem = subcontentStack.peek()?.subContent ?: category[selectedX].items
            selectedY = (selectedY + y).coerceIn(0, selectedItem.size - 1)
        }

        if(x != 0){
            selectedY = category[selectedX].itemY
            // Skip sliding animation when changing between categories
            selectedYf = selectedY.toFloat()
        }

    }

    fun setSelectionAbs(x:Int, y:Int){
        if(hideMenu) return
        if(x != selectedX){
            category[selectedX].itemY = selectedY
        }

        selectedX = x.coerceIn(0, category.size - 1)
        if(category[selectedX].items.isNotEmpty()){
            selectedY = y.coerceIn(0, category[selectedX].items.size - 1)
        }

        // Skip vertical sliding animation
        selectedYf = selectedY.toFloat()
    }

    fun sendBackSignal(){
        if(isOnOptions){
            isOnOptions = false
        }else if(subcontentStack.hasContent()){
            subcontentStack.pop()
            reassignYPos(false)
        }else if(!hideMenu){
            hideMenu = true
        }
    }

    fun reassignYPos(open: Boolean){
        val maxSizePosition = if(subcontentStack.hasContent()) subcontentStack.peek()?.subContent?.size ?: 0 else category[selectedX].items.size
        selectedXf += open.choose(-1f, 1f)
        subContentOffset = open.choose(-1f, 1f)
        selectedY = selectedY.coerceIn(0,  maxSizePosition - 1)
    }

    fun executeCurrentItem(){
        if(hideMenu) {
            // Press Menu to Unhide
            hideMenu = false
        }else{
            try{
                val data = subcontentStack.peek() ?: category[selectedX].items[selectedY]
                if(data.hasSubContent){
                    subcontentStack.push(data)
                    Log.d(TAG, "Try execution | Subcontent count : ${subcontentStack.count} - LastContent : ${subcontentStack.peek()}}")
                    reassignYPos(true)
                }else{
                    category[selectedX].items[selectedY].onLaunch.run()
                }
            }catch (ex:Exception){ ex.printStackTrace() }
        }
    }

    private var buttonGuide =
        "|{btn:confirm}| : Select\n|{btn:cancel}| : Back\n|{btn:menu}| : Menu\n|{btn:home}| : Home"
    private fun formatButtonGuide() : MultifontSpan {
        val retval = MultifontSpan()
        var useBtnFont = false
        val txt = buttonGuide.applyFontMacro(ButtonType.PlayStation, false)
        txt.split('|').forEach{
            useBtnFont = !useBtnFont
            retval.add(if(useBtnFont) xmbFont else xmbSystemFont, it)
        }
        return retval
    }

    private var buttonGuideRect : Rect = Rect()

    private fun updateButtonGuideRect(ctx:Canvas) {
        val text = formatButtonGuide()
        val sizeRect = Rect()
        val sysPad = getSystemPadding()
        ctx.getTextBound(text, paintTextSelected, sizeRect)
        buttonGuideRect.set(
            sysPad.right - sizeRect.width() - sd(30),
            sysPad.bottom - sizeRect.height() - sd(30),
            sysPad.right - sd(10),
            sysPad.bottom - sd(10),
        )
    }

    private fun lButtonGuide(ctx:Canvas){
        updateButtonGuideRect(ctx)
        val text = formatButtonGuide()
        ctx.drawRoundRect(
            buttonGuideRect.toRectF(),
            sd(10f),
            sd(10f),
            paintStatusBoxFill
        )

        ctx.drawRoundRect(
            buttonGuideRect.toRectF(),
            sd(10f),
            sd(10f),
            paintStatusBoxOutline
        )
        ctx.drawText(text,
            buttonGuideRect.left + sd(5f),
            buttonGuideRect.top + sd(5f),
            -1.0f,
            paintTextSelected
        )
    }

    fun executeCurrentOptionItem(){
        try{
            val currentOpt =
                (subcontentStack.peek() ?: category[selectedX].items[selectedY])
                .options[optionSelectedIndex]
            if(currentOpt.enabled){
                currentOpt.onClick.run()
                isOnOptions = false
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun findById(id:String) : VshX? = category.find { it.id == id }
    /// endregion
}
