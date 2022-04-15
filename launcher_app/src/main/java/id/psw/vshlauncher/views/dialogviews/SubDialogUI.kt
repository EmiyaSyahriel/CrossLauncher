package id.psw.vshlauncher.views.dialogviews

import android.graphics.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRect
import androidx.core.graphics.toRectF
import id.psw.vshlauncher.*
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.views.drawText

object SubDialogUI {
    private var hasInit = false
    private lateinit var texProgressBar : Bitmap
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
        ctx.drawBitmap(checkBoxTextures[value.select(1,0)], null, RectF(), null)
    }
}
