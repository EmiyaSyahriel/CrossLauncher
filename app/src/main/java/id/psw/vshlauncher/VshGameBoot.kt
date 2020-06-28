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
import androidx.core.graphics.withScale
import java.lang.Exception
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * TODO: document your custom view class.
 */
class VshGameBoot : View {

    companion object{
        const val TAG = "gameboot.raf"
    }
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
            attrs, R.styleable.VshGameBoot, defStyle, 0
        )
        a.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        Log.d(TAG, "Size Change detected")
        loadDefaultColdBootImage()
        loadCustomColdBootImage()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun loadDefaultColdBootImage(){
        val coldbootBmp = context.resources.getDrawable(R.drawable.gameboot_internal).toBitmap(1280, 720)
        val scale = (width > height).choose(width.toFloat() / coldbootBmp.width, height.toFloat() / coldbootBmp.height)
        coldbootImage = coldbootBmp.scale((scale * coldbootBmp.width).toInt(), (scale * coldbootBmp.height).toInt())
    }

    private fun loadCustomColdBootImage(){
        try{
            val dataFolder = context.getExternalFilesDir("")?.listFiles()
            Log.d(TAG, "Data files is located at : ${context.getExternalFilesDir(null)?.absolutePath}")
            dataFolder?.forEach { Log.d(TAG,"Files : ${it.name}") }
            val coldboots = dataFolder?.filter { it -> it.name.toLowerCase(Locale.ROOT).endsWith("gameboot.png") }
            if(coldboots != null){
                if(coldboots.isNotEmpty()){
                    val file = coldboots[0]
                    if(file.exists()){
                        val coldbootBmp = BitmapFactory.decodeStream(file.inputStream())
                        val scale = (width > height).choose(width / coldbootBmp.width, height / coldbootBmp.height)
                        coldbootImage = coldbootBmp.scale(scale * coldbootBmp.width, scale * coldbootBmp.height)
                    }
                }
            }
        }catch (e:Exception){

        }
    }

    var frame = 0
    var maxframe = 120
    var scale = 1f

    private fun mUpdate(canvas: Canvas){
        if(!finishCalled && frame >= maxframe){
            onFinishAnimation.run()
            finishCalled = true
        }
        if(frame <= maxframe) frame++

        val max4th = maxframe / 5f

        if(frame >= max4th * 4) {
            scale = 1f + max(0f, min(5f, (frame - (max4th * 4)) / max4th) * 5f)
            paint.alpha = 255 - (max(0f,min(1f, (frame - (max4th * 4)) / (max4th / 2f))) * 255).toInt()
            canvas.drawColor(Color.argb(255, 0,0,0))
        }
        if(frame <= max4th * 4){
            scale = 1f
            paint.alpha = 255
        }
        if(frame <= max4th){
            scale = 0.75f + max(0f, min(0.5f, (frame / max4th) * 0.25f))
            paint.alpha = (max(0f,min(1f, (frame/max4th))) * 255).toInt()
        }

        canvas.drawColor(Color.argb(paint.alpha, 0,0,0))


        val xPad = (width/2f) - (coldbootImage.width/2f)
        val yPad = (height/2f) - (coldbootImage.height/2f)
        canvas.withScale(scale,scale,width/2f,height/2f){
            canvas.drawBitmap(coldbootImage, xPad, yPad, paint)
        }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mUpdate(canvas)
    }
}
