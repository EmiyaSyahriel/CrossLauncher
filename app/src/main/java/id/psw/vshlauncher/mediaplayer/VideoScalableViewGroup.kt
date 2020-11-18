package id.psw.vshlauncher.mediaplayer

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.ViewGroup

class VideoScalableViewGroup : ViewGroup {

    enum class ScalingMode{
        OneByOne,
        Fit,
        Fill,
        Stretch
    }

    constructor(context: Context) : super(context) {init()}
    constructor(context: Context, attrs:AttributeSet) : super(context,attrs) {init()}
    constructor(context: Context, attrs:AttributeSet,styleRes:Int) : super(context,attrs,styleRes) {init()}

    private var currentScaling = ScalingMode.Fit

    private val screenAR :Float
        get() {
            return if(width > height) width / height.toFloat() else height / width.toFloat()
        }

    private val videoAR : Float
    get(){
        var retval = 16f/9f
        val firstChild = getChildAt(0)
        if(firstChild is VSHVideoView){
            val res = videoRes
            retval = res.x / res.y.toFloat()
        }
        return retval
    }
    private val videoRes : Point
    get(){
        var res = Point(1280,720)
        val firstChild = getChildAt(0)
        if(firstChild is VSHVideoView){
            res = firstChild.getVideoResoultion()
        }
        return res
    }
    private val fitScale: Point get(){
        val point = Point(1280,720)
        var scaleFactor = 1.0f
        val res = videoRes

        if(screenAR > videoAR){
            scaleFactor = height / res.y.toFloat()
        }else{
            scaleFactor = width / res.x.toFloat()
        }

        point.x = (res.x * scaleFactor).toInt()
        point.y = (res.y * scaleFactor).toInt()
        return point
    }
    private val fillScale: Point get(){
        val point = Point(1280,720)
        val scaleFactor: Float
        val res = videoRes

        if(screenAR < videoAR){
            scaleFactor = height / res.y.toFloat()
        }else{
            scaleFactor = width / res.x.toFloat()
        }

        point.x = (res.x * scaleFactor).toInt()
        point.y = (res.y * scaleFactor).toInt()
        return point
    }

    private fun getMeasureWidth():Int{
        return when(currentScaling){
            ScalingMode.Fit -> {
                fitScale.x
            }
            ScalingMode.Fill -> {
                fillScale.x
            }
            ScalingMode.Stretch ->{
                width
            }
            ScalingMode.OneByOne ->{
                videoRes.x
            }
        }
    }

    private fun getMeasureHeight():Int{

        return when(currentScaling){
            ScalingMode.Fit -> { fitScale.y }
            ScalingMode.Fill -> { fillScale.y }
            ScalingMode.Stretch -> { height }
            ScalingMode.OneByOne ->{ videoRes.y }
        }
    }

    private fun getMeasureSpecWidth() : Int{
        val size = getMeasureWidth()
        val mode = MeasureSpec.EXACTLY
        return MeasureSpec.makeMeasureSpec(size, mode)
    }

    private fun getMeasureSpecHeight() : Int{
        val size = getMeasureHeight()
        val mode = MeasureSpec.EXACTLY
        return MeasureSpec.makeMeasureSpec(size, mode)
    }

    private fun doMeasuring(){
        if(childCount >= 1){
            val selView = getChildAt(0)
            selView.measure(getMeasureSpecWidth(), getMeasureSpecHeight())
        }
    }

    private fun doLayouting(){
        if(childCount >= 1){
            val selView = getChildAt(0)
            val w = getMeasureWidth()
            val h = getMeasureHeight()
            val l = (width/2) - (w/2)
            val t = (height/2) - (h/2)
            val r = (width/2) + (w/2)
            val b = (height/2) + (h/2)
            selView.layout(l,t,r,b)
        }
    }

    private fun init(){

    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        doLayouting()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        doMeasuring()
    }

    fun switchScaling(){
        currentScaling = when(currentScaling){
            ScalingMode.OneByOne -> ScalingMode.Fit
            ScalingMode.Fit -> ScalingMode.Fill
            ScalingMode.Fill -> ScalingMode.Stretch
            ScalingMode.Stretch -> ScalingMode.OneByOne
        }
        doMeasuring()
        doLayouting()
    }
}