package id.psw.vshlauncher.views

import android.graphics.*
import id.psw.vshlauncher.FittingMode
import id.psw.vshlauncher.select

fun Canvas.drawText(text:String, x:Float, y:Float, paint: Paint, yOff:Float){
    drawText(text, x, y + (paint.textSize * yOff), paint)
}

fun Canvas.drawBitmap(bm:Bitmap, src: Rect?, dst: RectF, paint:Paint, fitMode:FittingMode){
    if(fitMode == FittingMode.STRETCH){
        drawBitmap(bm, src, dst, paint)
    }else{
        val w = src?.width() ?: bm.width
        val h = src?.height() ?: bm.height

        val sx = w / dst.width()
        val sy = h / dst.height()
        val sc = (fitMode == FittingMode.FIT).select(
            (sx <= sy).select(sx,sy),
            (sx >= sy).select(sx,sy),
        )
        val sdst = RectF(dst.left, dst.top, dst.left + (w * sc), dst.top + (h * sc))
        drawBitmap(bm, src, sdst, paint)
    }
}