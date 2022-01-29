package id.psw.vshlauncher.views

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import id.psw.vshlauncher.*
import id.psw.vshlauncher.submodules.InputSubmodule

class VshView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

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
    var useInternalWallpaper = false

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
        if(currentPage != view){
            when(currentPage){
                VshViewPage.ColdBoot -> cbEnd()
                VshViewPage.MainMenu -> menuEnd()
                VshViewPage.GameBoot -> gbEnd()
            }
            currentPage = view
            when(currentPage){
                VshViewPage.ColdBoot -> cbStart()
                VshViewPage.MainMenu -> menuStart()
                VshViewPage.GameBoot -> gbStart()
            }
        }
    }

    private val notificationBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(192,20,20,20)
    }

    private val notificationTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 20.0f
        color = Color.WHITE
        textAlign = Paint.Align.LEFT
    }

    private val notificationTitleTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 22.0f
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

    fun onUpdate(){
        context.vsh.itemOffsetX = (time.deltaTime * 10.0f).toLerp(context.vsh.itemOffsetX, 0.0f)
        if(VSH.Input.getKeyDown(InputSubmodule.Key.PadL)){
            context.vsh.itemCursorX--
            context.vsh.itemOffsetX = -1.0f
        }
        if(VSH.Input.getKeyDown(InputSubmodule.Key.PadR)){
            context.vsh.itemCursorX++
            context.vsh.itemOffsetX = 1.0f
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
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
                    }

                    drawNotifications(canvas)
                }
            }
        }
        VSH.Input.update()
        invalidate()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyUp(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }
}