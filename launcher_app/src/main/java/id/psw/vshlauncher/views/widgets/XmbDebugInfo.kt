package id.psw.vshlauncher.views.widgets

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Debug
import id.psw.vshlauncher.select
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.views.XmbWidget
import id.psw.vshlauncher.views.asBytes
import id.psw.vshlauncher.views.drawText
import id.psw.vshlauncher.views.removeShadowLayer
import id.psw.vshlauncher.vsh
import kotlin.math.roundToInt

class XmbDebugInfo(view: XmbView) : XmbWidget(view) {

    private val fpsRect = Rect()
    private val fpsRectF = RectF()
    private val memFmtSb = StringBuilder()
    private val memFmtNames = arrayOf("PSS", "PD", "SD", "PC", "SC", "??", "E", "??", "E", "??", "E", "??")
    private val memInfo = Debug.MemoryInfo()
    var memInfoThreadKeepRunning = false
    var dummyPaint = Paint()
    var showDetailedMemory = false
    var showLauncherFPS = false

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

    override fun render(ctx: Canvas) {
        if(showLauncherFPS) return
        val fps = (1.0f / time.deltaTime).roundToInt()
        val fpsTxt = "[FPS] $fps FPS | ${(time.deltaTime * 1000).roundToInt()} ms"
        dummyPaint.color = Color.GREEN
        dummyPaint.style = Paint.Style.FILL
        dummyPaint.setShadowLayer(2.0f, 2.0f, 2.0f, Color.BLACK)

        val isTvTxt = context.vsh.isTv.select("Device : TV","")

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

}