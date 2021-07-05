package id.psw.vshlauncher.views.VshServerSubcomponent

import android.graphics.*
import id.psw.vshlauncher.customtypes.SSADrawing
import id.psw.vshlauncher.staticdata.StaticData
import id.psw.vshlauncher.views.VshServer
import id.psw.vshlauncher.views.VshServer.drawText


object Debug {
    val Texts = ArrayList<String>()
    val debugPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply{}
    val assD = SSADrawing(StaticData.Paths.PSWLogo,0f,0f)

    private val debugTouchesPath = Path()

    fun lTouches(canvas: Canvas){
        debugPaint.color = Color.argb(0xFF, 0x00,0x99,0xFF)
        debugTouchesPath.reset()
        var offset = VshServer.calculatedAROffset
        VshServer.Input.taps.forEach {
            debugTouchesPath.addCircle(it.pos.x - offset.x, it.pos.y - offset.y, 10.0f, Path.Direction.CW)
        }
        debugTouchesPath.close()
        canvas.drawPath(debugTouchesPath, debugPaint)
    }

    private fun lFPS(canvas: Canvas){
        val fps = (1/ Time.deltaTime).toInt()
        debugPaint.color = Color.WHITE
        debugPaint.textSize = 18f
        canvas.drawText("$fps FPS | ${Time.deltaTime}ms", 0f, VshServer.refHeight - 20, debugPaint, 0.5f)
        canvas.drawText("Root item has ${VshServer.root.content.size} content ", 0f, VshServer.refHeight - 20, debugPaint, -1.5f)
    }

    fun lDebug(canvas: Canvas){
        Texts.forEachIndexed { i, it ->
            canvas.drawText(it, i * 1f, 10f, Paints.itemTitleSelected)
        }
        debugPaint.color = Color.argb(0x88,0x00,0x00,0xFF)
        canvas.drawRect(RectF(0f,0f, VshServer.refWidth, VshServer.refHeight), debugPaint)
        debugPaint.color = Color.argb(0x88,0xFF,0x00,0x00)
        val lr = ((VshServer.refWidth /2f) - (VshServer.refSafeWidth / 2f))
        canvas.drawRect(RectF(lr,0f, lr + VshServer.refSafeWidth, VshServer.refHeight), debugPaint)
        assD.draw(canvas)
        lTouches(canvas)
        lFPS(canvas)
    }
}
