package id.psw.vshlauncher.views

import android.annotation.SuppressLint
import android.graphics.*
import android.util.Log
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.customtypes.SSADrawing
import id.psw.vshlauncher.getRect
import id.psw.vshlauncher.icontypes.XMBIcon
import id.psw.vshlauncher.icontypes.XMBRootIcon
import id.psw.vshlauncher.staticdata.StaticData
import id.psw.vshlauncher.toLerp
import id.psw.vshlauncher.views.VshServer.Paints.itemSubtitleSelected
import id.psw.vshlauncher.views.VshServer.Paints.itemTitleSelected
import java.text.SimpleDateFormat
import java.util.*
import kotlin.ConcurrentModificationException
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.max

// TODO: Finish this
object VshServer {

    const val TAG = "VshServer"

    enum class InputKeys {
        DPadU, DPadD, DPadL, DPadR,
        Sort, Back, Select, Menu
    }

    fun recalculateClockRect() {
        // TODO("Not yet implemented")
    }

    var density = 0.0f
    var scaledDensity = 0.0f
    var width = 0.0f
    var height = 0.0f
    var refWidth = 1520.0f
    var refHeight = 720.0f
    var refSafeWidth = 960.0f
    var overalScale = 1.0f
    val scaledScreenWidth  get()= width / calculatedScale
    val scaledScreenHeight get()= height / calculatedScale
    val root : XMBRootIcon = XMBRootIcon()
    var showDesktop = false
    var coldBootTime = 0.0f
    var gameBootTime = 0.0f
    var depth = 1

    // region Extension
    private fun PointF.scaleToLocal() : PointF = PointF(this.x * calculatedScale, this.y * calculatedScale)
    private fun PointF.inverseScaleToLocal() : PointF = PointF(this.x / calculatedScale, this.y / calculatedScale)
    private fun Canvas.drawText(string: String, x:Float, y:Float, paint:Paint, v:Float){
        drawText(string, x, y + (paint.textSize * v), paint)
    }
    private operator fun PointF.times(calculatedScale: Float): PointF = PointF(x * calculatedScale, y * calculatedScale)
    // endregion

