package id.psw.vshlauncher

import android.graphics.Color

object FColor {
    enum class ColorMask(val v:Int){
        ALPHA(0b1000),
        RED(0b0100),
        GREEN(0b0010),
        BLUE(0b0001)
    }

    private fun Float.mByte() : Int = (this * 255).toInt().coerceIn(0,255)
    fun argb(a:Float,r:Float,g:Float,b:Float) : Int = Color.argb(a.mByte(),r.mByte(),g.mByte(),b.mByte())
    fun setComponent(baseColor:Int, cValue:Float,mask:ColorMask) : Int =
        Color.argb(
            mask.v.hasFlag(ColorMask.ALPHA.v).select(cValue.mByte(), Color.alpha(baseColor)),
            mask.v.hasFlag(ColorMask.RED.v).select(cValue.mByte(), Color.red(baseColor)),
            mask.v.hasFlag(ColorMask.GREEN.v).select(cValue.mByte(), Color.green(baseColor)),
            mask.v.hasFlag(ColorMask.BLUE.v).select(cValue.mByte(), Color.blue(baseColor))
        )

    fun setAlpha(baseColor:Int, cValue:Float) : Int = setComponent(baseColor, cValue, ColorMask.ALPHA)
    fun setRed(baseColor:Int, cValue:Float) : Int = setComponent(baseColor, cValue, ColorMask.RED)
    fun setGreen(baseColor:Int, cValue:Float) : Int = setComponent(baseColor, cValue, ColorMask.GREEN)
    fun setBlue(baseColor:Int, cValue:Float) : Int = setComponent(baseColor, cValue, ColorMask.BLUE)

    fun getAlpha(baseColor:Int) : Float = Color.red(baseColor) / 255.0f
    fun getRed(baseColor:Int) : Float = Color.red(baseColor) / 255.0f
    fun getGreen(baseColor:Int) : Float = Color.red(baseColor) / 255.0f
    fun getBlue(baseColor:Int) : Float = Color.red(baseColor) / 255.0f

}