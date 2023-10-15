package id.psw.vshlauncher.submodules

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import id.psw.vshlauncher.Logger
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.sdkAtLeast
import id.psw.vshlauncher.views.asBytes
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class BitmapManager(private val ctx: Vsh) : IVshSubmodule {
    companion object {
        lateinit var instance : BitmapManager
        private const val TAG = "Bitman"
    }

    private data class BitmapCache (
        val id : String,
        var bitmap : Bitmap?,
        var loadMutex : Mutex,
        var isLoading : Boolean = false,
        var isLoaded : Boolean = false,
        var refCount : Int = 0
            )

    private val cacheAccessLock = Mutex(false)
    private val cache = arrayListOf<BitmapCache>()
    private lateinit var dclWhite : Bitmap
    private lateinit var dclBlack : Bitmap
    private lateinit var dclTrans : Bitmap
    private var keepLoader : Boolean = false
    private var printDebug = false
    private fun approximateBitmapSize(bmp: Bitmap) : Int{
        var pxp = when(bmp.config){
            Bitmap.Config.ARGB_8888 -> 4
            Bitmap.Config.ARGB_4444 -> 2
            Bitmap.Config.RGB_565 -> 2
            Bitmap.Config.ALPHA_8 -> 1
            else -> 1
        }
        pxp = if(sdkAtLeast(Build.VERSION_CODES.O)){
            when(bmp.config){
                Bitmap.Config.RGBA_F16 -> 8
                Bitmap.Config.HARDWARE -> 16
                else -> pxp
            }
        }else{
            pxp
        }

        return pxp * bmp.width * bmp.height
    }

    val bitmapCount : Int get() = cache.count { it.bitmap != null }

    val totalCacheSize : Long get() {
        var l = 0L

        runBlocking {
            cacheAccessLock.withLock {
                cache.forEach {
                    val bmp = it.bitmap
                    if(bmp != null){
                        l += approximateBitmapSize(bmp).toLong()
                    }
                }
            }
        }
        return l
    }

    val queueCount : Int get() {
        return runBlocking {
            queueMutex.withLock {
                loadQueue.size
            }
        }
    }

    override fun onCreate() {
        instance = this
        ctx.threadPool.execute { loaderThreadMain() }
    }

    override fun onDestroy() {
        // TODO: Clean bitmaps from memory
    }

    private val loadQueue = arrayListOf<BitmapRef>()
    private val queueMutex = Mutex(false)

    private fun loaderThreadMain(){
        dclWhite = Bitmap.createBitmap(intArrayOf(Color.WHITE), 0, 1, 1, 1, Bitmap.Config.ARGB_8888)
        dclBlack = Bitmap.createBitmap(intArrayOf(Color.BLACK), 0, 1, 1, 1, Bitmap.Config.ARGB_8888)
        dclTrans = Bitmap.createBitmap(intArrayOf(Color.TRANSPARENT), 0, 1, 1, 1, Bitmap.Config.ARGB_8888)
        keepLoader = true
        while(keepLoader){
            runBlocking {
                queueMutex.lock(this)
                while(loadQueue.isNotEmpty()){
                    val q = loadQueue.first()
                    queueMutex.unlock(this)

                    val h = findHandle(q) ?: addHandle(q)

                    try{
                        h.bitmap = q.loader()
                        val bmp = h.bitmap
                        if(bmp != null){
                            val sz = approximateBitmapSize(bmp).toLong()
                            h.isLoaded = true
                            Logger.i(TAG, "[${q.id}] Loaded - ${sz.asBytes()}")
                        }else{
                            Logger.w(TAG, "[${q.id}] Load Failed")
                            h.isLoaded = false
                        }
                    }catch (e:Exception){
                        e.printStackTrace()
                        h.isLoaded = false
                    }

                    h.isLoading = false

                    queueMutex.lock(this)
                    loadQueue.removeAt(0)
                }

                cleanup()

                queueMutex.unlock(this)
                delay(100L)
            }
        }
    }

    private fun addHandle(ref: BitmapRef) : BitmapCache {
        return runBlocking {
            cacheAccessLock.withLock {
                val h = BitmapCache(
                ref.id,
                null,
                Mutex(false), isLoading = true, isLoaded = false, 0
                )
                cache.add(h)
                h
            }
        }
    }
    private fun remHandle(h: BitmapCache) : Boolean {
        return runBlocking {
            cacheAccessLock.withLock {
                cache.remove(h)
            }
        }
    }

    private fun findHandle(ref: BitmapRef) : BitmapCache? {
        return runBlocking {
            cacheAccessLock.withLock {
                cache.find {it.id == ref.id}
            }
        }
    }

    fun load(bitmapRef: BitmapRef) {
        val handle : BitmapCache = findHandle(bitmapRef) ?: runBlocking {
            val h = addHandle(bitmapRef)
            queueMutex.withLock {
                loadQueue.add(bitmapRef)
            }
            h
        }
        handle.refCount++

        if(handle.refCount > 1){
            Logger.i(TAG, "[${handle.id}] - Reference Add : ${handle.refCount}")
        }else{
            Logger.i(TAG, "[${handle.id}] - Load Queued")
        }
    }

    fun cleanup(){
        for(ch in cache){
            if(ch.refCount <= 0){
                ch.bitmap?.recycle()
            }
        }

        cache.removeAll { it.refCount == 0 }
    }

    fun get(bitmapRef: BitmapRef) : Bitmap =
        findHandle(bitmapRef)?.bitmap ?: when(bitmapRef.defColor){
            BitmapRef.FallbackColor.Black -> dclBlack
            BitmapRef.FallbackColor.White -> dclWhite
            BitmapRef.FallbackColor.Transparent -> dclTrans
        }

    fun isLoading(bitmapRef: BitmapRef) : Boolean =
        findHandle(bitmapRef)?.isLoading == true

    fun release(bitmapRef: BitmapRef){
        val handle = findHandle(bitmapRef)
        if(handle != null){
            handle.refCount--

            Logger.i(TAG, "[${handle.id}] - Reference Min ${handle.refCount}")
            if(handle.refCount <= 0){
                // Recycle Bitmap Inside Handle
                val bmp = handle.bitmap
                val sz = if(bmp != null){ approximateBitmapSize(bmp) }else{ 0 }.toLong()
                handle.bitmap = null
                handle.isLoaded = false
                remHandle(handle)
                bmp?.recycle()
                Logger.i(TAG, "[${handle.id}] - Unloaded ${sz.asBytes()}")
            }
            // Call GC so unused bitmap will be removed right away
            System.gc()
        }
    }

    fun releaseAll(){
        for(handle in cache){
            handle.bitmap?.recycle()
            handle.loadMutex.unlock(this)
        }
        cache.clear()
    }

    fun isLoaded(ref: BitmapRef): Boolean = findHandle(ref)?.isLoaded == true
}