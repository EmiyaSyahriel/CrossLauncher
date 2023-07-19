package id.psw.vshlauncher.views.dialogviews

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.text.TextPaint
import android.view.MotionEvent
import androidx.core.graphics.withClip
import id.psw.vshlauncher.R
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.select
import id.psw.vshlauncher.submodules.GamepadSubmodule.Key
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.VshViewPage
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.views.drawText
import java.io.InputStreamReader

class LicenseDialogView(private val vsh: VSH) :  XmbDialogSubview(vsh) {

    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = FontCollections.masterFont
        color = Color.WHITE
        textSize = 20.0f
        textAlign = Paint.Align.CENTER
    }

    private val tabs = arrayListOf<Pair<String, String>>()
    private var tabIndex = 0
    private var maxY = 0.0f
    private var currentY = 0.0f
    private var tabString = ""

    private var bound : RectF = RectF()
    private var tabRect : RectF = RectF()
    private val minH : Float get() = paint.textSize * 2.0f

    override val hasNegativeButton: Boolean
        get() = true

    override val positiveButton: String
        get() = vsh.getString(R.string.common_back)

    override val title: String
        get() = vsh.getString(R.string.setting_license_name)

    override fun onDialogButton(isPositive: Boolean) {
        if(!isPositive){
            finish(VshViewPage.MainMenu)
        }
    }

    private var isLastDown = false
    private var lastY = 0.0f

    // Display :
    //_______________
    //<| |Tab| Tab Tab |>
    //License content
    //_______________
    //

    override fun onStart() {
        val licIs = vsh.assets.open("licenses/lic.txt")

        val lists = InputStreamReader(licIs)
        lists.readLines().forEach {
            val parts = it.split('=')
            if(parts.size >= 2){
                val name = parts[0].trim()
                val file = parts[1].trim()
                tabs.add(name to file)
            }
        }

        licIs.close()

        switchTab(true)
    }

    private fun switchTab(left : Boolean) {
        val i = left.select(-1, 1)
        tabIndex = (tabIndex + i).coerceIn(0, tabs.size - 1)
        val tabInfo = tabs[tabIndex]
        val licIs = vsh.assets.open("licenses/${tabInfo.second}")
        currentY = paint.textSize * 1.5f
        val lists = InputStreamReader(licIs)
        tabString = lists.readText()
        licIs.close()
        maxY = tabString.lines().size * paint.textSize
    }

    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime: Float) {
        bound.set(drawBound)

        // Draw Tabs
        val w = bound.width() / tabs.size
        tabs.forEachIndexed { i, tab ->
            val l = drawBound.left + (i * w)
            tabRect.set(l, drawBound.top, l + w, drawBound.top + minH)
            ctx.withClip(tabRect){

                if(i == tabIndex){
                    ctx.drawARGB(128, 255, 255,255)
                }

                ctx.drawText(tab.first, tabRect.centerX(), tabRect.centerY(), paint, 0.5f)
            }
        }

        // Draw License Text
        ctx.withClip(bound.left, bound.top + minH, bound.right, bound.bottom){
            val lines = tabString.lines()
            lines.forEachIndexed { i, s ->
                val y = ((i + 1) * paint.textSize) + currentY
                ctx.drawText(s.trim(), drawBound.centerX(), y, paint)
            }
        }

        super.onDraw(ctx, drawBound, deltaTime)
    }

    private fun scroll(f : Float){
        val h = bound.height()
        currentY = if(maxY < h){
            minH
        }else{
            (currentY + f).coerceIn(-((maxY - h) + minH), minH)
        }
    }

    override fun onGamepad(key: Key, isPress: Boolean): Boolean {
        return if(isPress)  when(key){
                Key.L1 -> {
                    // Switch Tab --
                    switchTab(true)
                    true
                }
                Key.R1 -> {
                    // Switch Tab ++
                    switchTab(false)
                    true
                }
                Key.PadD -> {
                    // Scroll Down
                    scroll(20.0f)
                    true
                }
                Key.PadU -> {
                    // Scroll Up
                    scroll(-20.0f)
                    true
                }
                else -> false
            }
        else false
    }

    // Top Down
    override fun onTouch(a: PointF, b: PointF, act: Int) {
        val w = bound.width()
        val wh3 = w/3
        if(act == MotionEvent.ACTION_UP && b.x < wh3 * 1){ // Left
            switchTab(true)
        }else if(act == MotionEvent.ACTION_UP && b.x > wh3 * 2){ // Right
            switchTab(false)
        }else {
            if(!isLastDown){
                lastY = b.y
            }
            isLastDown = act == MotionEvent.ACTION_DOWN || act == MotionEvent.ACTION_MOVE
            val f = b.y - lastY
            scroll(f)
            lastY = b.y
        }
    }
}