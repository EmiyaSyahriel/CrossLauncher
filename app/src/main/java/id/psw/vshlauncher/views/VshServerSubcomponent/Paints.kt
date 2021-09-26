package id.psw.vshlauncher.views.VshServerSubcomponent

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import id.psw.vshlauncher.toLerp

object Paints {
    var backgroundColor = Color.argb(255,0,0,0)
    var backgroundAlpha = 0.5f
    private var backgroundLerpAlpha = 0.0f
    var vshBackground : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { }
    var itemTitleSelected : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 30f
        color=  Color.WHITE
        setShadowLayer(10f, 0f,0f, Color.WHITE)
    }
    val menuBackground: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(64,255,255,255)
    }
    var itemTitleUnselected : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 30f
        color =  Color.argb(128,255,255,255)
    }
    var categoryTitleSelected : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 20f
        color=  Color.WHITE
        setShadowLayer(5f, 0f,0f, Color.WHITE)
    }
    var categoryTitleUnselected : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 20f
        color=  Color.GRAY
    }
    var statusPaint : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 25f
        color=  Color.WHITE
    }
    var itemSubtitleSelected : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 20f
        color=  Color.WHITE
        setShadowLayer(5f, 0f,0f, Color.WHITE)
    }
    var itemSubtitleUnselected : Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 20f
        color=  Color.argb(128,255,255,255)
    }

    fun setFont(tf: Typeface){
        arrayOf(itemSubtitleSelected, itemSubtitleUnselected, itemTitleSelected, itemTitleUnselected).forEach {
            it.typeface = tf
        }
    }

    fun updatePaints()
    {
        backgroundLerpAlpha = Time.deltaTime .toLerp(backgroundLerpAlpha, backgroundAlpha).coerceIn(0f, 1f)
        vshBackground.color = Color.argb(
            backgroundLerpAlpha.toLerp(0,255),
            Color.red(backgroundColor),
            Color.green(backgroundColor),
            Color.blue(backgroundColor)
        )
    }
}