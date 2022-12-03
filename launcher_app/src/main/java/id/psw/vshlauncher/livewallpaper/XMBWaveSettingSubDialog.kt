package id.psw.vshlauncher.livewallpaper

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.text.TextPaint
import android.view.MotionEvent
import androidx.core.graphics.minus
import androidx.core.graphics.withTranslation
import id.psw.vshlauncher.PrefEntry
import id.psw.vshlauncher.R
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.select
import id.psw.vshlauncher.submodules.GamepadSubmodule
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.typography.drawText
import id.psw.vshlauncher.typography.getButtonedString
import id.psw.vshlauncher.views.VshViewPage
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.views.dialogviews.SubDialogUI
import id.psw.vshlauncher.views.drawText
import id.psw.vshlauncher.views.wrapText
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class XMBWaveSettingSubDialog(private val vsh: VSH) : XmbDialogSubview(vsh) {
    /*
    * Pages:
    * - 0 : Style / Speed
    * - 1 : Background Color
    * - 2 : Wave Color
    * */
    private var pageNumber = 0
    private lateinit var pref : SharedPreferences
    private var pageNumberF = 0.0f
    private var selectedItemIndices = arrayOf(0,0,0)
    private val selectedItemCounts = arrayOf(5,8,10)
    private lateinit var monthNameGetter : SimpleDateFormat
    private val lrChangePageAtOptionNum = arrayOf(Point(0,4), Point(1,7), Point(2,9))
    private val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = FontCollections.masterFont
        textSize = 25.0f
    }
    private var isLiveWallpaperActive = false
    private class StateData {
        var style = 0
        var speed = 1.0f
        var isDayNight = false
        var month = 0
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
        monthNameGetter = SimpleDateFormat("MMMM", Locale.getDefault())
        pref = vsh.getSharedPreferences(XMBWaveSurfaceView.PREF_NAME, Context.MODE_PRIVATE)
        checkActiveWallpaper()
        readWallpaperPreferences()
    }

    private fun readWallpaperPreferences() {
        val backA = pref.getInt(XMBWaveSurfaceView.KEY_COLOR_BACK_A, Color.argb(255,0,128,255))
        val backB = pref.getInt(XMBWaveSurfaceView.KEY_COLOR_BACK_B, Color.argb(255,0,0,255))
        val foreA = pref.getInt(XMBWaveSurfaceView.KEY_COLOR_FORE_A, Color.argb(255,255,255,255))
        val foreB = pref.getInt(XMBWaveSurfaceView.KEY_COLOR_FORE_B, Color.argb(0,255,255,255))

        state.isDayNight = pref.getBoolean(XMBWaveSurfaceView.KEY_DTIME, true)
        state.month = pref.getInt(XMBWaveSurfaceView.KEY_MONTH, 0)
        state.speed = pref.getFloat(XMBWaveSurfaceView.KEY_SPEED, 1.0f)
        state.style = pref.getInt(XMBWaveSurfaceView.KEY_STYLE, XMBWaveRenderer.WAVE_TYPE_PS3_NORMAL.toInt())
        state.bgTop = arrayOf(Color.red(backA), Color.green(backA), Color.blue(backA))
        state.bgBottom = arrayOf(Color.red(backB), Color.green(backB), Color.blue(backB))
        state.fgEdge = arrayOf(Color.alpha(foreA),Color.red(foreA), Color.green(foreA), Color.blue(foreA))
        state.fgCenter = arrayOf(Color.alpha(foreB),Color.red(foreB), Color.green(foreB), Color.blue(foreB))
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
        sendToNativeGL()
    }

    private fun changePage(next:Boolean){
        pageNumber = (pageNumber + next.select(1, -1)).coerceIn(0, 2)
        when(pageNumber){
            1 -> {
                updateColorPaintGradient(false)
            }
            2 -> {
                updateColorPaintGradient(true)
            }
            else -> {
                // Do nothing
            }
        }
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

    private fun sendToNativeGL(){
        NativeGL.setWaveStyle(state.style.toByte())


        NativeGL.setBgDayNightMode(state.isDayNight)
        NativeGL.setBackgroundMonth(state.month.toByte())
        NativeGL.setSpeed(state.speed)
        NativeGL.setBackgroundColor(
            Color.rgb(
                state.bgTop[0], state.bgTop[1],
                state.bgTop[2]
            ),
            Color.rgb(
                state.bgBottom[0], state.bgBottom[1],
                state.bgBottom[2]
            )
        )
        NativeGL.setForegroundColor(
            Color.argb(
                state.fgEdge[0], state.fgEdge[1],
                state.fgEdge[2], state.fgEdge[3]
            ),
            Color.argb(
                state.fgCenter[0], state.fgCenter[1],
                state.fgCenter[2], state.fgCenter[3]
            )
        )
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
        precauLines.addAll(textPaint.wrapText(vsh.getString(R.string.waveset_dlg_touchscreen_usage), drawBound.width()).lines())
        if(!isLiveWallpaperActive) {
            precauLines.addAll(textPaint.wrapText(vsh.getString(R.string.waveset_wave_not_active), drawBound.width()).lines())
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
        return selectedItemIndices[page] == idx && pageNumber == page
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
            }, vsh.getString(R.string.waveset_daynight_cycle) to {
                SubDialogUI.checkBox(ctx, it.centerX(), it.centerY(), state.isDayNight)
                (true to true)
            }, vsh.getString(R.string.waveset_bg_month_number) to {
                val name = when(state.month){
                    -1 -> vsh.getString(R.string.waveset_bg_month_custom)
                    0 -> vsh.getString(R.string.waveset_bg_month_current)
                    in 1 .. 12 -> {
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.MONTH, state.month - 1);
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                        monthNameGetter.format(cal.time)
                    }
                    else -> vsh.getString(R.string.unknown)
                }
                textAlign(Paint.Align.CENTER)
                ctx.drawText(name, it.centerX(), it.centerY(), textPaint, 0.5f)
                (true to true)
            }
            )

            textAlign(Paint.Align.CENTER)
            makeSelectionShadow(isSelected(0, 4))
            ctx.drawText(vsh.getString(R.string.waveset_dlg_page_number).format(1,3), pageCenter.x, drawBound.bottom - (textPaint.textSize * 1.1f), textPaint)
        }
    }

    private fun colorKVPDrawFunc(ctx:Canvas, value:Int, it:RectF) : Pair<Boolean, Boolean> {
        drawGaugeWithValue(ctx, 0.0f, 255.0f, value.toFloat(), { value.toString() }, it)
        return rangePair(value, 0, 255)
    }

    private val testBufferRectF = RectF()

    private fun drawTestToWave(page:Int, idx:Int, ctx:Canvas, it:RectF, mustCustom:Boolean) : Pair<Boolean, Boolean>{
        if(isSelected(page, idx)) {
            if(mustCustom && state.month != -1){
                ctx.drawText(
                    vsh.getString(R.string.waveset_bg_is_not_custom),
                    it.centerX(), it.centerY(),
                    textPaint, 0.5f
                )
                return false to false
            }else{
                textAlign(Paint.Align.CENTER)
                val t = currentTime % 4.0
                if(t < 2.0){
                    ctx.drawText(
                        vsh.getButtonedString(R.string.waveset_test_press_right),
                        it.centerX(), it.centerY(),
                        0.5f, textPaint
                    )
                }else{
                    ctx.drawText(
                        vsh.getString(R.string.waveset_test_swipe_right),
                        it.centerX(), it.centerY(),
                        textPaint, 0.5f
                    )
                }
            }

        }
        return false to true
    }

    private fun onDrawPage2(ctx: Canvas, drawBound: RectF) {
        (ctx to drawBound).page(1.0f){

            testBufferRectF.set(
                drawBound.centerX() - 150.0f,
                drawBound.centerY() - 150.0f,
                drawBound.centerX() + 150.0f,
                drawBound.centerY() + 150.0f
            )

            ctx.drawRect(testBufferRectF, colorPaint)

            val colorFields = vsh.resources.getStringArray(R.array.xmb_wave_color_field_names)

            drawKvp(ctx, 1,
                colorFields[0] to { colorKVPDrawFunc(ctx, state.bgTop[0], it) },
                colorFields[1] to { colorKVPDrawFunc(ctx, state.bgTop[1], it) },
                colorFields[2] to { colorKVPDrawFunc(ctx, state.bgTop[2], it)  },
                colorFields[3] to { colorKVPDrawFunc(ctx, state.bgBottom[0], it) },
                colorFields[4] to { colorKVPDrawFunc(ctx, state.bgBottom[1], it) },
                colorFields[5] to { colorKVPDrawFunc(ctx, state.bgBottom[2], it) },
                vsh.getString(R.string.waveset_dlg_test_to_wave)  to { drawTestToWave(1, 6, ctx, it, true) }
            )

            textAlign(Paint.Align.CENTER)
            makeSelectionShadow(isSelected(1, 7))
            ctx.drawText(vsh.getString(R.string.waveset_dlg_page_number).format(2,3), pageCenter.x, drawBound.bottom - (textPaint.textSize * 1.1f), textPaint)
        }
    }

    private fun onDrawPage3(ctx: Canvas, drawBound: RectF) {
        (ctx to drawBound).page(2.0f){

            testBufferRectF.set(
                drawBound.centerX() - 150.0f,
                drawBound.centerY() - 150.0f,
                drawBound.centerX() + 150.0f,
                drawBound.centerY() + 150.0f
            )

            ctx.drawRect(testBufferRectF, colorPaint)

            val colorFields = vsh.resources.getStringArray(R.array.xmb_wave_color_field_names)
            drawKvp(ctx, 2,
                colorFields[9] to { colorKVPDrawFunc(ctx, state.fgEdge[0], it) },
                colorFields[6] to { colorKVPDrawFunc(ctx, state.fgEdge[1], it) },
                colorFields[7] to { colorKVPDrawFunc(ctx, state.fgEdge[2], it) },
                colorFields[8] to { colorKVPDrawFunc(ctx, state.fgEdge[3], it) },
                colorFields[13] to { colorKVPDrawFunc(ctx, state.fgCenter[0], it) },
                colorFields[10] to { colorKVPDrawFunc(ctx, state.fgCenter[1], it) },
                colorFields[11] to { colorKVPDrawFunc(ctx, state.fgCenter[2], it) },
                colorFields[12] to { colorKVPDrawFunc(ctx, state.fgCenter[3], it) },
                vsh.getString(R.string.waveset_dlg_test_to_wave) to { drawTestToWave(2, 8, ctx, it, false) }
            )

            textAlign(Paint.Align.CENTER)
            makeSelectionShadow(isSelected(2, 9))
            ctx.drawText(vsh.getString(R.string.waveset_dlg_page_number).format(3,3), pageCenter.x, drawBound.bottom - (textPaint.textSize * 1.1f), textPaint)
        }
    }

    override fun onDialogButton(isPositive: Boolean) {
        if(isPositive){
            if(pageNumber == 2){
                // Save
                saveStateToWavePreferences()
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

    @SuppressLint("ApplySharedPref")
    private fun saveStateToWavePreferences() {
        pref.edit()
            .putInt(XMBWaveSurfaceView.KEY_COLOR_BACK_A, Color.rgb(state.bgTop[0],state.bgTop[1],state.bgTop[2]))
            .putInt(XMBWaveSurfaceView.KEY_COLOR_BACK_B, Color.rgb(state.bgBottom[0],state.bgBottom[1],state.bgBottom[2]))
            .putInt(XMBWaveSurfaceView.KEY_COLOR_FORE_A, Color.argb(state.fgEdge[0],state.fgEdge[1],state.fgEdge[2],state.fgEdge[3]))
            .putInt(XMBWaveSurfaceView.KEY_COLOR_FORE_B, Color.argb(state.fgCenter[0],state.fgCenter[1],state.fgCenter[2],state.fgCenter[3]))
            .putBoolean(XMBWaveSurfaceView.KEY_DTIME, state.isDayNight)
            .putInt(XMBWaveSurfaceView.KEY_MONTH, state.month)
            .putFloat(XMBWaveSurfaceView.KEY_SPEED, state.speed)
            .putInt(XMBWaveSurfaceView.KEY_STYLE, state.style)
            .commit() // We need this to be synchronous
    }

    override fun onGamepad(key: GamepadSubmodule.Key, isPress: Boolean): Boolean {
        var handled = false
        if(isPress){
            val pageNum = pageNumber
            val itemNum = selectedItemIndices[pageNum]
            val canChangePage = lrChangePageAtOptionNum.find { it -> it.x == pageNum && it.y == itemNum } != null
            val isTen = key == GamepadSubmodule.Key.L1 || key == GamepadSubmodule.Key.R1

            when(key){
                GamepadSubmodule.Key.PadL, GamepadSubmodule.Key.L1 -> {
                    if(canChangePage){
                        changePage(false)
                    }else{
                        changeItemValue(false,isTen.select(10,1))
                    }
                    handled = true
                }
                GamepadSubmodule.Key.PadR, GamepadSubmodule.Key.R1 -> {
                    if(canChangePage) {
                        changePage(true)
                    }else{
                        changeItemValue(true, isTen.select(10,1) )
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

    private fun chColor(value:Int, count:Int, add:Boolean) : Int{
        return (value + add.select(count, -count)).coerceIn(0, 255)
    }

    private fun updateColorPaintGradient(isFg: Boolean){
        val colors = if(isFg){
            intArrayOf(
                Color.argb(state.fgEdge[0],state.fgEdge[1],state.fgEdge[2],state.fgEdge[3]),
                Color.argb(state.fgCenter[0],state.fgCenter[1],state.fgCenter[2],state.fgCenter[3]),
                Color.argb(state.fgEdge[0],state.fgEdge[1],state.fgEdge[2],state.fgEdge[3])
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

    private fun changeItemValue(isRight: Boolean, count : Int = 1) {
        // Page 1
        if(isSelected(0, 0)){
            swapWaveStyleName(isRight)
        }
        if(isSelected(0, 1)){
            state.speed = (((state.speed * 10).toInt() + isRight.select(1,-1)).toFloat() / 10.0f).coerceIn(0.1f, 5.0f)
            sendToNativeGL()
        }
        if(isSelected(0, 2)){
            state.isDayNight = !state.isDayNight
            sendToNativeGL()
        }
        if(isSelected(0, 3)){
            state.month += isRight.select(1, -1)
            if(state.month < -1) state.month = 12
            else if(state.month > 12) state.month = -1
            sendToNativeGL()
        }

        // Page 2
        if(isSelected(1, 0)) {
            state.bgTop[0] = chColor(state.bgTop[0], count, isRight)
            updateColorPaintGradient(false)
        }
        if(isSelected(1, 1)) {
            state.bgTop[1] = chColor(state.bgTop[1], count, isRight)
            updateColorPaintGradient(false)
        }
        if(isSelected(1, 2)) {
            state.bgTop[2] = chColor(state.bgTop[2], count, isRight)
            updateColorPaintGradient(false)
        }
        if(isSelected(1, 3)) {
            state.bgBottom[0] = chColor(state.bgBottom[0], count, isRight)
            updateColorPaintGradient(false)
        }
        if(isSelected(1, 4)) {
            state.bgBottom[1] = chColor(state.bgBottom[1], count, isRight)
            updateColorPaintGradient(false)
        }
        if(isSelected(1, 5)) {
            state.bgBottom[2] = chColor(state.bgBottom[2], count, isRight)
            updateColorPaintGradient(false)
        }
        if(isSelected(1, 6)){
            sendToNativeGL()
        }


        if(isSelected(2, 0)){
            state.fgEdge[0] = chColor(state.fgEdge[0], count, isRight)
            updateColorPaintGradient(true)
        }
        if(isSelected(2, 1)){
            state.fgEdge[1] = chColor(state.fgEdge[1], count, isRight)
            updateColorPaintGradient(true)
        }
        if(isSelected(2, 2)){
            state.fgEdge[2] = chColor(state.fgEdge[2], count, isRight)
            updateColorPaintGradient(true)
        }
        if(isSelected(2, 3)){
            state.fgEdge[3] = chColor(state.fgEdge[3], count, isRight)
            updateColorPaintGradient(true)
        }
        if(isSelected(2, 4)){
            state.fgCenter[0] = chColor(state.fgCenter[0], count, isRight)
            updateColorPaintGradient(true)
        }
        if(isSelected(2, 5)){
            state.fgCenter[1] = chColor(state.fgCenter[1], count, isRight)
            updateColorPaintGradient(true)
        }
        if(isSelected(2, 6)){
            state.fgCenter[2] = chColor(state.fgCenter[2], count, isRight)
            updateColorPaintGradient(true)
        }
        if(isSelected(2, 7)){
            state.fgCenter[3] = chColor(state.fgCenter[3], count, isRight)
            updateColorPaintGradient(true)
        }
        if(isSelected(2, 8)){
            sendToNativeGL()
        }
    }

    private val touchLast = PointF()
    /**
     * Disable movement after page change, so value in the new page will not accidentally changed
     * until user lift their touch
     */
    private var disableTouchMove = false

    override fun onTouch(a: PointF, b: PointF, act: Int) {
        when(act){
            MotionEvent.ACTION_MOVE -> {
                if(!disableTouchMove){
                    val len = (b - touchLast)
                    if(len.length() > 100.0f){

                        if(abs(len.x) > abs(len.y)){ // horizontal
                            val pageNum = pageNumber
                            val itemNum = selectedItemIndices[pageNum]
                            val canChangePage = lrChangePageAtOptionNum.find { it -> it.x == pageNum && it.y == itemNum } != null

                            if(canChangePage){
                                changePage(len.x >= 0.0f)
                                disableTouchMove = true
                            }else{
                                changeItemValue(len.x > 0.0f, 1)
                            }

                        } else { // vertical
                            changeItemIndex(len.y > 0.0f)
                        }
                        touchLast.set(b)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL  -> {
                touchLast.set(0.0f, 0.0f)
                disableTouchMove = false
            }
            MotionEvent.ACTION_DOWN -> {
                touchLast.set(b)
            }
        }
    }
}
