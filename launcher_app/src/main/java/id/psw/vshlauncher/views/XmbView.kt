package id.psw.vshlauncher.views

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.os.Build
import android.text.TextPaint
import android.util.AttributeSet
import android.view.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import id.psw.vshlauncher.*
import id.psw.vshlauncher.submodules.PadKey
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.screens.*
import id.psw.vshlauncher.views.widgets.*
import java.util.*
import kotlin.ConcurrentModificationException
import kotlin.concurrent.thread

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

    var time = TimeData()
    var scaling = ScaleInfo()
    var tempRect = RectF()
    var isHWAccelerated = false
    var useInternalWallpaper = true
    var gamepadNotifIcon : Bitmap
    val dummyPaint = Paint().apply {
        color = Color.GREEN
        textSize = 20.0f
    }
    var keygenActive = false

    var fpsLimit = 0L
    private lateinit var drawThread : Thread
    private var shouldKeepRenderThreadRunning = true
    private var isRenderThreadRunning = false

    private fun doNothing(){}

    class Screens(v:XmbView) {
        val mainMenu = XmbMainMenu(v)
        val coldBoot = XmbColdboot(v)
        val gameBoot = XmbGameboot(v)
        val dialog = XmbDialog(v)
        val idle = XmbIdleScreen(v)
    }

    class Widgets(v:XmbView){
        val sideMenu = XmbSideMenu(v)
        val debugInfo = XmbDebugInfo(v)
        val debugTouch = XmbDebugTouch(v)
        val analogClock = XmbAnalogClock(v)
        val statusBar = XmbStatusBar(v)
        val searchQuery = XmbSearchQuery(v)
    }

    lateinit var screens : Screens
    lateinit var widgets : Widgets

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

    private fun checkCanvasHwAcceleration(){
        if(!isHWAccelerated){
            context.vsh.postNotification(null, context.getString(R.string.no_hwaccel_warning_title),
                context.getString(R.string.no_hwaccel_warning_desc)
            )
        }
    }

    private var screenOrientationWarningPosted = false
    private fun postPortraitScreenOrientationWarning() {
        if(!screenOrientationWarningPosted){
            context.vsh.postNotification(null,
                context.getString(R.string.screen_portrait_warning_title),
                context.getString(R.string.screen_portrait_warning_desc)
            )
            screenOrientationWarningPosted = true
        }
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
            M.pref.set(PrefEntry.REFERENCE_RESOLUTION, dSize)
        }
    }

    private fun loadPreferences(){
        val vsh = context.vsh
        val pref = vsh.M.pref

        screens.mainMenu.layoutMode = when(pref.get(PrefEntry.MENU_LAYOUT, 0)){
            0 -> XmbLayoutType.PS3
            1 -> XmbLayoutType.PSP
            2 -> XmbLayoutType.Bravia
            3 -> XmbLayoutType.PSX
            else -> XmbLayoutType.PS3
        }

        val defSize = (1280 shl 16) or 720
        val refSize = pref.get(PrefEntry.REFERENCE_RESOLUTION, defSize)
        setReferenceScreenSize(refSize shr 16, refSize and 0xFFFF, false)

        screens.coldBoot.hideEpilepsyWarning = pref.get(PrefEntry.DISABLE_EPILEPSY_WARNING, 0) == 1
        screens.mainMenu.dimOpacity = pref.get(PrefEntry.BACKGROUND_DIM_OPACITY, 0)
        widgets.statusBar.dateTimeFormat =
            pref.get(PrefEntry.DISPLAY_STATUS_BAR_FORMAT, widgets.statusBar.dateTimeFormat)
        screens.gameBoot.defaultSkip = pref.get(PrefEntry.SKIP_GAMEBOOT, 0) != 0
        widgets.debugInfo.showLauncherFPS = pref.get(PrefEntry.SHOW_LAUNCHER_FPS, 0) != 0
        widgets.debugInfo.showDetailedMemory = pref.get(PrefEntry.SHOW_DETAILED_MEMORY, 0) != 0
    }

    init {
        val gpIconDr = ResourcesCompat.getDrawable(context.resources, R.drawable.category_games, null)
        gamepadNotifIcon = gpIconDr!!.toBitmap(50,50)
        setZOrderOnTop(true)
        contentDescription = context.getString(R.string.xmb_view_content_description)
        holder.setFormat(PixelFormat.TRANSPARENT)
        DrawExtension.init(context.vsh)
    }

    private var onceStarted = false
    private fun start(){
        screens = Screens(this)
        widgets = Widgets(this)

        activeScreen = screens.idle
        switchScreen(context.xmb.skipColdBoot.select(screens.mainMenu, screens.coldBoot))

        loadPreferences()
        checkCanvasHwAcceleration()
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

    lateinit var activeScreen : XmbScreen

    fun switchScreen(nScreen:XmbScreen){
        System.gc() // Garbage Collect Every Page Change

        activeScreen.end()
        activeScreen = nScreen
        activeScreen.start()

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

    fun showSideMenu(open: Boolean = true) {
        widgets.sideMenu.isDisplayed = open
        widgets.sideMenu.selectedIndex = context.vsh.hoveredItem?.setMenuOpened(open) ?: 0
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
        if(widgets.sideMenu.isDisplayed){
            widgets.sideMenu.onTouchScreen(start, current, action)
        }else{
            activeScreen.onTouchScreen(start, current, action)
        }
    }

    fun onGamepadInput(key: PadKey, isDown:Boolean) : Boolean{
        if(widgets.sideMenu.isDisplayed){
            return widgets.sideMenu.onGamepadInput(key, isDown)
        }
        return activeScreen.onGamepadInput(key, isDown)
    }

    fun onUpdate(){
        context.vsh.itemOffsetX = (time.deltaTime * 10.0f).coerceIn(0.0f, 1.0f).toLerp(context.vsh.itemOffsetX, 0.0f)
        context.vsh.itemOffsetY = (time.deltaTime * 10.0f).coerceIn(0.0f, 1.0f).toLerp(context.vsh.itemOffsetY, 0.0f)
        context.vsh.updateBatteryInfo()
    }

    override fun onDetachedFromWindow() {
        widgets.debugInfo.memInfoThreadKeepRunning = false
        super.onDetachedFromWindow()
    }

    fun xmbOnDraw(canvas: Canvas?) {
        if(!onceStarted){
            onceStarted = true
            start()
        }

        adaptScreenSize()
        time.tickTime(this)
        onUpdate()
        canvas?.withScale(scaling.fitScale, scaling.fitScale, 0.0f, 0.0f) {
            canvas.withTranslation(-scaling.viewport.left, -scaling.viewport.top){

                if(activeScreen != screens.idle){
                    val dimAlpha = ((screens.mainMenu.dimOpacity / 10.0f) * 255).toInt()
                    canvas.drawARGB(dimAlpha, 0,0,0)
                }

                activeScreen.render(canvas)

                drawNotifications(canvas)
                drawKeygen(canvas)
                widgets.sideMenu.render(canvas)

                widgets.debugInfo.render(canvas)
                widgets.debugTouch.render(canvas)

            }
        }
    }

    /** Shortcut to [XmbDialog.showDialog] */
    fun showDialog(dlg : XmbDialogSubview){
        screens.dialog.showDialog(dlg)
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

    override fun surfaceCreated(holder: SurfaceHolder) {
        startDrawThread()
    }

    private var _lastOrientation : Int = Configuration.ORIENTATION_UNDEFINED

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

        val orientation = when {
            width < height -> Configuration.ORIENTATION_PORTRAIT
            width > height -> Configuration.ORIENTATION_LANDSCAPE
            else -> Configuration.ORIENTATION_UNDEFINED
        }

        if(orientation != _lastOrientation){
            when(orientation){
                Configuration.ORIENTATION_PORTRAIT ->
                {
                    postPortraitScreenOrientationWarning()
                }
                else-> {

                }
            }
        }
        _lastOrientation = orientation

        onSizeChanged(width, height, this.width, this.height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        pauseRendering()
    }

}