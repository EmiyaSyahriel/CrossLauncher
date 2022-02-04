package id.psw.vshlauncher.types.sequentialimages

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.media.MediaMetadataRetriever
import id.psw.vshlauncher.FittingMode
import id.psw.vshlauncher.views.drawBitmap
import java.util.concurrent.Executors

class XMBAnimMMR(file:String) : XMBFrameAnimation() {
    companion object{
        private val frameFetchingThreadPool = Executors.newFixedThreadPool(16)
    }

    private val retriever = MediaMetadataRetriever().apply {
        setDataSource(file)
    }

    override val frameCount: Int get() {
        var retval = 0
        try{
            retval = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0
        }catch(e:Exception){
        }
        return retval
    }

    override var currentTime: Float = 0f
    private var pHasRecycled = false
    private var pDuration = (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION) ?: "0").toInt()
    override val hasRecycled: Boolean get() = pHasRecycled
    private val compBitmap = Bitmap.createBitmap(320,176,Bitmap.Config.ARGB_8888)
    private val tmpRectF = RectF(0f,0f,320f,176f)
    private val composer = Canvas(compBitmap)

    private fun dispatchImageGetter(){
        frameFetchingThreadPool.execute {
            val dTime = (((currentTime * 1000) % pDuration) * 1000).toInt()
            val frame = retriever.getFrameAtTime(dTime.toLong(), MediaMetadataRetriever.OPTION_CLOSEST)
            if(frame != null){
                //synchronized(composer){
                    composer.drawBitmap(frame, null, tmpRectF, null, FittingMode.FIT, 0.5f, 0.5f)
                    frame.recycle()
                //}
            }
        }
    }

    override fun getFrame(deltaTime: Float): Bitmap {
        if(hasRecycled) throw IllegalAccessException("Image has been destroyed.")
        currentTime += deltaTime
        dispatchImageGetter()
        return compBitmap
    }

    override fun recycle() {
        try{
            pHasRecycled = true
            compBitmap.recycle()
            retriever.release()
        }catch(e:Exception){}
    }

}