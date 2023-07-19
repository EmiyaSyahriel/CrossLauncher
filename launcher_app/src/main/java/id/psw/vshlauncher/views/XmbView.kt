package id.psw.vshlauncher.views

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Build
import android.os.Debug
import android.text.TextPaint
import android.util.AttributeSet
import android.view.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import id.psw.vshlauncher.*
import id.psw.vshlauncher.activities.XMB
import id.psw.vshlauncher.submodules.GamepadSubmodule
import id.psw.vshlauncher.typography.FontCollections
import java.util.*
import kotlin.ConcurrentModificationException
import kotlin.concurrent.thread
import kotlin.math.roundToInt

class XmbView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {
    companion object{
        const val TAG = "xmb.view"
    }

    class ScaleInfo {
        /** Scale to fit screen from inside */
        var fitScale = 1.0f
        /** Scale to fill screen from outside */
        var fillScale = 1.0f
        /** Raw screen / canvas rect */
        var screen : RectF = RectF(0.0f, 0.0f, 1280.0f, 720.0f)
        /** Raw screen / canvasval rect */
        var offsetScreen : RectF = RectF(0.0f, 0.0f, 1280.0f, 720.0f)
        /** Target rect to draw, with paddings outside target drawing rectangle */
        var viewport : RectF = RectF()
        /** Target rect to draw */
        val landTarget : RectF = RectF(0.0f,0.0f,1280.0f,720.0f)
        val portTarget : RectF = RectF(0.0f,0.0f,720.0f,1280.0f)
        //val target : RectF get() = (screen.width() > screen.height()).select(lTarget, pTarget)
        val target : RectF get() = (screen.width() > screen.height()).select(landTarget, portTarget)
    }

    var gamebootActive: Boolean = true
    var time = VshViewTimeData()
    var currentPage = VshViewPage.ColdBoot
    var state = VshViewStates()
    var scaling = ScaleInfo()
    var tempRect = RectF()
    var isHWAccelerated = false
    var useInternalWallpaper = true
    lateinit var gamepadNotifIcon : Bitmap
    val dummyPaint = Paint().apply {
        color = Color.GREEN
        textSize = 20.0f
    }
    var keygenActive = false

    var fpsLimit = 0L
    private lateinit var drawThread : Thread
    private var shouldKeepRenderThreadRunning = true
    private var isRenderThreadRunning = false
    internal var showDetailedMemory = false
    private fun doNothing(){}