    object Paints {
        var backgroundColor = Color.argb(255,0,0,0)
        var backgroundAlpha = 0.5f
        private var backgroundLerpAlpha = 0.0f
        var vshBackground : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { }
        var itemTitleSelected :Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 30f
            color=  Color.WHITE
            setShadowLayer(10f, 0f,0f,Color.WHITE)
        }
        var itemTitleUnselected :Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 30f
            color=  Color.GRAY
        }
        var categoryTitleSelected :Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 20f
            color=  Color.WHITE
            setShadowLayer(5f, 0f,0f,Color.WHITE)
        }
        var categoryTitleUnselected :Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 20f
            color=  Color.GRAY
        }
        var statusPaint :Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 25f
            color=  Color.WHITE
        }
        var itemSubtitleSelected :Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 20f
            color=  Color.WHITE
            setShadowLayer(5f, 0f,0f,Color.WHITE)
        }
        var itemSubtitleUnselected :Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 20f
            color=  Color.GRAY
        }

        fun setFont(tf: Typeface){
            arrayOf(itemSubtitleSelected, itemSubtitleUnselected, itemTitleSelected, itemTitleUnselected).forEach {
                it.typeface = tf
            }
        }

        fun updatePaints()
        {
            backgroundLerpAlpha = Time.deltaTime .toLerp(backgroundLerpAlpha, backgroundAlpha).coerceIn(0f, 1f)
            vshBackground.color = Color.argb(
                backgroundLerpAlpha.toLerp(0,255),
                Color.red(backgroundColor),
                Color.green(backgroundColor),
                Color.blue(backgroundColor)
            )
        }
    }

    object Time {
        private var lastTime = 0L
        var deltaTime = 0f
        var currentTime = 0f
        fun updateTime(){
            val cmillis = System.currentTimeMillis()
            deltaTime = (cmillis - lastTime) / 1000f
            lastTime = cmillis
            // avoiding overflow
            currentTime = (currentTime + deltaTime) % 131072f
        }
    }

    object xMath{
        fun max(vararg numbers: Float):Float{
            var retval = numbers[0]
            numbers.forEach { if(it > retval) retval = it }
            return retval
        }

        fun min(vararg numbers: Float):Float{
            var retval = numbers[0]
            numbers.forEach { if(it < retval) retval = it }
            return retval
        }
        fun maxAbs(vararg numbers: Float):Float{
            var retval = abs(numbers[0])
            numbers.forEach { if(abs(it) > abs(retval)) retval = abs(it) }
            return retval
        }

        fun minAbs(vararg numbers: Float):Float{
            var retval = abs(numbers[0])
            numbers.forEach { if(abs(it) < abs(retval)) retval = abs(it) }
            return retval
        }

        fun lerp(a: Float, b: Float, t: Float): Float = a + ((b - a) * t)
    }

    object BootScreen{

        fun lGameBoot(canvas:Canvas, deltaTime: Float){

        }

        fun lColdboot(canvas:Canvas, deltaTime: Float){

        }
    }

    object ContextMenu{
        var visible = true
        fun switchVisibility(){
            visible = !visible
        }

        fun lContextMenu(canvas:Canvas){

        }
    }

    object StatusBar {
        var hide = false
        var showOperatorName = true
        var operatorName = "No Operator"
        var use24Format = false
        var clockExpandInfo = "Some info here --"
        val shouldClockExpanded : Boolean get() = clockExpandInfo.isNotBlank()
        var isLoading = true
        var animateLoadClock = true
        val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply{
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply{
            color = Color.argb(0x88,0xFF,0xFF,0xFF)
            style = Paint.Style.FILL
        }
        val clockBackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply{
            color = Color.argb(0x88,0x00,0x00,0x00)
            style = Paint.Style.FILL
        }

        private fun calculateHandRotation(c: PointF, t : Float, r : Float) : PointF{
            val clockHand = PointF(0f,0f)
            if(animateLoadClock && isLoading){
            // Do rotating the clock by frame instead of clock
                clockHand.y = Math.sin(((Time.currentTime % 1f) * 360) * VshView.Deg2Rad).toFloat() * r
                clockHand.x = Math.cos(((Time.currentTime % 1f) * 360) * VshView.Deg2Rad).toFloat() * r
            }else{
                clockHand.x = Math.cos(((t * 360) - 90) * VshView.Deg2Rad).toFloat() * r
                clockHand.y = Math.sin(((t * 360) - 90) * VshView.Deg2Rad).toFloat() * r
            }
            return PointF(c.x + clockHand.x, c.y + clockHand.y)
        }

        fun drawClock(ctx:Canvas, x:Float, y:Float, r:Float) {
            ctx.drawCircle(x,y,r, clockBackPaint)
            ctx.drawCircle(x,y,r, outlinePaint)
            val min = Calendar.getInstance().get(Calendar.MINUTE) / 60f
            val hrs = Calendar.getInstance().get(Calendar.HOUR) / 12f
            val minPos = calculateHandRotation(PointF(x,y), min, r * 0.8f)
            val hrsPos = calculateHandRotation(PointF(x,y), hrs, r * 0.5f)
            ctx.drawLine(x,y,minPos.x, minPos.y, outlinePaint)
            ctx.drawLine(x,y,hrsPos.x, hrsPos.y, outlinePaint)
            if(isLoading){
                ctx.drawCircle(x,y, r*(Time.currentTime % 1.0f), outlinePaint)
            }
        }

        @SuppressLint("SimpleDateFormat")
        fun getFormattedClock():String {
            val hh = if(use24Format) "HH" else "hh"
            val df = SimpleDateFormat("dd/MM $hh:mm")
            return df.format(Calendar.getInstance().time)
        }

        fun lStatusBar(ctx : Canvas){
            val y = refHeight * 0.1f
            val stHeight = if(shouldClockExpanded) 75 else 40
            val statusRect = RectF(refWidth-600,y, scaledScreenWidth + 20f,y + stHeight)

            ctx.drawRoundRect(statusRect, 5f,5f, fillPaint)
            ctx.drawRoundRect(statusRect, 5f,5f, outlinePaint)
            Paints.statusPaint.textAlign = Paint.Align.RIGHT

            val sb = StringBuilder()
            if(showOperatorName) sb.append(operatorName).append("        ")
            sb.append(getFormattedClock())
            ctx.drawText(sb.toString(), refWidth-80, y, Paints.statusPaint, 1.2f)
            if(shouldClockExpanded){
                ctx.drawText(clockExpandInfo, refWidth-80, y, Paints.statusPaint, 2.2f)
            }

            drawClock(ctx,refWidth - 40f, statusRect.centerY(), 22f)
        }
    }

    object Notification {
        var isEnabled= true
        const val NOTIF_LONG = 15f
        const val NOTIF_MEDIUM = 10f
        const val NOTIF_SHORT = 5f
        class NotifItem (val content:String, val icon:Bitmap, val title:String = "", var duration:Float = NOTIF_MEDIUM){
            val isNoTitle get() = title.isBlank()
        }

        private val notifications = arrayListOf<NotifItem>()

        fun postNotification(content:String, icon: Bitmap, title:String = "", duration: Float = NOTIF_MEDIUM){
            notifications.add(NotifItem(content, icon, "", duration))
        }

        fun lNotification (canvas:Canvas, deltaTime:Float){
            if(!isEnabled) return
            val deadNotif = arrayListOf<NotifItem>()
            notifications.forEach {
                it.duration -= deltaTime
                if(it.duration < 0.0f) deadNotif.add(it)
            }
            notifications.removeAll(deadNotif)
        }
    }

    object Input {
        var useUSMode = false
        private var lastPos = PointF(0f,0f)
        var launchRect = RectF(0f,0f,0f,0f)

        private var isDragging = false
        var dragSensitivity = 30
        enum class AxisLock {
            None, Horizontal, Vertical
        }
        var axisLock = AxisLock.None

        fun recalculateLaunchPos(){
            // TODO
        }

        data class Taps(val id:Int, var pos:PointF)
        val taps = ArrayList<Taps>()
        var moveTap = Taps(0,PointF(0F,0F))

        fun onKeyDown(key: InputKeys){
            when(key){
                InputKeys.Back -> sendBackSignal()
                InputKeys.Select -> sendConfirmSignal()
                InputKeys.DPadU -> setSelectionRel(0,-1)
                InputKeys.DPadD -> setSelectionRel(0,1)
                InputKeys.DPadL -> setSelectionRel(-1,0)
                InputKeys.DPadR -> setSelectionRel(1,0)
                else -> { /** TODO: */ }
            }
        }

        fun setSelectionRel(x:Int,y:Int){
            try{
                val horz = getActiveHorizontalParentMenu()
                val vert = getActiveVerticalParentMenu()

                val xidx = (horz.selectedIndex + x).coerceIn(0, (horz.contentSize - 1).coerceAtLeast(0))
                val yidx = (vert.selectedIndex + y).coerceIn(0, (vert.contentSize - 1).coerceAtLeast(0))

                if(horz.selectedIndex != xidx) CrossMenu.xLerpOffset= x * -1.0F;
                if(vert.selectedIndex != yidx) CrossMenu.yLerpOffset= y * -1.0F;

                vert.selectedIndex = yidx;
                horz.selectedIndex = xidx;
            }catch(e:Exception){}
        }

        fun onKeyUp(key: InputKeys){
        }

        fun onTouchDown(id:Int, pos:PointF){
            taps.add(Taps(id, pos * (1/calculatedScale)))
        }

        var moveTapId = -1

        fun onTouchMove(id:Int, pos:PointF){
            taps.forEach {
                if(it.id == id){
                    val lastPos = it.pos
                    it.pos = pos * (1/calculatedScale)

                    val x = it.pos.x - lastPos.x
                    val y = it.pos.y - lastPos.y
                    val absX = abs(x)
                    val absY = abs(y)

                    if(max(absX, absY) > dragSensitivity){
                        if(moveTapId == -1 || moveTapId == it.id){
                            if(absX > absY){
                                if(axisLock == AxisLock.Horizontal || axisLock == AxisLock.None){
                                    moveTapId = it.id
                                    axisLock = AxisLock.Horizontal
                                    setSelectionRel(if(x > 0) -1 else 1, 0)
                                }
                            }else{
                                if(axisLock == AxisLock.Vertical || axisLock == AxisLock.None){
                                    moveTapId = it.id
                                    axisLock = AxisLock.Vertical
                                    setSelectionRel(0, if(y > 0) -1 else 1)
                                }
                            }
                        }
                    }
                }
            }
        }

        fun onTouchUp(id:Int, pos:PointF){
            val item = taps.filter { it.id == id }
            if(item.isNotEmpty()){
                item.forEach{
                    if(moveTapId == it.id) {
                        moveTapId = -1
                        axisLock = AxisLock.None
                    }
                    taps.remove(it)
                }
            }
        }
    }

    object CrossMenu {
        var xLoc = 0.3f
        var yLoc = 0.3f
        var yLerpOffset = 0.0f
        var xLerpOffset = 0.0f

        fun lHorizontalMenu(ctx:Canvas)
        {
            updateLerps()
            Debug.debugPaint.color = Color.argb(64,0,0,255)
            if(depth <= 1){
                val items = root.content
                val sidx = root.selectedIndex
                val sidxf = root.selectedIndexF
                items.forEachIndexed { i, it ->
                    val delta = i - sidx
                    val deltaF = i - sidxf
                    val isSelected = delta == 0

                    val iconSize = if(isSelected) 125f else 100f
                    val iconHalf = iconSize/2f
                    val posX = xLoc * refWidth  - (iconSize * xLerpOffset)
                    val posY = yLoc * refHeight - (iconSize * 0.0F )

                    val itemX = (posX - iconHalf) + (deltaF * (iconSize + 50))
                    val iconRect = RectF(itemX, posY - iconHalf, itemX + iconSize, posY +iconHalf)

                    ctx.drawRect(iconRect, Debug.debugPaint)

                    val icon = if(isSelected) it.activeIcon else it.inactiveIcon
                    ctx.drawBitmap(icon, icon.getRect(), iconRect, Paints.itemTitleSelected)
                    if(isSelected){
                        Paints.categoryTitleSelected .textAlign = Paint.Align.CENTER
                        ctx.drawText(it.name, iconRect.centerX(), iconRect.bottom + 10f, Paints.categoryTitleSelected , 1.0f)
                    }
                }
            }else{
                val iconSize = 125f
                val iconHalf = iconSize/2f
                val posX = xLoc * refWidth  - (iconSize * xLerpOffset)
                val posY = yLoc * refHeight - (iconSize * yLerpOffset)
                val iconRect =  RectF(posX - iconSize, posY - iconHalf, posX + iconHalf,posY + iconHalf)
                val parent = getActiveVerticalParentMenu()
                ctx.drawBitmap(parent.inactiveIcon, Rect(0,0,75,75), iconRect, Paints.itemSubtitleUnselected)
            }
        }

        fun updateLerps()
        {
            xLerpOffset = 0.5f.toLerp(xLerpOffset, 0f)
            yLerpOffset = 0.5f.toLerp(yLerpOffset, 0f)
        }

        fun lVerticalMenu(ctx:Canvas)
        {
            val sidx = getActiveVerticalParentMenu().selectedIndex
            val sidxf = getActiveVerticalParentMenu().selectedIndexF
            val items = verticalItems
            val posXName = (xLoc * refWidth) + 100
            Debug.debugPaint.color = Color.argb(64,0,255,0)
            items.forEachIndexed{ i, it ->
                val delta = i - sidx
                val deltaF = i - sidxf
                val isSelected = delta == 0

                val iconSize = if(isSelected) 110f else 100f
                val iconHalf = iconSize/2f
                val posX = xLoc * refWidth  - (iconSize * xLerpOffset)
                val posY = yLoc * refHeight - (iconSize * yLerpOffset)

                var itemY = (posY - iconHalf) + (deltaF * (iconSize + 20))
                if(delta < 0){
                    itemY -= 1
                }else{
                    itemY += iconSize + 50
                }
                if(delta >= 1) itemY += 20;

                val titlePaint = if(isSelected) Paints.itemTitleSelected else Paints.itemTitleUnselected
                val descPaint = if(isSelected) Paints.itemSubtitleSelected else Paints.itemSubtitleUnselected

                val iconRect = RectF(posX - iconHalf, itemY, posX + iconHalf, itemY + iconSize)
                val icon = if(isSelected) it.activeIcon else it.inactiveIcon
                ctx.drawBitmap(icon, icon.getRect(), iconRect, titlePaint)
                titlePaint.textAlign = Paint.Align.LEFT
                descPaint.textAlign = Paint.Align.LEFT

                ctx.drawRect(iconRect, Debug.debugPaint)

                val textYOffset = if(it.hasDescription) -0.25f else 0.5f

                ctx.drawText(it.name, posXName, iconRect.centerY(), titlePaint, textYOffset)
                if(it.hasDescription) ctx.drawText(it.description, posXName, iconRect.centerY(), descPaint, 1.0f)
            }
        }
    }

    object Debug {
        val Texts = ArrayList<String>()
        val debugPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply{}
        val assD = SSADrawing(StaticData.Paths.PSWLogo,0f,0f)

        private val debugTouchesPath = Path()

        fun lTouches(canvas:Canvas){
            debugPaint.color = Color.argb(0xFF, 0x00,0x99,0xFF)
            debugTouchesPath.reset()
            var offset = calculatedAROffset
            Input.taps.forEach {
                debugTouchesPath.addCircle(it.pos.x - offset.x, it.pos.y - offset.y, 10.0f, Path.Direction.CW)
            }
            debugTouchesPath.close()
            canvas.drawPath(debugTouchesPath, debugPaint)
        }

        private fun lFPS(canvas:Canvas){
            val fps = (1/Time.deltaTime).toInt()
            debugPaint.color = Color.WHITE
            debugPaint.textSize = 18f
            canvas.drawText("$fps FPS | ${Time.deltaTime}ms", 0f, refHeight - 20, debugPaint, 0.5f)
            canvas.drawText("Root item has ${root.content.size} content ", 0f, refHeight - 20, debugPaint, -1.5f)
        }

        fun lDebug(canvas:Canvas){
            Texts.forEachIndexed { i, it ->
                canvas.drawText(it, i * 1f, 10f, itemTitleSelected)
            }
            debugPaint.color = Color.argb(0x88,0x00,0x00,0xFF)
            canvas.drawRect(RectF(0f,0f, refWidth, refHeight), debugPaint)
            debugPaint.color = Color.argb(0x88,0xFF,0x00,0x00)
            val lr = ((refWidth /2f) - (refSafeWidth / 2f))
            canvas.drawRect(RectF(lr,0f, lr + refSafeWidth, refHeight), debugPaint)
            assD.draw(canvas)
            lTouches(canvas)
            lFPS(canvas)
        }
    }

    init {
        println("XMB Rendering Server is initialized")
    }

    fun reinitContext(ctx: VSH){
        density = ctx.resources.displayMetrics.density
        scaledDensity = ctx.resources.displayMetrics.scaledDensity
    }

    private val calculatedScale get()= (if(width / refWidth < height / refHeight) width/ refWidth else height/ refHeight) * overalScale
    private val calculatedAROffset :PointF get(){
        val x = (((width / calculatedScale)  / 2f)-  (refWidth  / 2f))
        val y = (((height / calculatedScale) / 2f) - (refHeight / 2f))
        return PointF(x,y)
    }
    private val fitAR get()= (if(width / refWidth > height / refHeight) width/ height else height/ width) * overalScale

    fun calculateCenteringArea() : PointF{
        val s = 1f/ calculatedScale
        val screen = PointF(width * s, height * s)
        val target = PointF(refWidth, refHeight)
        return PointF((screen.x - target.x)/2f, (screen.y - target.y) / 2f)
    }

    fun draw(canvas: Canvas){
        Time.updateTime()
        Paints.updatePaints()
        width = canvas.width.toFloat()
        height = canvas.height.toFloat()
        val center = calculateCenteringArea()
        canvas.withScale(calculatedScale, calculatedScale) {
            canvas.withTranslation(center.x, center.y) {
                canvas.drawPaint(Paints.vshBackground)
                // Debug.lDebug(canvas)

                if(!showDesktop){
                    try{
                        CrossMenu.lVerticalMenu(canvas)
                        CrossMenu.lHorizontalMenu(canvas)
                    }catch(cce:ConcurrentModificationException){
                        Log.w("VshServer.Render","Rendering suspended waiting for item population.. Safe to ignore")
                    }
                }
                StatusBar.lStatusBar(canvas)
            }
        }
    }

    fun getActiveVerticalParentMenu() : XMBIcon{
        var retval : XMBIcon = root
        for(cDepth in 0 until depth){
            retval = retval.content[retval.selectedIndex]
        }
        return retval
    }

    val horizontalItems : ArrayList<XMBIcon> get() = getActiveHorizontalParentMenu().content
    val verticalItems : ArrayList<XMBIcon> get() = getActiveVerticalParentMenu().content

    fun getActiveHorizontalParentMenu() : XMBIcon {
        var retval : XMBIcon = root
        for(cDepth in 0 until (depth - 1)){
            retval = retval.content[retval.selectedIndex]
        }
        return retval
    }

    fun getActiveItem() : XMBIcon
    {
        val item = getActiveVerticalParentMenu()
        return item.content[item.selectedIndex]
    }

    fun sendBackSignal() {
        if(!ContextMenu.visible){
            ContextMenu.visible = false
        }else if(showDesktop){
            showDesktop = false
        }else if(depth <= 1) {
            showDesktop = !showDesktop
        }else{
            depth--
        }
    }

    fun sendConfirmSignal(){
        getActiveItem().onLaunch()
    }

    fun setSelection(vararg indices:Int)
    {
        depth = indices.size
        var retval : XMBIcon = root
        for(cDepth in 0 until depth){
            retval.selectedIndex = indices[cDepth]
            retval = retval.content[retval.selectedIndex]
        }
    }

    fun findCategory(id:String) : XMBIcon? {
        var item : XMBIcon? = null

        root.content.forEach {
            Log.d(TAG, "ItemID : ${it.id} | ${it.itemId}")
            if(it.itemId.equals(id, true)) item = it
        }

        if(item !=null){
            Log.d(TAG, "Found item : $id")
        }else{
            Log.d(TAG, "Item with ID $id not found")
        }
        return item
    }
}


