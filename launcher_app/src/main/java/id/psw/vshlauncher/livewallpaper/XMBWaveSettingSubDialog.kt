package id.psw.vshlauncher.livewallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.*
import android.service.wallpaper.WallpaperService
import android.text.TextPaint
import androidx.core.graphics.withTranslation
import id.psw.vshlauncher.PrefEntry
import id.psw.vshlauncher.R
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.select
import id.psw.vshlauncher.submodules.GamepadSubmodule
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.typography.MultifontSpan
import id.psw.vshlauncher.typography.drawText
import id.psw.vshlauncher.views.VshViewPage
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.views.dialogviews.SubDialogUI
import id.psw.vshlauncher.views.drawText
import id.psw.vshlauncher.views.wrapText
import kotlin.math.abs

class XMBWaveSettingSubDialog(private val vsh: VSH) : XmbDialogSubview(vsh) {
    /*
    * Pages:
    * - 0 : Style / Speed
    * - 1 : Background Color
    * - 2 : Wave Color
    * */
    private var pageNumber = 0
    private var pageNumberF = 0.0f
    private var selectedItemIndices = arrayOf(0,0,0)
    private val selectedItemCounts = arrayOf(3,8,10)
    private val lrChangePageAtOptionNum = arrayOf(Point(0,2), Point(1,7), Point(2,9))
    private val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = FontCollections.masterFont
        textSize = 25.0f
    }
    private var isLiveWallpaperActive = false

    private class StateData {
        var style = 0
        var speed = 1.0f
        var bgColorMode = 0
        var bgTop = arrayOf(0,0,0)
        var bgBottom = arrayOf(0,0,0)
        var fgEdge = arrayOf(0,0,0,0)
        var fgCenter = arrayOf(0,0,0,0)
    }
    private val state = StateData()

    private fun makeSelectionShadow(isSelection : Boolean){
        textPaint.setShadowLayer(5.0f, 0.0f, 0.0f, isSelection.select(Color.WHITE, Color.BLACK))
    }
    private fun textAlign(align:Paint.Align){
        textPaint.textAlign = align
    }
    private fun textLine(from : Float, index: Float) : Float = from + ((textPaint.textSize * 1.1f) * index)

    private val colorPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE

    }

    private val titles = arrayOf(
        vsh.getString(R.string.waveset_behavior_style),
        vsh.getString(R.string.waveset_color_background),
        vsh.getString(R.string.waveset_color_foreground)
    )

    override val title: String
        get() = titles[pageNumber.coerceIn(0,2)]
    private var pageCenter = PointF(0.0f, 0.0f)
    private var currentTime = 0.0f

    override val hasPositiveButton: Boolean = true
    override val hasNegativeButton: Boolean = true
    override val negativeButton: String get(){
        return vsh.getString((pageNumber == 0).select(android.R.string.cancel, R.string.common_back))
    }
    override val positiveButton: String get() {
        return vsh.getString((pageNumber == 2).select(R.string.common_finish, R.string.common_next))
    }

    private fun checkActiveWallpaper(){
        val ws = vsh.getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager
        val asWallpaper = ws.wallpaperInfo?.packageName == vsh.packageName
        val asLayer = vsh.pref.getBoolean(PrefEntry.USES_INTERNAL_WAVE_LAYER, false)
        isLiveWallpaperActive = asWallpaper || asLayer
    }

    override fun onStart() {
        checkActiveWallpaper()
    }

    private fun getWaveStyleName(style:Int): String{
        return when(style.toByte()){
            XMBWaveRenderer.WAVE_TYPE_PS3_BLINKS -> vsh.getString(R.string.waveset_behavior_style_ps3_dynamic)
            XMBWaveRenderer.WAVE_TYPE_PS3_NORMAL -> vsh.getString(R.string.waveset_behavior_style_ps3_classic)
            XMBWaveRenderer.WAVE_TYPE_PSP_CENTER -> vsh.getString(R.string.waveset_behavior_style_psp_centered)
            XMBWaveRenderer.WAVE_TYPE_PSP_BOTTOM -> vsh.getString(R.string.waveset_behavior_style_psp_classic)
            else -> "Unknown"
        }
    }
    private fun swapWaveStyleName(moveRight:Boolean){
        state.style = when(state.style.toByte()){
            XMBWaveRenderer.WAVE_TYPE_PS3_NORMAL -> moveRight.select(XMBWaveRenderer.WAVE_TYPE_PSP_CENTER, XMBWaveRenderer.WAVE_TYPE_PS3_BLINKS)
            XMBWaveRenderer.WAVE_TYPE_PS3_BLINKS -> moveRight.select(XMBWaveRenderer.WAVE_TYPE_PS3_NORMAL, XMBWaveRenderer.WAVE_TYPE_PSP_BOTTOM)
            XMBWaveRenderer.WAVE_TYPE_PSP_BOTTOM -> moveRight.select(XMBWaveRenderer.WAVE_TYPE_PS3_BLINKS, XMBWaveRenderer.WAVE_TYPE_PSP_CENTER)
            XMBWaveRenderer.WAVE_TYPE_PSP_CENTER -> moveRight.select(XMBWaveRenderer.WAVE_TYPE_PSP_BOTTOM, XMBWaveRenderer.WAVE_TYPE_PS3_NORMAL)
            else -> XMBWaveRenderer.WAVE_TYPE_PS3_NORMAL
        }.toInt()
        NativeGL.setWaveStyle(state.style.toByte())
    }

    private fun changePage(next:Boolean){
        pageNumber = (pageNumber + next.select(1, -1)).coerceIn(0, 2)
    }

    private fun Pair<Canvas, RectF>.page(pageNumber:Float, pageFunc : () -> Unit){
        first.withTranslation((pageNumber - pageNumberF) * second.width(), 0.0f){

            if(abs(pageNumberF - pageNumber) < 0.5){
                pageFunc()
            }
        }
    }

    override fun onClose() {
        vsh.waveShouldReReadPreferences = true
    }

    private fun drawPageLeftRight(ctx:Canvas, drawBound: RectF){
        val pageNum = pageNumber
        val itemNum = selectedItemIndices[pageNum]
        val canChangePage = lrChangePageAtOptionNum.find { it -> it.x == pageNum && it.y == itemNum } != null

        if(canChangePage){
            textAlign(Paint.Align.CENTER)
            SubDialogUI.arrowCapsule(ctx, drawBound.centerX(), drawBound.centerY(), drawBound.width() - 100.0f, textPaint, currentTime, 0.5f,
            pageNumber > 0, pageNumber < 2)
        }
    }

    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime:Float) {
        if(pageNumberF > pageNumber) pageNumberF = (pageNumberF - (deltaTime * 8.0f)).coerceAtLeast(pageNumber.toFloat())
        if(pageNumberF < pageNumber) pageNumberF = (pageNumberF + (deltaTime * 8.0f)).coerceAtMost(pageNumber.toFloat())
        currentTime += deltaTime
        pageCenter.set(drawBound.centerX(), drawBound.centerY())

        makeSelectionShadow(false)
        textAlign(Paint.Align.CENTER)
        val precauLines = arrayListOf<String>()
        precauLines.addAll(textPaint.wrapText("Touch screen : Use Swipe to move between items and pages", drawBound.width()).lines())
        if(!isLiveWallpaperActive) {
            precauLines.addAll(textPaint.wrapText("Wave Wallpaper is not currently active, Preview will not available.", drawBound.width()).lines())
        }
        precauLines.forEachIndexed { i, it ->
            ctx.drawText(
                it,
                drawBound.centerX(),
                textLine(drawBound.top, 1.0f),
                textPaint,
                i + 1.0f
            )
        }

        onDrawPage1(ctx, drawBound)
        onDrawPage2(ctx, drawBound)
        onDrawPage3(ctx, drawBound)

        drawPageLeftRight(ctx, drawBound)

    }
    private fun isSelected(page:Int, idx:Int) : Boolean{
        return selectedItemIndices[page] == idx
    }

    private val kvpValueBuffer = RectF()

    private fun drawKvp(ctx:Canvas, page:Int, vararg items: Pair<String, (RectF) -> Pair<Boolean, Boolean>>){
        val cCenter = items.size * 0.5f
        val top = textLine(pageCenter.y, -cCenter)
        items.forEachIndexed { i, it ->
            val iif = i.toFloat()
            makeSelectionShadow(isSelected(page,i))
            textAlign(Paint.Align.RIGHT)
            ctx.drawText(it.first, pageCenter.x - 25.0f, textLine(top, iif), textPaint, 1.0f)
            textAlign(Paint.Align.CENTER)
            kvpValueBuffer.set(pageCenter.x + 25.0f, textLine(top, iif), pageCenter.x + 250.0f, textLine(top, iif + 1.0f))
            val lr = it.second.invoke(kvpValueBuffer)
            if(isSelected(page, i)){
                textAlign(Paint.Align.CENTER)
                SubDialogUI.arrowCapsule(ctx, kvpValueBuffer.centerX(), textLine(top, iif), 250.0f, textPaint, currentTime, 1.0f, isLeft = lr.first, isRight = lr.second)
            }
        }
    }

    private fun drawGaugeWithValue(ctx:Canvas, min:Float, max:Float, value:Float, formater : (Float) -> String, it:RectF){
        SubDialogUI.progressBar(ctx, min, max, value, it.left, it.centerY() - 6f, it.width() - 50.0f, align = Paint.Align.LEFT)
        textAlign(Paint.Align.LEFT)
        ctx.drawText(formater(value), it.right - 40.0f, it.top, textPaint, 1.0f)
    }
    private fun <T:Comparable<T>> rangePair(value:T, min:T, max:T) : Pair<Boolean, Boolean>{
        return (value > min) to (value < max)
    }

    private fun onDrawPage1(ctx: Canvas, drawBound: RectF) {
        (ctx to drawBound).page(0.0f){
            textAlign(Paint.Align.RIGHT)

            drawKvp(ctx, 0, vsh.getString(R.string.waveset_behavior_style) to {
                ctx.drawText(getWaveStyleName(state.style), it.centerX(), it.top, textPaint, 1.0f)
                (true to true)
            }, vsh.getString(R.string.waveset_behavior_speed) to {
                drawGaugeWithValue(ctx, 0.0f, 5.0f, state.speed, { f -> "%.1f".format(f) }, it)
                ((state.speed > 0.11) to (state.speed < 4.99))
            })

            textAlign(Paint.Align.CENTER)
            makeSelectionShadow(isSelected(0, 2))
            ctx.drawText("Next", pageCenter.x, drawBound.bottom - (textPaint.textSize * 1.1f), textPaint)
        }
    }

    private fun colorKVPDrawFunc(ctx:Canvas, value:Int, it:RectF) : Pair<Boolean, Boolean> {
        drawGaugeWithValue(ctx, 0.0f, 255.0f, value.toFloat(), { value.toString() }, it)
        return rangePair(value, 0, 255)
    }

    private val testBufferRectF = RectF()

    private fun onDrawPage2(ctx: Canvas, drawBound: RectF) {
        (ctx to drawBound).page(1.0f){

            testBufferRectF.set(
                drawBound.centerX() - 150.0f,
                drawBound.centerY() - 150.0f,
                drawBound.centerX() + 150.0f,
                drawBound.centerY() + 150.0f
            )

            ctx.drawRect(testBufferRectF, colorPaint)

            drawKvp(ctx, 1,
                "Top Grdt. R" to { colorKVPDrawFunc(ctx, state.bgTop[0], it) },
                "Top Grdt. G" to { colorKVPDrawFunc(ctx, state.bgTop[1], it) },
                "Top Grdt. B" to { colorKVPDrawFunc(ctx, state.bgTop[2], it)  },
                "Bottom Grdt. R" to { colorKVPDrawFunc(ctx, state.bgBottom[0], it) },
                "Bottom Grdt. G" to { colorKVPDrawFunc(ctx, state.bgBottom[1], it) },
                "Bottom Grdt. B" to { colorKVPDrawFunc(ctx, state.bgBottom[2], it) },
                "Test to Wave" to {
                    if(isSelected(1, 6)) {
                        textAlign(Paint.Align.CENTER)

                        var t = currentTime % 4.0
                        if(t < 2.0){
                            ctx.drawText(
                                MultifontSpan()
                                    .add(FontCollections.masterFont, "Press ")
                                    .add(vsh, GamepadSubmodule.Key.PadR),
                                it.centerX(), it.centerY(),
                                0.5f, textPaint
                            )
                        }else{
                            ctx.drawText(
                                "Swipe to Right",
                                it.centerX(), it.centerY(),
                                textPaint, 0.5f
                            )
                        }
                    }
                    false to true
                }
            )

            textAlign(Paint.Align.CENTER)
            makeSelectionShadow(isSelected(1, 7))
            ctx.drawText("Next", pageCenter.x, drawBound.bottom - (textPaint.textSize * 1.1f), textPaint)
        }
    }

    private fun onDrawPage3(ctx: Canvas, drawBound: RectF) {
        (ctx to drawBound).page(2.0f){
            textAlign(Paint.Align.CENTER)
            makeSelectionShadow(isSelected(2, 9))
            ctx.drawText("Next", pageCenter.x, drawBound.bottom - (textPaint.textSize * 1.1f), textPaint)
        }
    }

    override fun onDialogButton(isPositive: Boolean) {
        if(isPositive){
            if(pageNumber == 2){
                // Save
                finish(VshViewPage.MainMenu)
            }else{
                changePage(true)
            }
        }else{
            if(pageNumber == 0){
                finish(VshViewPage.MainMenu)
            }else{
                changePage(false)
            }
        }
    }

    override fun onGamepad(key: GamepadSubmodule.Key, isPress: Boolean): Boolean {
        var handled = false
        if(isPress){
            val pageNum = pageNumber
            val itemNum = selectedItemIndices[pageNum]
            val canChangePage = lrChangePageAtOptionNum.find { it -> it.x == pageNum && it.y == itemNum } != null

            when(key){
                GamepadSubmodule.Key.PadL -> {
                    if(canChangePage){
                        changePage(false)
                    }else{
                        changeItemValue(false)
                    }
                    handled = true
                }
                GamepadSubmodule.Key.PadR -> {
                    if(canChangePage) {
                        changePage(true)
                    }else{
                        changeItemValue(true)
                    }
                    handled = true
                }
                GamepadSubmodule.Key.PadU ->{
                    changeItemIndex(false)
                    handled = true
                }
                GamepadSubmodule.Key.PadD ->{
                    changeItemIndex(true)
                    handled = true
                }
                else -> {}
            }
        }
        return handled || super.onGamepad(key, isPress)
    }

    private fun changeItemIndex(isDown : Boolean){
        pageNumber = pageNumber.coerceIn(0, 2)
        try{
            var cIndex = selectedItemIndices[pageNumber]
            val max = selectedItemCounts[pageNumber]
            cIndex += isDown.select(1, -1)
            if(cIndex < 0) cIndex = max - 1
            else if(cIndex >= max) cIndex = 0
            selectedItemIndices[pageNumber] = cIndex
        }catch(e:ArrayIndexOutOfBoundsException){
            pageNumber = pageNumber.coerceIn(0, 2)
        }
    }

    private fun chColor(value:Int, add:Boolean) : Int{
        return (value + add.select(1, -1)).coerceIn(0, 255)
    }

    private fun updateColorPaintGradient(isFg: Boolean){
        val colors = if(isFg){
            intArrayOf(
                Color.argb(state.fgEdge[3],state.fgEdge[0],state.fgEdge[1],state.fgEdge[2]),
                Color.argb(state.fgCenter[3],state.fgCenter[0],state.fgCenter[1],state.fgCenter[2]),
                Color.argb(state.fgEdge[3],state.fgEdge[0],state.fgEdge[1],state.fgEdge[2])
            )
        }else{
            intArrayOf(
                Color.argb(255, state.bgTop[0],state.bgTop[1],state.bgTop[2]),
                Color.argb(255, state.bgBottom[0],state.bgBottom[1],state.bgBottom[2])
            )
        }
        colorPaint.shader = LinearGradient(
            pageCenter.x, pageCenter.y - 150f, pageCenter.x, pageCenter.y + 150f,
            colors,
            null, Shader.TileMode.CLAMP
        )
    }

    private fun changeItemValue(isRight: Boolean) {
        // Page 1
        if(isSelected(0, 0)){
            swapWaveStyleName(isRight)
        }
        if(isSelected(0, 1)){
            state.speed = (((state.speed * 10).toInt() + isRight.select(1,-1)).toFloat() / 10.0f).coerceIn(0.1f, 5.0f)
        }

        // Page 2
        if(isSelected(1, 0)) {
            state.bgTop[0] = chColor(state.bgTop[0], isRight)
            updateColorPaintGradient(false)
        }
        if(isSelected(1, 1)) {
            state.bgTop[1] = chColor(state.bgTop[1], isRight)
            updateColorPaintGradient(false)
        }
        if(isSelected(1, 2)) {
            state.bgTop[2] = chColor(state.bgTop[2], isRight)
            updateColorPaintGradient(false)
        }
        if(isSelected(1, 3)) {
            state.bgBottom[0] = chColor(state.bgBottom[0], isRight)
            updateColorPaintGradient(false)
        }
        if(isSelected(1, 4)) {
            state.bgBottom[1] = chColor(state.bgBottom[1], isRight)
            updateColorPaintGradient(false)
        }
        if(isSelected(1, 5)) {
            state.bgBottom[2] = chColor(state.bgBottom[2], isRight)
            updateColorPaintGradient(false)
        }

    }
}