package id.psw.vshlauncher

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import java.io.File
import java.util.*

/**
 * View that will be shown on Launcher Startup
 *
 * Mimics PS3 Game OS Boot Animation / Cold Boot
 */
@Suppress("DEPRECATION")
class VshColdBoot : View {

    val TAG = "coldboot.raf"
    var onFinishAnimation = Runnable {  }
    var finishCalled = false
    var coldbootImage : Bitmap = ColorDrawable(Color.TRANSPARENT).toBitmap(128,128)
    var paint = Paint(Paint.ANTI_ALIAS_FLAG)

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.VshColdBoot, defStyle, 0
        )
        a.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        loadDefaultColdBootImage()
        loadCustomColdBootImage()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun loadDefaultColdBootImage(){
        val coldbootBmp = context.resources.getDrawable(R.drawable.coldboot_internal).toBitmap(1280, 720)
        val scale = (width < height).choose(width.toFloat() / coldbootBmp.width, height.toFloat() / coldbootBmp.height)
        coldbootImage = coldbootBmp.scale((scale * coldbootBmp.width).toInt(), (scale * coldbootBmp.height).toInt())
    }

    private fun loadCustomColdBootImage(){
        try{
            val dataFolder = context.getExternalFilesDir("")?.listFiles()
            val coldboots = dataFolder?.filter { it -> it.name.toLowerCase(Locale.ROOT).endsWith("coldboot.png") }
            if(coldboots != null){
                if(coldboots.isNotEmpty()){
                    val file = coldboots[0]
                    if(file.exists()){
                        val coldbootBmp = BitmapFactory.decodeStream(file.inputStream())
                        val scale = (width < height).choose(width / coldbootBmp.width, height / coldbootBmp.height)
                        coldbootImage = coldbootBmp.scale(scale * coldbootBmp.width, scale * coldbootBmp.height)
                    }
                }
            }
        }catch (e:Exception){

        }
    }

    var frame = 0
    var maxframe = 300

    fun mUpdate(canvas: Canvas){
        if(!finishCalled && frame >= maxframe){
            onFinishAnimation.run()
            finishCalled = true
        }

        val max5th = maxframe / 5f

        if(frame <= max5th){
            paint.alpha = (255 * (frame/max5th)).toInt()
            canvas.drawColor(Color.argb(255-(paint.alpha /2) , 0,0,0))
        }else if(frame >= (4 * max5th)){
            paint.alpha = (255 * (1f - ((frame - (4f * max5th)) / max5th))).toInt()
            canvas.drawColor(Color.argb(paint.alpha/2, 0,0,0))
        }else{
            canvas.drawColor(Color.argb(paint.alpha /2 , 0,0,0))
        }

        val xPad = (width/2f) - (coldbootImage.width/2f)
        val yPad = (height/2f) - (coldbootImage.height/2f)
        canvas.drawBitmap(coldbootImage,xPad,yPad,paint)
        if(frame <= maxframe) frame++
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mUpdate(canvas)
    }
}
