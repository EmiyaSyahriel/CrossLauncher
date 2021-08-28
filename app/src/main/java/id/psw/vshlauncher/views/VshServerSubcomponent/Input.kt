package id.psw.vshlauncher.views.VshServerSubcomponent

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.view.MotionEvent
import id.psw.vshlauncher.distanceTo
import id.psw.vshlauncher.views.VshServer
import id.psw.vshlauncher.views.VshServer.times

object Input {
    enum class Keys {
        DPadU, DPadD, DPadL, DPadR,
        Square, Cross, Circle, Triangle
    }

    var useUSMode = false
    private var lastPos = PointF(0f,0f)
    private val launchPos : PointF
        get() = PointF(
        CrossMenu.xLoc * VshServer.orientWidth,
        (CrossMenu.yLoc * VshServer.orientHeight) + (CrossMenu.selectedIconSize * 1.4f)
    )
    val launchPosRad : Float get() = CrossMenu.selectedIconSize/1.5f

    /** Delay of the first hold tap after first keydown */
    val holdReTapFirstDelay = 0.3f
    /** Delay between hold re-taps */
    val holdReTapRepeatDelay = 0.05f

    enum class DirectionPad(val value:Int) {
        Up(0),
        Down(1),
        Left(2),
        Right(3),
    }

    val directionalPadPos : Array<PointF> get() {
        return Array<PointF>(4){
            val halfIcon = CrossMenu.selectedIconSize
            when(it){
                // 0 = Up
                DirectionPad.Up.value -> PointF(CrossMenu.xLoc * VshServer.orientWidth, 0f)
                // 1 = Down
                DirectionPad.Down.value -> PointF(CrossMenu.xLoc * VshServer.orientWidth, VshServer.orientHeight)
                // 2 = Left
                DirectionPad.Left.value -> PointF(0f, CrossMenu.yLoc * VshServer.orientHeight)
                // 3 = Right
                DirectionPad.Right.value -> PointF(VshServer.orientWidth, CrossMenu.yLoc * VshServer.orientHeight)
                // else = None
                else -> PointF(0F,0F)
            }
        }
    }
    val directionalPadRad : Float get() = CrossMenu.selectedIconSize

    private var isDragging = false
    var dragSensitivity = 30
    enum class AxisLock {
        None, Horizontal, Vertical
    }
    var axisLock = AxisLock.None

    fun recalculateLaunchPos(){

    }

    class TapPoint(val id:Int, var pos: PointF){
        var hasFirstTap = false
        var time : Float = 0.0f
        var totalTime : Float = 0.0f
        var lastTapTime : Float = 0.0F
    }
    val taps = ArrayList<TapPoint>()
    var moveTap = TapPoint(0, PointF(0F,0F))

    fun onKeyDown(key: Keys){
        when(key){
            Keys.Cross -> if(useUSMode) VshServer.sendConfirmSignal() else VshServer.sendBackSignal()
            Keys.Circle -> if(useUSMode)  VshServer.sendBackSignal() else VshServer.sendConfirmSignal()
            Keys.DPadU -> setSelectionRel(0,-1)
            Keys.DPadD -> setSelectionRel(0,1)
            Keys.DPadL -> setSelectionRel(-1,0)
            Keys.DPadR -> setSelectionRel(1,0)
            else -> { /** TODO: */ }
        }
    }

    fun setSelectionRel(x:Int,y:Int){
        try{
            val horz = VshServer.getActiveHorizontalParentMenu()
            val vert = VshServer.getActiveVerticalParentMenu()

            val xidx = (horz.selectedIndex + x).coerceIn(0, (horz.contentSize - 1).coerceAtLeast(0))
            val yidx = (vert.selectedIndex + y).coerceIn(0, (vert.contentSize - 1).coerceAtLeast(0))

            if(horz.selectedIndex != xidx) CrossMenu.xLerpOffset= x * -1.0F
            if(vert.selectedIndex != yidx) CrossMenu.yLerpOffset= y * -1.0F

            vert.selectedIndex = yidx
            horz.selectedIndex = xidx
        }catch(e:Exception){}
    }

    fun onKeyUp(key: Keys){
    }

    fun onTouchDown(id:Int, pos: PointF){
        val tap = TapPoint(id, pos * (1/ VshServer.calculatedScale))
        taps.add(tap)
        doInputTapEvaluation(tap, MotionEvent.ACTION_POINTER_DOWN)
    }

    var moveTapId = -1

    fun onTouchMove(id:Int, pos: PointF){
        taps.forEach {
            if(it.id == id){
                val lastPos = it.pos
                it.pos = pos * (1/ VshServer.calculatedScale)
                var lastDist = lastPos.distanceTo(it.pos)
                if(lastDist > 20){
                    it.hasFirstTap = false
                    it.time = 0.0f
                }
            }
        }
    }

    fun onTouchUp(id:Int, pos: PointF){
        val item = taps.filter { it.id == id }
        if(item.isNotEmpty()){
            item.forEach{
                doInputTapEvaluation(it, MotionEvent.ACTION_POINTER_UP)
                taps.remove(it)
            }
        }
    }

    fun doInputTapEvaluation(tap:TapPoint, action:Int){

        val screenOffset = VshServer.calculateCenteringArea()
        val offsetTap = PointF(tap.pos.x - screenOffset.x, tap.pos.y - screenOffset.y)

        if(action != MotionEvent.ACTION_POINTER_UP){
            directionalPadPos.forEachIndexed { i, it ->
                if(offsetTap.distanceTo(it) < (directionalPadRad* 2) ){
                    when(i){
                        DirectionPad.Up.value -> onKeyDown(Keys.DPadU)
                        DirectionPad.Down.value -> onKeyDown(Keys.DPadD)
                        DirectionPad.Left.value -> onKeyDown(Keys.DPadL)
                        DirectionPad.Right.value -> onKeyDown(Keys.DPadR)
                    }
                }
            }
        }

        if(offsetTap.distanceTo(launchPos) < launchPosRad ){
            if(action == MotionEvent.ACTION_POINTER_UP && tap.totalTime < 0.5f){
                VshServer.getActiveItem()?.onLaunch()
            }else{
                VshServer.showContextMenu()
            }
        }
    }

    fun tickInput(){
        var time = Time.deltaTime
        taps.forEach {
            it.totalTime += time;
            it.time += time
            if(it.hasFirstTap){
                if(time > holdReTapRepeatDelay){
                    time = 0.0f
                    doInputTapEvaluation(it, MotionEvent.ACTION_POINTER_INDEX_SHIFT)
                }
            }else{
                if(time > holdReTapFirstDelay){
                    time = 0.0f
                    it.hasFirstTap = true
                    doInputTapEvaluation(it, MotionEvent.ACTION_MASK)
                }
            }
        }
    }

    private val debugPaint = Paint().apply { color = Color.argb(64,255,255,0) }

    fun lDebugInput(ctx: Canvas){
        ctx.drawCircle(launchPos.x, launchPos.y, launchPosRad, debugPaint)
        directionalPadPos.forEach {
            ctx.drawCircle(it.x, it.y, directionalPadRad, debugPaint)
        }
    }
}
