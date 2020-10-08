package id.psw.vshlauncher

import android.app.Activity
import android.content.res.Configuration
import android.graphics.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.graphics.minus
import androidx.core.graphics.toRectF
import androidx.core.view.ViewCompat
import java.lang.Math.round

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
    val spriteRect = Rect(sprPosX * sprSizeX, sprPosY * sprSizeX, (sprPosX+1)*sprSizeX, (sprPosY +1) * sprSizeY)
    this.drawBitmap(bitmap,spriteRect, dest, paint)
}
fun Canvas.drawSubImage(bitmap:Bitmap, count:Point, spritePos:Point, drawPos:Point, drawSize:Point, paint:Paint){
    val sprSizeX = bitmap.width/ count.x
    val sprSizeY = bitmap.height/count.y
    val sprPosX = spritePos.x % count.x
    val sprPosY = spritePos.y % count.y
    val spriteRect = Rect(sprPosX * sprSizeX, sprPosY * sprSizeX, (sprPosX+1)*sprSizeX, (sprPosY +1) * sprSizeY)
    val dest = Rect(drawPos.x, drawPos.y, drawPos.x + drawSize.x, drawPos.y + drawSize.y)
    this.drawBitmap(bitmap,spriteRect, dest, paint)
}
fun Canvas.drawSubImage(bitmap:Bitmap, count:Point, spritePos:Point, drawPos:Point, drawSize:Int, paint:Paint){
    val sprSizeX = bitmap.width/ count.x
    val sprSizeY = bitmap.height/count.y
    val sprPosX = spritePos.x % count.x
    val sprPosY = spritePos.y % count.y
    val spriteRect = Rect(sprPosX * sprSizeX, sprPosY * sprSizeX, (sprPosX+1)*sprSizeX, (sprPosY +1) * sprSizeY)
    val dest = Rect(drawPos.x, drawPos.y, drawPos.x + drawSize, drawPos.y + drawSize)
    this.drawBitmap(bitmap,spriteRect, dest, paint)
}
fun Canvas.drawSubImage(bitmap:Bitmap, xCount:Int, yCount:Int, spritePosX : Int, spritePosY : Int, drawPosX:Int, drawPosY : Int, drawSizeX:Int, drawSizeY : Int, paint:Paint){
    val sprSizeX = bitmap.width/ xCount
    val sprSizeY = bitmap.height/ yCount
    val sprPosX = spritePosX % xCount
    val sprPosY = spritePosY % yCount
    val spriteRect = Rect(sprPosX * sprSizeX, sprPosY * sprSizeX, (sprPosX+1)*sprSizeX, (sprPosY +1) * sprSizeY)
    val dest = Rect(drawPosX, drawPosY, drawPosX + drawSizeX, drawPosY + drawSizeY)
    this.drawBitmap(bitmap,spriteRect, dest, paint)
}

fun Canvas.drawSubImage(bitmap:Bitmap, xCount:Int, yCount:Int, spritePosX : Int, spritePosY : Int, drawPosX:Int, drawPosY : Int, drawSize:Int, paint:Paint){
    val sprSizeX = bitmap.width/ xCount
    val sprSizeY = bitmap.height/ yCount
    val sprPosX = spritePosX % xCount
    val sprPosY = spritePosY % yCount
    val spriteRect = Rect(sprPosX * sprSizeX, sprPosY * sprSizeX, (sprPosX+1)*sprSizeX, (sprPosY +1) * sprSizeY)
    val dest = Rect(drawPosX, drawPosY, drawPosX + drawSize, drawPosY + drawSize)
    this.drawBitmap(bitmap,spriteRect, dest, paint)
}

fun Canvas.drawSubImage(bitmap:Bitmap, xCount:Int, yCount:Int, spritePosX : Int, spritePosY : Int, dest:Rect, paint:Paint, enabled:Boolean = true){
    val sprSizeX = bitmap.width/ xCount
    val sprSizeY = bitmap.height/ yCount
    val sprPosX = spritePosX % xCount
    val sprPosY = spritePosY % yCount
    val spriteRect = Rect(sprPosX * sprSizeX, sprPosY * sprSizeX, (sprPosX+1)*sprSizeX, (sprPosY +1) * sprSizeY)
    paint.alpha = if(enabled) 255 else 100
    this.drawBitmap(bitmap,spriteRect, dest, paint)
}

fun Canvas.drawSubImage(iconBitmap:Bitmap, highlightBitmap:Bitmap, xCount:Int, yCount:Int, spritePosX : Int, spritePosY : Int, dest:Rect, iconPaint:Paint, highlightPaint:Paint, selected:Boolean = false , enabled:Boolean = true){
    if(selected && enabled) drawSubImage(highlightBitmap, xCount, yCount, spritePosX, spritePosY, dest, highlightPaint, enabled)
    drawSubImage(iconBitmap, xCount, yCount, spritePosX, spritePosY, dest, iconPaint, enabled)
}