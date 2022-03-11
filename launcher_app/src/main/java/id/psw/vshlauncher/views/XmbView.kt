package id.psw.vshlauncher.views

import android.content.Context
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
import id.psw.vshlauncher.activities.XMB
import id.psw.vshlauncher.submodules.GamepadSubmodule
import id.psw.vshlauncher.views.dialogviews.TestDialogView
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.roundToInt

class XmbView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    class ScaleInfo {
        /** Scale to fit screen from inside */
        var fitScale = 1.0f
        /** Scale to fill screen from outside */
        var fillScale = 1.0f
        /** Raw screen / canvas rect */
        var screen : RectF = RectF(0.0f, 0.0f, 1280.0f, 720.0f)
        /** Raw screen / canvas rect */
        var offsetScreen : RectF = RectF(0.0f, 0.0f, 1280.0f, 720.0f)
        /** Target rect to draw, with paddings outside target drawing rectangle */
        var viewport : RectF = RectF()
        /** Target rect to draw */
        val landTarget : RectF = RectF(0.0f,0.0f,1280.0f,720.0f)
        val portTarget : RectF = RectF(0.0f,0.0f,720.0f,1280.0f)
        //val target : RectF get() = (screen.width() > screen.height()).select(lTarget, pTarget)
        val target : RectF get() = (screen.width() > screen.height()).select(landTarget, portTarget)
    }

    var time = VshViewTimeData()
    var currentPage = VshViewPage.ColdBoot
    var state = VshViewStates()
    var scaling = ScaleInfo()
    var tempRect = RectF()
    var isHWAccelerated = false
    var useInternalWallpaper = false
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

        state.coldBoot.hideEpilepsyWarning = pref.getBoolean(PrefEntry.DISABLE_EPILEPSY_WARNING, false)
    }

    init {
        val gpIconDr = ResourcesCompat.getDrawable(context.resources, R.drawable.category_games, null)
        gamepadNotifIcon = gpIconDr!!.toBitmap(50,50)
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSPARENT)
        loadPreferences()
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
        //if(currentPage != view){
            when(currentPage){
                VshViewPage.ColdBoot -> cbEnd()
                VshViewPage.MainMenu -> menuEnd()
                VshViewPage.GameBoot -> gbEnd()
                VshViewPage.Dialog -> dlgEnd()
            }
            currentPage = view
            when(currentPage){
                VshViewPage.ColdBoot -> cbStart()
                VshViewPage.MainMenu -> menuStart()
                VshViewPage.GameBoot -> gbStart()
                VshViewPage.Dialog -> dlgStart()
            }
        //}
    }

    private val notificationBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(192,20,20,20)
    }

    private val notificationTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 15.0f
        color = Color.WHITE
        textAlign = Paint.Align.LEFT
    }

    private val notificationTitleTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 20.0f
        // typeface = Typeface.create(typeface, Typeface.BOLD)
        textAlign = Paint.Align.LEFT
    }

    private val notificationRectBuffer = RectF()

    fun drawNotifications(ctx:Canvas){
        var top = 20.0f
        context.vsh.getUpdatedNotification().forEach {
            val textLeft = (it.icon != null).select(120.0f, 10.0f)

            val descText = notificationTextPaint.wrapText(it.desc, 530f - textLeft)
            val notifHeight = kotlin.math.max((it.icon == null).select(10f,  120f), 10f + ((descText.lines().size + 1) * notificationTextPaint.textSize) + 10f)
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
    }

    private fun isKeyDownOrRepeat(key:GamepadSubmodule.Key): Boolean{
        val downTime =  VSH.Gamepad.getDownTime(key)
        val validDownTime = (downTime % 0.2f) > 0.1f
        return VSH.Gamepad.getKeyDown(key) || (downTime > 0.5f && validDownTime)
    }

    private fun updateGamepadList(){
        val ids = InputDevice.getDeviceIds()
        for(id in ids){
            if(!VSH.Gamepad.gamePads.containsKey(id)){

                val gm = InputDevice.getDevice(id)
                if(gm.sources hasFlag InputDevice.SOURCE_GAMEPAD){
                    val desc = "${gm.name} (${Integer.toHexString(gm.vendorId).padStart(4,'0')} ${Integer.toHexString(gm.productId).padStart(4,'0')})"
                    context.vsh.postNotification(gamepadNotifIcon, "New Gamepad Connected", desc)
                    VSH.Gamepad.gamePads[id] = GamepadSubmodule.GamePadInfo(
                        gm.id,
                        gm.productId,
                        gm.vendorId,
                        gm.name
                    )
                }
            }
        }

        for(id in VSH.Gamepad.gamePads){
            if(!ids.contains(id.key)){
                context.vsh.postNotification(gamepadNotifIcon, "Gamepad Disconnected", id.value.displayName)
                VSH.Gamepad.gamePads.remove(id.key)
            }
        }
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
        }
        call(start, current, action)
    }

    fun onUpdate(){
        updateGamepadList()
        if(currentPage == VshViewPage.MainMenu){

            context.vsh.itemOffsetX = (time.deltaTime * 10.0f).coerceIn(0.0f, 1.0f).toLerp(context.vsh.itemOffsetX, 0.0f)
            context.vsh.itemOffsetY = (time.deltaTime * 10.0f).coerceIn(0.0f, 1.0f).toLerp(context.vsh.itemOffsetY, 0.0f)
            when {
                isKeyDownOrRepeat(GamepadSubmodule.Key.PadL) -> {
                    if(!state.itemMenu.isDisplayed){
                        if(context.vsh.isInRoot){
                            context.vsh.moveCursorX(false)
                        }else{
                            context.vsh.backStep()
                        }
                    }
                }
                isKeyDownOrRepeat(GamepadSubmodule.Key.PadR) && context.vsh.isInRoot -> {
                    if(!state.itemMenu.isDisplayed){
                        context.vsh.moveCursorX(true)
                    }
                }
                isKeyDownOrRepeat(GamepadSubmodule.Key.PadU) -> {
                    if(state.itemMenu.isDisplayed){
                        menuMoveItemMenuCursor(false)
                    }else{
                        context.vsh.moveCursorY(false)
                    }
                }
                isKeyDownOrRepeat(GamepadSubmodule.Key.PadD) -> {
                    if(state.itemMenu.isDisplayed){
                        menuMoveItemMenuCursor(true)
                    }else{
                        context.vsh.moveCursorY(true)
                    }
                }
                VSH.Gamepad.getKeyDown(GamepadSubmodule.Key.Triangle) -> {
                    val item = context.vsh.hoveredItem
                    if(state.itemMenu.isDisplayed){
                        state.itemMenu.isDisplayed = false
                    }else{
                        if(item != null){
                            if(item.hasMenu){
                                state.itemMenu.isDisplayed = true
                            }
                        }
                    }
                }
                VSH.Gamepad.getKeyDown(GamepadSubmodule.Key.Square) -> {
                    if(context.vsh.isInRoot){
                        context.vsh.doCategorySorting()
                        state.crossMenu.sortHeaderDisplay = 5.0f
                    }
                }
                VSH.Gamepad.getKeyDown(GamepadSubmodule.Key.Confirm) -> {
                    if(state.itemMenu.isDisplayed) {
                        menuStartItemMenu()
                        state.itemMenu.isDisplayed = false
                    }else{
                        context.vsh.launchActiveItem()
                    }
                }
                VSH.Gamepad.getKeyDown(GamepadSubmodule.Key.Cancel) -> {
                    if(state.itemMenu.isDisplayed){
                        state.itemMenu.isDisplayed = false
                    }else{
                        context.vsh.backStep()
                    }
                }
            }

            if(swapLayoutType){
                state.crossMenu.layoutMode = when(state.crossMenu.layoutMode){
                    XMBLayoutType.PS3 -> XMBLayoutType.PSP
                    XMBLayoutType.PSP -> XMBLayoutType.Bravia
                    else -> XMBLayoutType.PS3
                }
                state.crossMenu.menuScaleTime = 1.0f
                swapLayoutType = false
            }
        }else if(currentPage == VshViewPage.GameBoot){
            if(isKeyDownOrRepeat(GamepadSubmodule.Key.Cross)){
                switchPage(VshViewPage.MainMenu)
            }
        }else if(currentPage == VshViewPage.ColdBoot){
            if(isKeyDownOrRepeat(GamepadSubmodule.Key.Triangle)){
                showDialog(TestDialogView(context.vsh))
            }
        }
    }

    private val fpsRect = Rect()
    private val fpsRectF = RectF()
    private fun drawFPS(ctx:Canvas){
        val fps = (1.0f / time.deltaTime).roundToInt()
        val fpstxt = "[FPS] $fps FPS | ${(time.deltaTime * 1000).roundToInt()} ms"
        val memtotald = VSH.dbgMemInfo.totalPrivateDirty + VSH.dbgMemInfo.totalSharedDirty
        val memtotalc = VSH.dbgMemInfo.totalPrivateClean + VSH.dbgMemInfo.totalSharedClean
        val memtxt = "[MEMORY] Usage: ${memtotald}kB (${memtotalc}kB Clean) / Total ${VSH.actMemInfo.totalMem/1000}kB "
        dummyPaint.color = Color.GREEN
        dummyPaint.style = Paint.Style.FILL
        dummyPaint.setShadowLayer(2.0f, 2.0f, 2.0f, Color.BLACK)

        arrayOf(
            fpstxt,
            memtxt
        ).forEachIndexed { i, it ->
            ctx.drawText(it, 20f, 50f + (i * dummyPaint.textSize), dummyPaint, 1.0f)
        }

        dummyPaint.removeShadowLayer()
    }



    fun xmbOnDraw(canvas: Canvas?) {
        adaptScreenSize()
        tickTime()
        onUpdate()
        if(canvas != null) {
            if(useInternalWallpaper){
                // TODO: Draw Internal Wallpaper
            }
            canvas.withScale(scaling.fitScale, scaling.fitScale, 0.0f, 0.0f) {
                canvas.withTranslation(-scaling.viewport.left, -scaling.viewport.top){
                    when(currentPage){
                        VshViewPage.ColdBoot -> cbRender(canvas)
                        VshViewPage.MainMenu -> menuRender(canvas)
                        VshViewPage.GameBoot -> gbRender(canvas)
                        VshViewPage.Dialog -> dlgRender(canvas)
                    }

                    drawDebugLocation(canvas, context.xmb)
                    drawKeygen(canvas)
                    drawNotifications(canvas)
                    if(context.vsh.showLauncherFPS) drawFPS(canvas)
                }
            }
        }
        VSH.Gamepad.update(time.deltaTime)
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