    private fun drawThreadFunc(){
        if(!isRenderThreadRunning){
            isRenderThreadRunning = true

            // Wait for the render thread to run
            while(!holder.surface.isValid && shouldKeepRenderThreadRunning){
                doNothing()
                if(fpsLimit != 0L){
                    Thread.sleep(1000L/fpsLimit)
                }
            }

            // Now draw!
            while(holder.surface.isValid && shouldKeepRenderThreadRunning){
                val canvas = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    holder.lockHardwareCanvas()
                }else{
                    holder.lockCanvas()
                }
                isHWAccelerated = canvas.isHardwareAccelerated
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                xmbOnDraw(canvas)

                holder.unlockCanvasAndPost(canvas)

                if(fpsLimit != 0L){
                    Thread.sleep(1000L/fpsLimit)
                }

            }
            isRenderThreadRunning = false
        }
    }

    fun pauseRendering(){
        shouldKeepRenderThreadRunning = false
        try{
            drawThread.join()
        }catch(e:Exception){}
    }

    fun startDrawThread(){
        shouldKeepRenderThreadRunning = true
        drawThread = thread(start=true, isDaemon = true){ drawThreadFunc() }.apply { name = "XMB Render Thread" }
    }

    fun setReferenceScreenSize(width: Int, height: Int, writePref:Boolean){
        scaling.landTarget.set(0.0f, 0.0f, width * 1.0f, height * 1.0f)
        scaling.portTarget.set(0.0f, 0.0f, height * 1.0f, width * 1.0f)

        if(writePref){
            val dSize = (width shl 16) or height
            context.vsh.pref.edit().putInt(PrefEntry.REFERENCE_RESOLUTION, dSize).apply()
        }
    }

    private fun loadPreferences(){
        val vsh = context.vsh
        val pref = vsh.pref

        state.crossMenu.layoutMode = when(pref.getInt(PrefEntry.MENU_LAYOUT, 0)){
            0 -> XMBLayoutType.PS3
            1 -> XMBLayoutType.PSP
            2 -> XMBLayoutType.Bravia
            3 -> XMBLayoutType.PSX
            else -> XMBLayoutType.PS3
        }

        val defSize = (1280 shl 16) or 720
        val refSize = pref.getInt(PrefEntry.REFERENCE_RESOLUTION, defSize)
        setReferenceScreenSize(refSize shr 16, refSize and 0xFFFF, false)

        state.coldBoot.hideEpilepsyWarning = pref.getInt(PrefEntry.DISABLE_EPILEPSY_WARNING, 0) == 1
        state.crossMenu.dimOpacity = pref.getInt(PrefEntry.BACKGROUND_DIM_OPACITY, 0)
        state.crossMenu.dateTimeFormat =
            pref.getString(PrefEntry.DISPLAY_STATUS_BAR_FORMAT, state.crossMenu.dateTimeFormat)
                ?: state.crossMenu.dateTimeFormat
        state.gameBoot.defaultSkip = pref.getInt(PrefEntry.SKIP_GAMEBOOT, 0) != 0
        vsh.showLauncherFPS = pref.getInt(PrefEntry.SHOW_LAUNCHER_FPS, 0) != 0
        showDetailedMemory = pref.getInt(PrefEntry.SHOW_DETAILED_MEMORY, 0) != 0
    }

    init {
        val gpIconDr = ResourcesCompat.getDrawable(context.resources, R.drawable.category_games, null)
        gamepadNotifIcon = gpIconDr!!.toBitmap(50,50)
        setZOrderOnTop(true)
        contentDescription = context.getString(R.string.xmb_view_content_description)
        holder.setFormat(PixelFormat.TRANSPARENT)
        loadPreferences()
        DrawExtension.init(context.vsh)
    }

    private fun adaptScreenSize(){
        if(!fitsSystemWindows){  fitsSystemWindows = true }

        scaling.apply {
            screen.right = width.toFloat()
            screen.bottom = height.toFloat()

            val arW = screen.width() / target.width()
            val arH = screen.height() / target.height()
            fillScale = arW.coerceAtLeast(arH)
            fitScale = arW.coerceAtMost(arH)

            val svW = (1.0f / fitScale) * screen.width()
            val svH = (1.0f / fitScale) * screen.height()

            val svWd = (svW - target.width()) / 2.0f
            val svHd = (svH - target.height()) / 2.0f

            viewport.left = -svWd
            viewport.top = -svHd
            viewport.right = target.width() + svWd
            viewport.bottom = target.height() + svHd
        }
    }

    fun switchPage(view:VshViewPage){
        System.gc() // Garbage Collect Every Page Change
        //if(currentPage != view){
        val nope : () -> Unit = {

        }

        when(currentPage){
            VshViewPage.ColdBoot -> cbEnd()
            VshViewPage.MainMenu -> menuEnd()
            VshViewPage.GameBoot -> gbEnd()
            VshViewPage.Dialog -> dlgEnd()
            else -> nope()
        }
        currentPage = view
        when(currentPage){
            VshViewPage.ColdBoot -> cbStart()
            VshViewPage.MainMenu -> menuStart()
            VshViewPage.GameBoot -> gbStart()
            VshViewPage.Dialog -> dlgStart()
            else -> nope()
        }
        //}
        System.gc() // Garbage Collect Every Page Change
    }

    private val notificationBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(192,20,20,20)
    }

    private val notificationTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 15.0f
        color = Color.WHITE
        textAlign = Paint.Align.LEFT
        typeface = FontCollections.masterFont
    }

    private val notificationTitleTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 20.0f
        // typeface = Typeface.create(typeface, Typeface.BOLD)
        textAlign = Paint.Align.LEFT
        typeface = FontCollections.masterFont
    }

    private val notificationRectBuffer = RectF()

    fun openItemMenu(open: Boolean = true) {
        state.itemMenu.isDisplayed = open
        state.itemMenu.selectedIndex = context.vsh.hoveredItem?.setMenuOpened(open) ?: 0

    }

    fun drawNotifications(ctx:Canvas){
        var top = 20.0f
        try{
            context.vsh.getUpdatedNotification().forEach {
                val textLeft = (it.icon != null).select(120.0f, 10.0f)

                val descText = notificationTextPaint.wrapText(it.desc, 530f - textLeft)
                val notifHeight = kotlin.math.max((it.icon == null).select(10f,  120f), 10f + ((descText.lines().size + 3) * notificationTextPaint.textSize) + 10f)
                val notifWidth = 530f
                val notifLeft = (1.0f - kotlin.math.min(it.remainingTime / 0.15f, 1.0f))
                    .toLerp(scaling.target.right - 540f, scaling.viewport.right)

                notificationRectBuffer.set(notifLeft, top, notifLeft + notifWidth, top + notifHeight)
                ctx.drawRoundRect(notificationRectBuffer, 10.0f, notificationBgPaint)
                val tLeft = notificationRectBuffer.left + textLeft
                ctx.drawText(it.title, tLeft, notificationRectBuffer.top + 10.0f, notificationTitleTextPaint, 1.0f)

                descText.lines().forEachIndexed { i, s ->
                    val tTop = notificationRectBuffer.top + notificationTitleTextPaint.textSize + 15.0f +
                            (notificationTextPaint.textSize * i)
                    ctx.drawText(s, tLeft, tTop, notificationTextPaint, 1.0f)
                }

                val cIcon = it.icon
                if(cIcon != null){
                    val cX = notificationRectBuffer.left + 60f
                    val cY = notificationRectBuffer.centerY()
                    notificationRectBuffer.set(
                        cX - 40f,
                        cY - 40f,
                        cX + 40f,
                        cY + 40f
                    )
                    ctx.drawBitmap(cIcon, null, notificationRectBuffer, null)
                }

                top += notifHeight + 5.0f
            }
        }catch(e:ConcurrentModificationException){}
    }

    private val touchStartPointF = PointF()
    private val touchCurrentPointF = PointF()
    private var lastTouchAction = MotionEvent.ACTION_UP

    fun onTouchScreen(start : PointF, current: PointF, action:Int){
        touchStartPointF.set(start)
        touchCurrentPointF.set(current)
        lastTouchAction = action
        val call : (PointF, PointF, Int) -> Unit = when(currentPage){
            VshViewPage.MainMenu -> ::menuOnTouchScreen
            VshViewPage.Dialog -> ::dlgOnTouchScreen
            VshViewPage.GameBoot -> ::cbOnTouchScreen
            VshViewPage.ColdBoot -> ::gbOnTouchScreen
            VshViewPage.HomeScreen -> ::homeOnTouchScreen
        }
        call(start, current, action)
    }

    fun onGamepadInput(key:GamepadSubmodule.Key, isDown:Boolean) : Boolean{
        return when(currentPage){
            VshViewPage.MainMenu -> ::menuOnGamepad
            VshViewPage.Dialog -> ::dlgOnGamepad
            VshViewPage.ColdBoot -> ::cbOnGamepad
            VshViewPage.GameBoot -> ::gbOnGamepad
            VshViewPage.HomeScreen -> ::homeOnGamepad
        }(key, isDown)
    }

    fun onCharacterInput(ch:Char){

    }

    fun onUpdate(){
        context.vsh.itemOffsetX = (time.deltaTime * 10.0f).coerceIn(0.0f, 1.0f).toLerp(context.vsh.itemOffsetX, 0.0f)
        context.vsh.itemOffsetY = (time.deltaTime * 10.0f).coerceIn(0.0f, 1.0f).toLerp(context.vsh.itemOffsetY, 0.0f)
        context.vsh.updateBatteryInfo()
    }

    private val fpsRect = Rect()
    private val fpsRectF = RectF()
    private val memFmtSb = StringBuilder()
    private val memFmtNames = arrayOf("PSS", "PD", "SD", "PC", "SC", "??", "E", "??", "E", "??", "E", "??")
    private val memInfo = Debug.MemoryInfo()
    private var memInfoThreadKeepRunning = false

    private fun memFmt(name:String, vararg names:Int) : String {
        memFmtSb.clear()
        memFmtSb.append(name).append(" - ")
        names.forEachIndexed { i, v ->
            memFmtSb.append(memFmtNames[i]).append(":").append((v * 1000L).asBytes()).append(" | ")
        }
        return memFmtSb.toString()
    }

    private val memInfoThread = Thread {
        memInfoThreadKeepRunning = true
        while(memInfoThreadKeepRunning){
            Debug.getMemoryInfo(memInfo)
            Thread.sleep(30L)
        }
    }

    private fun drawFPS(ctx:Canvas){
        val fps = (1.0f / time.deltaTime).roundToInt()
        val fpsTxt = "[FPS] $fps FPS | ${(time.deltaTime * 1000).roundToInt()} ms"
        dummyPaint.color = Color.GREEN
        dummyPaint.style = Paint.Style.FILL
        dummyPaint.setShadowLayer(2.0f, 2.0f, 2.0f, Color.BLACK)

        val isTV = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
        }else{
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEVISION)
        }

        val isTvTxt = isTV.select("Device : TV","")

        val nMemSz = "ANDROID NATIVE MEM - TOTAL:${Debug.getNativeHeapSize().asBytes()} | USED:${Debug.getNativeHeapAllocatedSize().asBytes()} | FREE:${Debug.getNativeHeapFreeSize().asBytes()}"


        val arr = arrayListOf<String>()
        arr.add(fpsTxt)
        arr.add(nMemSz)

        if(showDetailedMemory){
            if(!memInfoThreadKeepRunning){
                memInfoThread.start()
            }

            val jdMemSz = memFmt("JVM RUNTIME MEM", memInfo.dalvikPss, memInfo.dalvikPrivateDirty, memInfo.dalvikSharedDirty)
            val jnMemSz = memFmt("JVM NATIVE MEM", memInfo.nativePss, memInfo.nativePrivateDirty, memInfo.nativeSharedDirty)
            val joMemSz = memFmt("JVM OTHER MEM", memInfo.otherPss, memInfo.otherPrivateDirty ,memInfo.otherSharedDirty)
            val jtMemSz = memFmt("JVM TOTAL MEM", memInfo.totalPss, memInfo.totalPrivateDirty, memInfo.totalSharedDirty, memInfo.totalPrivateClean, memInfo.totalSharedClean)

            arr.add(jdMemSz)
            arr.add(jnMemSz)
            arr.add(joMemSz)
            arr.add(jtMemSz)
        }
        arr.add(isTvTxt)

        arr.forEachIndexed { i, it ->
            ctx.drawText(it, 20f, 50f + (i * (dummyPaint.textSize * 1.25f)), dummyPaint, 1.0f)
        }

        dummyPaint.removeShadowLayer()
    }

    override fun onDetachedFromWindow() {
        memInfoThreadKeepRunning = false
        super.onDetachedFromWindow()
    }

    fun xmbOnDraw(canvas: Canvas?) {
        adaptScreenSize()
        tickTime()
        onUpdate()
        if(canvas != null) {
            if(useInternalWallpaper){

            }

            if(currentPage != VshViewPage.HomeScreen){
                val dimAlpha = ((state.crossMenu.dimOpacity / 10.0f) * 255).toInt()
                canvas.drawARGB(dimAlpha, 0,0,0)
            }

            canvas.withScale(scaling.fitScale, scaling.fitScale, 0.0f, 0.0f) {
                canvas.withTranslation(-scaling.viewport.left, -scaling.viewport.top){
                    when(currentPage){
                        VshViewPage.ColdBoot -> ::cbRender
                        VshViewPage.MainMenu -> ::menuRender
                        VshViewPage.GameBoot -> ::gbRender
                        VshViewPage.Dialog -> ::dlgRender
                        VshViewPage.HomeScreen -> ::homeRender
                    }(canvas)

                    drawDebugLocation(canvas, context.xmb)
                    drawKeygen(canvas)
                    drawNotifications(canvas)
                    if(context.vsh.showLauncherFPS) drawFPS(canvas)
                }
            }
        }
    }

    private fun drawKeygen(ctx: Canvas) {
        if(keygenActive) return
        val cal = Calendar.getInstance()
        val mon = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        // April Mob wkwkwkwkwkwk
        if(day == 1 && mon == 3){
            dummyPaint.textAlign = Paint.Align.LEFT
            dummyPaint.color = Color.WHITE
            dummyPaint.alpha = 64

            val xs = scaling.viewport.right - 200.0f
            val ys = scaling.viewport.bottom - 50.0f
            dummyPaint.textSize = 15.0f
            ctx.drawText(context.getString(R.string.settings_system_test_display_title), xs, ys, dummyPaint)
            dummyPaint.textSize = 10.0f
            ctx.drawText(context.getString(R.string.settings_system_test_display_desc), xs, ys + 20.0f, dummyPaint)
            dummyPaint.alpha = 255
            dummyPaint.textSize = 15.0f
        }
    }

    private fun drawDebugLocation(ctx: Canvas, xmb: XMB) {
        if(lastTouchAction == MotionEvent.ACTION_DOWN || lastTouchAction == MotionEvent.ACTION_MOVE){
            dummyPaint.style= Paint.Style.FILL
            dummyPaint.color = Color.argb(128,255,255,255)
            ctx.drawCircle(touchCurrentPointF.x, touchCurrentPointF.y, 10.0f, dummyPaint)
            ctx.drawCircle(touchStartPointF.x, touchStartPointF.y, 10.0f, dummyPaint)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyUp(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        startDrawThread()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        onSizeChanged(width, height, this.width, this.height);
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        pauseRendering()
    }

}