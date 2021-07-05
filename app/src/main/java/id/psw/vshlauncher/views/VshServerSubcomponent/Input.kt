package id.psw.vshlauncher.views.VshServerSubcomponent

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import id.psw.vshlauncher.views.VshServer
import id.psw.vshlauncher.views.VshServer.times

object Input {
    var useUSMode = false
    private var lastPos = PointF(0f,0f)
    private val launchPos : PointF
        get() = PointF(
        CrossMenu.xLoc * VshServer.refWidth,
        (CrossMenu.yLoc * 1.5f * VshServer.refHeight) + (CrossMenu.selectedIconSize/2f)
    )
    val launchPosRad : Float get() = CrossMenu.selectedIconSize/1.5f

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
                DirectionPad.Up.value -> PointF(CrossMenu.xLoc * VshServer.refWidth,halfIcon)
                // 1 = Down
                DirectionPad.Down.value -> PointF(CrossMenu.xLoc * VshServer.refWidth, VshServer.refHeight - halfIcon)
                // 2 = Left
                DirectionPad.Left.value -> PointF(halfIcon, CrossMenu.yLoc * VshServer.refHeight)
                // 3 = Right
                DirectionPad.Right.value -> PointF(VshServer.refWidth - halfIcon, CrossMenu.yLoc * VshServer.refHeight)
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

    data class Taps(val id:Int, var pos: PointF)
    val taps = ArrayList<Taps>()
    var moveTap = Taps(0, PointF(0F,0F))

    fun onKeyDown(key: VshServer.InputKeys){
        when(key){
            VshServer.InputKeys.Back -> VshServer.sendBackSignal()
            VshServer.InputKeys.Select -> VshServer.sendConfirmSignal()
            VshServer.InputKeys.DPadU -> setSelectionRel(0,-1)
            VshServer.InputKeys.DPadD -> setSelectionRel(0,1)
            VshServer.InputKeys.DPadL -> setSelectionRel(-1,0)
            VshServer.InputKeys.DPadR -> setSelectionRel(1,0)
            else -> { /** TODO: */ }
        }
    }

    fun setSelectionRel(x:Int,y:Int){
        try{
            val horz = VshServer.getActiveHorizontalParentMenu()
            val vert = VshServer.getActiveVerticalParentMenu()

            val xidx = (horz.selectedIndex + x).coerceIn(0, (horz.contentSize - 1).coerceAtLeast(0))
            val yidx = (vert.selectedIndex + y).coerceIn(0, (vert.contentSize - 1).coerceAtLeast(0))

            if(horz.selectedIndex != xidx) CrossMenu.xLerpOffset= x * -1.0F;
            if(vert.selectedIndex != yidx) CrossMenu.yLerpOffset= y * -1.0F;

            vert.selectedIndex = yidx;
            horz.selectedIndex = xidx;
        }catch(e:Exception){}
    }

    fun onKeyUp(key: VshServer.InputKeys){
    }

    fun onTouchDown(id:Int, pos: PointF){
        taps.add(Taps(id, pos * (1/ VshServer.calculatedScale)))
        directionalPadPos.forEachIndexed { i, it ->

        }
    }

    var moveTapId = -1

    fun onTouchMove(id:Int, pos: PointF){
        taps.forEach {
            if(it.id == id){
                val lastPos = it.pos
                it.pos = pos * (1/ VshServer.calculatedScale)
            }
        }
    }

    fun onTouchUp(id:Int, pos: PointF){
        val item = taps.filter { it.id == id }
        if(item.isNotEmpty()){
            item.forEach{
                taps.remove(it)
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
