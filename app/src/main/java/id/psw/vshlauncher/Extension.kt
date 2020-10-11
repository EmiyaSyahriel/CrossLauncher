package id.psw.vshlauncher

import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.Log
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import java.io.File
import java.lang.Math.round
import java.util.*
import java.util.function.Predicate

fun PointF.distanceTo(other : PointF) : Float{
    return PointF(other.x - this.x, other.y - this.y).length()
}

fun Long.toSize() : String{
    var size = this.toDouble()
    var sizeIndex = 0
    while(size > 1024){
        size /= 1024
        sizeIndex++
    }
    size = round(size * 100.0) / 100.0
    val sizeName = MimeTypeDict.sizeMap.getOrNull(sizeIndex) ?: "B"
    return "$size$sizeName"
}

fun <T> Boolean.choose(ifTrue : T, ifFalse: T):T{
    return if (this) {ifTrue} else {ifFalse}
}



fun View.getSystemPadding() : Rect{
    val res = context.resources
    val retval = Rect(0,0,res.displayMetrics.widthPixels, res.displayMetrics.heightPixels)
    val navBarId = res.getIdentifier("navigation_bar_height","dimen","android")
    val statusBarId = res.getIdentifier("status_bar_height", "dimen", "android")
    if(res.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
        retval.left = res.getDimensionPixelSize(navBarId)
        retval.right = res.displayMetrics.widthPixels + (res.getDimensionPixelSize(navBarId) / 2)
        retval.top = res.getDimensionPixelSize(statusBarId)
        retval.bottom = res.displayMetrics.heightPixels
    }else{
        retval.top = res.getDimensionPixelSize(statusBarId)
        retval.bottom = res.displayMetrics.heightPixels + retval.top// - res.getDimensionPixelSize(navBarId)
    }

    Log.d("extSystemPadding", "Renderable Area : $retval")
    return retval
}

fun Canvas.drawSubImage(bitmap:Bitmap, count:Point, spritePos:Point, dest : RectF, paint:Paint){
    val sprSizeX = bitmap.width/ count.x
    val sprSizeY = bitmap.height/count.y
    val sprPosX = spritePos.x % count.x
    val sprPosY = spritePos.y % count.y
    val spriteRect = Rect(sprPosX * sprSizeX, sprPosY * sprSizeY, (sprPosX+1)*sprSizeX, (sprPosY +1) * sprSizeY)
    this.drawBitmap(bitmap,spriteRect, dest, paint)
}
fun Canvas.drawSubImage(bitmap:Bitmap, count:Point, spritePos:Point, drawPos:Point, drawSize:Point, paint:Paint){
    val sprSizeX = bitmap.width/ count.x
    val sprSizeY = bitmap.height/count.y
    val sprPosX = spritePos.x % count.x
    val sprPosY = spritePos.y % count.y
    val spriteRect = Rect(sprPosX * sprSizeX, sprPosY * sprSizeY, (sprPosX+1)*sprSizeX, (sprPosY +1) * sprSizeY)
    val dest = Rect(drawPos.x, drawPos.y, drawPos.x + drawSize.x, drawPos.y + drawSize.y)
    this.drawBitmap(bitmap,spriteRect, dest, paint)
}
fun Canvas.drawSubImage(bitmap:Bitmap, count:Point, spritePos:Point, drawPos:Point, drawSize:Int, paint:Paint){
    val sprSizeX = bitmap.width/ count.x
    val sprSizeY = bitmap.height/count.y
    val sprPosX = spritePos.x % count.x
    val sprPosY = spritePos.y % count.y
    val spriteRect = Rect(sprPosX * sprSizeX, sprPosY * sprSizeY, (sprPosX+1)*sprSizeX, (sprPosY +1) * sprSizeY)
    val dest = Rect(drawPos.x, drawPos.y, drawPos.x + drawSize, drawPos.y + drawSize)
    this.drawBitmap(bitmap,spriteRect, dest, paint)
}
fun Canvas.drawSubImage(bitmap:Bitmap, xCount:Int, yCount:Int, spritePosX : Int, spritePosY : Int, drawPosX:Int, drawPosY : Int, drawSizeX:Int, drawSizeY : Int, paint:Paint){
    val sprSizeX = bitmap.width/ xCount
    val sprSizeY = bitmap.height/ yCount
    val sprPosX = spritePosX % xCount
    val sprPosY = spritePosY % yCount
    val spriteRect = Rect(sprPosX * sprSizeX, sprPosY * sprSizeY, (sprPosX+1)*sprSizeX, (sprPosY +1) * sprSizeY)
    val dest = Rect(drawPosX, drawPosY, drawPosX + drawSizeX, drawPosY + drawSizeY)
    this.drawBitmap(bitmap,spriteRect, dest, paint)
}

