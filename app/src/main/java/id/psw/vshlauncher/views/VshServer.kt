package id.psw.vshlauncher.views

import android.graphics.*
import android.util.Log
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.icontypes.XMBIcon
import id.psw.vshlauncher.icontypes.XMBRootIcon
import id.psw.vshlauncher.views.VshServerSubcomponent.*
import kotlin.ConcurrentModificationException
import kotlin.collections.ArrayList

// TODO: Finish this
object VshServer {

    const val TAG = "VshServer"

    fun recalculateClockRect() {
        // TODO("Not yet implemented")
    }

    var density = 0.0f
    var scaledDensity = 0.0f
    var targetWidth = 0.0f
    var targetHeight = 0.0f
    var _refWidth = 1280.0f
    var _refHeight = 720.0f

    val isLandscape get() = targetWidth > targetHeight

    val orientWidth get()= if(isLandscape) _refWidth else _refHeight
    val orientHeight get()= if(isLandscape) _refHeight else _refWidth

    var refSafeWidth = 960.0f
    var overalScale = 1.0f
    val scaledScreenWidth  get()= targetWidth / calculatedScale
    val scaledScreenHeight get()= targetHeight / calculatedScale
    val root : XMBRootIcon = XMBRootIcon()
    var showDesktop = false
    var coldBootTime = 0.0f
    var gameBootTime = 0.0f
    var depth = 1

    // region Extension
    public fun PointF.scaleToLocal() : PointF = PointF(this.x * calculatedScale, this.y * calculatedScale)
    public fun PointF.inverseScaleToLocal() : PointF = PointF(this.x / calculatedScale, this.y / calculatedScale)
    public fun Canvas.drawText(string: String, x:Float, y:Float, paint:Paint, v:Float){
        drawText(string, x, y + (paint.textSize * v), paint)
    }
    public operator fun PointF.times(calculatedScale: Float): PointF = PointF(x * calculatedScale, y * calculatedScale)
    // endregion

    object BootScreen{

        fun lGameBoot(canvas:Canvas, deltaTime: Float){

        }

        fun lColdboot(canvas:Canvas, deltaTime: Float){

        }
    }
    init {
        println("XMB Rendering Server is initialized")
    }

    fun reinitContext(ctx: VSH){
        density = ctx.resources.displayMetrics.density
        scaledDensity = ctx.resources.displayMetrics.scaledDensity
    }

    public val calculatedScale get()= (if(targetWidth / orientWidth < targetHeight / orientHeight) targetWidth/ orientWidth else targetHeight/ orientHeight) * overalScale
    public val calculatedAROffset :PointF get(){
        val x = (((targetWidth / calculatedScale)  / 2f)-  (orientWidth  / 2f))
        val y = (((targetHeight / calculatedScale) / 2f) - (orientHeight / 2f))
        return PointF(x,y)
    }
    public val fitAR get()= (if(targetWidth / orientWidth > targetHeight / orientHeight) targetWidth/ targetHeight else targetHeight/ targetWidth) * overalScale

    fun calculateCenteringArea() : PointF{
        val s = 1f/ calculatedScale
        val screen = PointF(targetWidth * s, targetHeight * s)
        val target = PointF(orientWidth, orientHeight)
        return PointF((screen.x - target.x)/2f, (screen.y - target.y) / 2f)
    }

    fun draw(canvas: Canvas){
        Time.updateTime()
        Paints.updatePaints()
        Input.tickInput()
        targetWidth = canvas.width.toFloat()
        targetHeight = canvas.height.toFloat()
        val center = calculateCenteringArea()
        canvas.withScale(calculatedScale, calculatedScale) {
            canvas.withTranslation(center.x, center.y) {
                canvas.drawPaint(Paints.vshBackground)
                // Debug.lDebug(canvas)

                if(!showDesktop){
                    try{
                        CrossMenu.lVerticalMenu(canvas)
                        CrossMenu.lHorizontalMenu(canvas)
                        Debug.lDebug(canvas)
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

    fun showContextMenu() {
        ContextMenu.visible = true
    }
}


