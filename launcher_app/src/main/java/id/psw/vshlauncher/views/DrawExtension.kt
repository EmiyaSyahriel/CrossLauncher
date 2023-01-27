package id.psw.vshlauncher.views

import android.graphics.*
import androidx.core.graphics.toRect
import androidx.core.graphics.toRectF
import androidx.core.graphics.withClip
import id.psw.vshlauncher.*
import id.psw.vshlauncher.types.Ref
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object DrawExtension {
    private var hasInit = false
    private lateinit var texProgressBar : Bitmap
    private lateinit var texGlowEdge : Bitmap
    private var checkBoxTextures : Array<Bitmap> = arrayOf()

    private val texProgressBarUVRectBuffer = Rect()
    private fun texProgressBarUv(hPart:Int, isBack:Boolean) : Rect{
        val w = texProgressBar.width
        val h = texProgressBar.height
        val t = (isBack.select(0.0f, 0.5f) * h).toInt()
        val l = when(hPart){
                0 -> 0
                1 -> (0.25f * w).toInt()
                2 -> (0.75f * w).toInt()
                else -> 0
            }
        val r = when(hPart){
            0 -> (0.25f * w).toInt()
            1 -> (0.75f * w).toInt()
            2 -> w
            else -> 0
        }
        val b = (isBack.select(0.5f, 1.0f) * h).toInt()
        texProgressBarUVRectBuffer.set(l,t,r,b)
        return texProgressBarUVRectBuffer
    }

    private fun x3Patch(rc:Rect, x:Int) : Rect{
        return x3Patch(rc.toRectF(), x).toRect()
    }

    private val x3PatchRectFBuffer = RectF()
    private fun x3Patch(rc:RectF, x:Int) : RectF{
        val h = rc.height() / 2.0f
        return when(x){
            0 -> {
                x3PatchRectFBuffer.set(rc.left, rc.top, rc.left + h, rc.bottom)
                x3PatchRectFBuffer
            }
            1->{
                x3PatchRectFBuffer.set(rc.left + h,  rc.top,rc.right - h, rc.bottom)
                x3PatchRectFBuffer
            }
            2-> {
                x3PatchRectFBuffer.set(rc.right - h ,  rc.top, rc.right, rc.bottom)
                x3PatchRectFBuffer
            }
            else -> throw IndexOutOfBoundsException("Only supports [0-2]")
        }
    }

    fun init(vsh: VSH){
        texProgressBar = vsh.loadTexture(R.drawable.miptex_progressbar, false)

        texGlowEdge = vsh.loadTexture(R.drawable.miptex_gradient_border_128, 120, 120, false)
        checkBoxTextures = arrayOf(
            vsh.loadTexture(R.drawable.ic_checkbox_blank, false),
            vsh.loadTexture(R.drawable.ic_checkbox_filled, true),
        )

        hasInit = true
    }

    private fun paintAlignToFloat(align:Paint.Align) : Float{
        return when(align){
            Paint.Align.LEFT -> 0.0f
            Paint.Align.CENTER -> 0.5f
            Paint.Align.RIGHT -> 1.0f
            else -> 0.0f
        }
    }

    private val encpPaint = Paint()
    private val scrollBarPaint = Paint().apply {
        color = Color.WHITE
    }
    private val arrows = arrayOf('\u25BA','\u25C4')
    private val arrowPath = Path()
    fun arrowCapsule(ctx:Canvas, x:Float, y:Float, w:Float, paint:Paint, cTime:Float, yOffset:Float = 0.0f, isLeft:Boolean = true, isRight: Boolean = true){
        synchronized(encpPaint){
            encpPaint.set(paint)
            val xAlign = paintAlignToFloat(encpPaint.textAlign)
            val animT = (cTime % 2.0f) / 2.0f
            encpPaint.alpha = (1.0f - animT).toLerp(0.0f, 255.0f).toInt().coerceIn(0, 255)
            val tSlate = (1.0f - animT).toLerp(20.0f, 5.0f)
            arrowPath.reset()
            val h = encpPaint.textSize
            val hx = x - xAlign.toLerp(0.0f, w)
            val lx = hx - xAlign.toLerp(0.0f, tSlate)
            val rx = hx + w + xAlign.toLerp(0.0f, tSlate)
            val hy = (y + h) - (h * yOffset)
            val hh = h * 0.5f
            if(isLeft) {
                arrowPath.moveTo(lx, hy)
                arrowPath.lineTo(lx, hy + h)
                arrowPath.lineTo(lx - h, hy + hh)
                arrowPath.close()
            }
            if(isRight) {
                arrowPath.moveTo(rx, hy)
                arrowPath.lineTo(rx, hy + h)
                arrowPath.lineTo(rx + h, hy + hh)
                arrowPath.close()
            }
            ctx.drawPath(arrowPath, encpPaint)
        }
    }

    fun scrollSinTime(time:Float, maxTime:Float) : Float{
        val x = (time % maxTime) / maxTime
        return (((2.0 * cos (2 * x * PI)) + 1) / 2).coerceIn(0.0, 1.0).toFloat()
    }

    private val scrollClipRectF = RectF()

    fun scrollText(ctx:Canvas, str:String, startX:Float, endX:Float, y:Float, paint:Paint, yOffset:Float, time: Ref<Float>, cps: Float)
        = scrollText(ctx, str, startX, endX, y, paint, yOffset, time.p, cps)

    fun scrollText(ctx:Canvas, str:String, startX:Float, endX:Float, y:Float, paint:Paint, yOffset:Float, time: Float, cps: Float){
        val w = paint.measureText(str)
        if(w > endX - startX){
            val align = paint.textAlign
            paint.textAlign = Paint.Align.LEFT
            val oy = y + (yOffset * paint.textSize)
            scrollClipRectF.set(
                startX,
                oy - paint.textSize,
                endX,
                oy + paint.textSize
            )
            ctx.withClip(scrollClipRectF){
                val t = scrollSinTime(time, str.length / cps)
                val x = t.toLerp(startX, startX - w + (endX - startX))
                ctx.drawText(str, x, y, paint, yOffset)
            }
            paint.textAlign = align
        }else{
            val x = when(paint.textAlign) {
                Paint.Align.LEFT -> startX
                Paint.Align.RIGHT -> endX
                Paint.Align.CENTER -> (startX + endX) / 2.0f
                else -> startX
            }
            ctx.drawText(str, x, y, paint, yOffset)
        }
    }


    fun progressBar(ctx: Canvas, min:Float, max:Float, value:Float, x:Float, y:Float, w:Float, h:Float = 12.0f , align : Paint.Align = Paint.Align.LEFT){
        val xAlign = paintAlignToFloat(align)
        val rect = RectF(x + xAlign.toLerp(0.0f, -w), y, x + xAlign.toLerp(w, 0.0f), y + h)

        ctx.drawBitmap(texProgressBar, texProgressBarUv(0,true), x3Patch(rect, 0), null)
        ctx.drawBitmap(texProgressBar, texProgressBarUv(1,true), x3Patch(rect, 1), null)
        ctx.drawBitmap(texProgressBar, texProgressBarUv(2,true), x3Patch(rect, 2), null)

        val valRect = RectF(rect.left, rect.top, value.lerpFactor(min, max).toLerp(rect.left + rect.height(), rect.right), rect.bottom)

        ctx.drawBitmap(texProgressBar, texProgressBarUv(0,false), x3Patch(valRect, 0), null)
        ctx.drawBitmap(texProgressBar, texProgressBarUv(1,false), x3Patch(valRect, 1), null)
        ctx.drawBitmap(texProgressBar, texProgressBarUv(2,false), x3Patch(valRect, 2), null)
    }

    fun numericValue(ctx: Canvas, rect: RectF, min:Int, max:Int, value:Int, paint:Paint){

    }

    fun checkBox(ctx:Canvas, at: PointF, value:Boolean){
        checkBox(ctx, at.x, at.y, value)
    }

    fun checkBox(ctx:Canvas, x: Float, y: Float, value:Boolean){
        val sz = checkBoxTextures[value.select(1,0)]
        val w = 16.0f
        val h = 16.0f
        ctx.drawBitmap(sz, null, RectF(x-w, y-h ,x+w, y+h), null)
    }

    private val scrollBarRectFTmp = RectF()
    private val scrollBarPathTmp = Path()

    fun scrollBar(ctx:Canvas, rect:RectF, percentage:Float, size:Float){
        scrollBarPathTmp.reset()
        val isVertical = rect.width() < rect.height()
        val sqr = isVertical.select(rect.width(), rect.height())
        if(isVertical){
            val h = rect.height() - (sqr * 2)
            val sz = size.coerceIn(0.1f, 1.0f) * h
            // Top

            // Bottom
            // Body
            val bodyTop = percentage.toLerp(rect.top + sqr, rect.bottom - sz - sqr)
            scrollBarPathTmp.addRect(
                rect.left, bodyTop, rect.right,bodyTop + sz,
                Path.Direction.CW
            )
        }else{
            val w = rect.width() - (sqr * 2)
            val sz = size.coerceIn(0.1f, 1.0f) * w
            // Left
            // Right
            // Body
            val bodyLf = percentage.toLerp(rect.left + sqr, rect.right - sz - sqr)
            scrollBarPathTmp.addRect(
                bodyLf, rect.top, bodyLf + sz,rect.bottom,
                Path.Direction.CW
            )
        }
        ctx.drawPath(scrollBarPathTmp, scrollBarPaint)
    }

    private val glowOverlayRectTmp = Rect()
    private val glowOverlayRectFTmp = RectF()
    private val glowOverlayPaint = Paint()

    private fun glowOverlayDrawPatches(
        ctx:Canvas, il :Int, it : Int, ir :Int, ib:Int,
        fl:Float, ft:Float, fr:Float, fb:Float
    ) {
        glowOverlayRectTmp.set(il,it,ir,ib)
        glowOverlayRectFTmp.set(fl,ft,fr,fb)
        ctx.drawBitmap(texGlowEdge, glowOverlayRectTmp, glowOverlayRectFTmp, glowOverlayPaint)
    }

    fun glowOverlay(ctx:Canvas, rect:RectF, edge:Int, paint:Paint?, isActive : Boolean, time:Float = 0.0f){
        if(paint != null){
            glowOverlayPaint.set(paint)
        }
        if(isActive){
            val s = sin(time)
            val t = (s * 5.0f)
            glowOverlayPaint.setShadowLayer(t, 0.0f, 0.0f, Color.WHITE)
        }else{
            glowOverlayPaint.setShadowLayer(0.0f, 0.0f,0.0f, Color.TRANSPARENT)
        }

        // Top
        glowOverlayDrawPatches(ctx, 0,0,40,40, rect.left, rect.top , rect.left + edge, rect.top + edge)
        glowOverlayDrawPatches(ctx, 40,0,80,40, rect.left + edge, rect.top , rect.right - edge, rect.top + edge)
        glowOverlayDrawPatches(ctx, 80,0,120,40, rect.right - edge, rect.top , rect.right, rect.top + edge)
        // Center
        glowOverlayDrawPatches(ctx, 0,40,40,80, rect.left, rect.top +edge, rect.left + edge, rect.bottom - edge)
        glowOverlayDrawPatches(ctx, 40,40,80,80, rect.left + edge, rect.top +edge, rect.right - edge, rect.bottom - edge)
        glowOverlayDrawPatches(ctx, 80,40,120,80, rect.right - edge, rect.top +edge, rect.right, rect.bottom - edge)
        // Bottom
        glowOverlayDrawPatches(ctx, 0,80,40,120, rect.left, rect.bottom - edge, rect.left + edge, rect.bottom)
        glowOverlayDrawPatches(ctx, 40,80,80,120, rect.left + edge, rect.bottom - edge, rect.right - edge, rect.bottom)
        glowOverlayDrawPatches(ctx, 80,80,120,120, rect.right - edge, rect.bottom - edge, rect.right, rect.bottom)
    }
}