fun Canvas.drawSubImage(bitmap:Bitmap, xCount:Int, yCount:Int, spritePosX : Int, spritePosY : Int, drawPosX:Float, drawPosY : Float, drawSizeX:Float, drawSizeY : Float, paint:Paint){
    val sprSizeX = bitmap.width/ xCount
    val sprSizeY = bitmap.height/ yCount
    val sprPosX = spritePosX % xCount
    val sprPosY = spritePosY % yCount
    val spriteRect = Rect(sprPosX * sprSizeX, sprPosY * sprSizeY, (sprPosX+1)*sprSizeX, (sprPosY +1) * sprSizeY)
    val dest = RectF(drawPosX, drawPosY, drawPosX + drawSizeX, drawPosY + drawSizeY)
    this.drawBitmap(bitmap,spriteRect, dest, paint)
}


fun Canvas.drawSubImage(bitmap:Bitmap, xCount:Int, yCount:Int, spritePosX : Int, spritePosY : Int, drawPosX:Int, drawPosY : Int, drawSize:Int, paint:Paint){
    val sprSizeX = bitmap.width/ xCount
    val sprSizeY = bitmap.height/ yCount
    val sprPosX = spritePosX % xCount
    val sprPosY = spritePosY % yCount
    val spriteRect = Rect(sprPosX * sprSizeX, sprPosY * sprSizeY, (sprPosX+1)*sprSizeX, (sprPosY +1) * sprSizeY)
    val dest = Rect(drawPosX, drawPosY, drawPosX + drawSize, drawPosY + drawSize)
    this.drawBitmap(bitmap,spriteRect, dest, paint)
}

fun Canvas.drawSubImage(bitmap:Bitmap, xCount:Int, yCount:Int, spritePosX : Int, spritePosY : Int, drawPosX:Float, drawPosY : Float, drawSize:Float, paint:Paint){
    val sprSizeX = bitmap.width / xCount
    val sprSizeY = bitmap.height / yCount
    val sprPosX = spritePosX % xCount
    val sprPosY = spritePosY % yCount
    val spriteRect = Rect(sprPosX * sprSizeX, sprPosY * sprSizeY, (sprPosX+1)*sprSizeX, (sprPosY +1) * sprSizeY)
    val dest = RectF(drawPosX, drawPosY, drawPosX + drawSize, drawPosY + drawSize)
    this.drawBitmap(bitmap,spriteRect, dest, paint)
}

fun Canvas.drawSubImage(bitmap:Bitmap, xCount:Int, yCount:Int, spritePosX : Int, spritePosY : Int, dest:Rect, paint:Paint, enabled:Boolean = true){
    val sprSizeX = bitmap.width/ xCount
    val sprSizeY = bitmap.height/ yCount
    val sprPosX = spritePosX % xCount
    val sprPosY = spritePosY % yCount
    val spriteRect = Rect(sprPosX * sprSizeX, sprPosY * sprSizeY, (sprPosX+1)*sprSizeX, (sprPosY +1) * sprSizeY)
    paint.alpha = if(enabled) 255 else 100
    this.drawBitmap(bitmap,spriteRect, dest, paint)
}

fun Canvas.drawSubImage(iconBitmap:Bitmap, highlightBitmap:Bitmap, xCount:Int, yCount:Int, spritePosX : Int, spritePosY : Int, dest:Rect, iconPaint:Paint, highlightPaint:Paint, selected:Boolean = false , enabled:Boolean = true){
    if(selected && enabled) drawSubImage(highlightBitmap, xCount, yCount, spritePosX, spritePosY, dest, highlightPaint, enabled)
    drawSubImage(iconBitmap, xCount, yCount, spritePosX, spritePosY, dest, iconPaint, enabled)
}

fun Canvas.drawText(text:String, x:Float, y:Float, paint: Paint, yOffset:Float){
    drawText(text, x, y - (paint.textSize * yOffset), paint)
}

