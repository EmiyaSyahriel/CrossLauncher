package id.psw.vshlauncher.customtypes

import android.graphics.Canvas

internal fun Canvas.drawSSA(source:String, x:Float, y:Float){ SSADrawing(source, x, y).draw(this) }
internal fun Canvas.drawSSA(ssa:SSADrawing, x:Float, y:Float){
    ssa.x = x
    ssa.y = y
    ssa.draw(this)
}