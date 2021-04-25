package id.psw.vshlauncher.customtypes

import android.graphics.*
import androidx.core.graphics.withClip
import androidx.core.graphics.withTranslation

/**
 * (more) Advanced SSA Path renderer
 * @param sourcePath Advanced Substation Alpha Drawing (Google, that's A_S for you)
 * @param x Horizontal Offset
 * @param y Vertical Offset
 * @param w Clip Width
 * @param h Clip Height
 *
 * Commands:
 * - "m {x} {y}" - Move to
 * - "l {x} {y}" - Line to
 * - "b {x} {y} {cx1} {cy1} {cx2} {cy2}" - Cubic Curve To
 * - "f {a} {r} {g} {b}" - Fill with Color
 * - "s {a} {r} {g} {b}" - Stroke with Color
 * - "ss {w}" - Stroke Width
 * */
data class SSADrawing (val sourcePath:String, var x:Float, var y:Float){
    private data class SSAPath(val command:String, val num: ArrayList<Float>)
    private val paths = ArrayList<SSAPath>()

    init {
        val items = sourcePath.split(' ','\n','\r').filter { it.isNotEmpty() }.toList()
        var currentItem = ""
        var currentItemParam = arrayListOf<Float>()
        items.forEach {
            val parsed = it.toFloatOrNull()
            if(S.validSources.contains(it)){
                if(currentItem.isNotEmpty()) paths.add(SSAPath(currentItem, currentItemParam))
                currentItem = it
                currentItemParam = arrayListOf()
            }else if(parsed != null){
                currentItemParam.add(parsed)
            }
            if(currentItem.isNotEmpty()) paths.add(SSAPath(currentItem, currentItemParam))
        }
    }

    private object S {
        var validSources = arrayListOf("m","l","b","s","f","ss")
        var fill = Paint(Paint.ANTI_ALIAS_FLAG).apply{
            style = Paint.Style.FILL
        }
        var outline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }
    }

    private var path: Path = Path()

    private fun moveTo(src:SSAPath){
        if(src.num.size >= 2){
            if(!path.isEmpty) path.close()
            path.moveTo(src.num[0], src.num[1])
        }
    }
    private fun lineTo(src:SSAPath){ if(src.num.size >= 2){ path.lineTo(src.num[0], src.num[1]) } }
    private fun bezierTo(src:SSAPath){ if(src.num.size >= 6){ path.cubicTo(src.num[0], src.num[1],src.num[2], src.num[3],src.num[4], src.num[5]) } }
    private fun fill(src:SSAPath, ctx: Canvas){
        if(src.num.size>=4){
            val a = src.num[0].toInt()
            val r = src.num[1].toInt()
            val g = src.num[2].toInt()
            val b = src.num[3].toInt()
            S.fill.color = Color.argb(a,r,g,b)
        }
        path.close()
        ctx.withTranslation(x,y) {
            ctx.drawPath(path, S.fill)
        }
        path.reset()
    }

    private fun strokeSize(src:SSAPath){
        if(src.num.size>=1){
            S.outline.strokeWidth = src.num[1]
        }
    }

    private fun stroke(src:SSAPath, ctx: Canvas){
        if(src.num.size>=4){
            val a = src.num[0].toInt()
            val r = src.num[1].toInt()
            val g = src.num[2].toInt()
            val b = src.num[3].toInt()
            S.outline.color = Color.argb(a,r,g,b)
        }
        path.close()
        ctx.withTranslation(x,y) {
            ctx.drawPath(path, S.outline)
        }
        path.reset()
    }

    fun draw(ctx: Canvas){
        path.reset()
        //ctx.withClip(RectF(0f,0f,w,h)){
        paths.forEach{
            when(it.command){
                "m" -> moveTo(it)
                "l" -> lineTo(it)
                "b" -> bezierTo(it)
                "ss" -> strokeSize(it)
                "s" -> stroke(it, ctx)
                "f" -> fill(it, ctx)
            }
        }
        //}

    }
}