fun Canvas.drawProgressBar34Sprite(bitmap:Bitmap,size:RectF,progress:Float,paint:Paint){
    // bounds
    Temp.tempRectF.set(size)
    val ySize= size.height()
    var left   =   PointF(Temp.tempRectF.left, Temp.tempRectF.top)
    var center = PointF(Temp.tempRectF.left + ySize, Temp.tempRectF.top)
    var right  =  PointF(Temp.tempRectF.right - ySize, Temp.tempRectF.top)
    var xSize = Temp.tempRectF.width() - (ySize * 2)

    drawSubImage(bitmap, 3,2,0,0,left.x  ,left.y,ySize,paint)
    drawSubImage(bitmap, 3,2,1,0,center.x,center.y,xSize,ySize,paint)
    drawSubImage(bitmap, 3,2,2,0,right.x ,right.y,ySize,paint)

    Temp.tempRectF.set(size.left, size.top, progress.toLerp(size.left,size.right), size.bottom)
    left = PointF(Temp.tempRectF.left, Temp.tempRectF.top)
    center = PointF(Temp.tempRectF.left + ySize, Temp.tempRectF.top)
    right = PointF((Temp.tempRectF.right - ySize).coerceAtLeast(center.x), Temp.tempRectF.top)
    xSize = Temp.tempRectF.width() - (ySize * 2)
    drawSubImage(bitmap, 3,2,0,1,left.x,left.y,ySize,paint)
    drawSubImage(bitmap, 3,2,1,1,center.x,center.y,xSize,ySize,paint)
    drawSubImage(bitmap, 3,2,2,1,right.x,right.y,ySize,paint)
}

fun Bitmap.getRect():Rect{
    return Rect(0,0,width,height)
}

fun String.asPathGetFileName():String{
    return this.split(File.separatorChar).last().split(".").first()
}
fun String.asPathGetFileExtension():String{
    return this.split(File.separatorChar).last().split(".").last()
}
fun Canvas.drawFormat(bitmap:Bitmap,format:String,position:Point,scale:Float,bitmapPaint:Paint,textPaint: TextPaint){

    val oriAlign = textPaint.textAlign
    val formatStr = format.toUpperCase(Locale.ROOT)
    val oriTypeface = textPaint.typeface

    // calculate padding and rects
    textPaint.typeface = Typeface.create(oriTypeface, Typeface.BOLD)
    val yPadding = (4 * scale).toInt()
    val xPadding = (10 * scale).toInt()
    val retval = Rect(0,0,0,0)
    textPaint.getTextBounds(formatStr, 0, formatStr.length, retval)
    val w = (retval.width()/2) + xPadding
    val h = (retval.height()/2) + yPadding
    Temp.tempRect.set(position.x - w, position.y - h, position.x + w, position.y + h )

    // draw background
    drawBitmap(bitmap, bitmap.getRect(), Temp.tempRect, bitmapPaint)

    // draw text
    textPaint.textAlign = Paint.Align.CENTER
    drawText(formatStr, position.x.toFloat(), position.y.toFloat(), textPaint, -0.25f)

    textPaint.textAlign = oriAlign
    textPaint.typeface = oriTypeface
}

enum class XYScaling {
    Width,
    Height,
    Square
}

fun Drawable.toBitmap(desiredSize:Int, scaling:XYScaling, config:Bitmap.Config):Bitmap{
    val scaleFactor = when(scaling){
        XYScaling.Width -> {
            desiredSize / intrinsicWidth.toFloat()
        }
        XYScaling.Height -> {
            desiredSize / intrinsicHeight.toFloat()
        }
        else -> {1.0f}
    }

    val w = if(scaling == XYScaling.Square) desiredSize else (intrinsicWidth * scaleFactor).toInt()
    val h = if(scaling == XYScaling.Square) desiredSize else (intrinsicHeight * scaleFactor).toInt()

    return toBitmap(w,h,config)
}

/**
 * Lambda extension to execute an operation when the [T]? (Nullable of Any type) is not null
 * @param operation What to do when this variable is not null
 */
fun <T> T?.whenNotNull(operation: (T) -> Unit){ if(this != null) operation.invoke(this) }

fun <T> T?.whenNullAndNot(onNull: () -> Unit, onNotNull: (T) -> Unit){
    if(this != null) onNotNull.invoke(this)
    else onNull.invoke()
}