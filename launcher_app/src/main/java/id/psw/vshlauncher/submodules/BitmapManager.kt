package id.psw.vshlauncher.submodules

import android.graphics.Bitmap
import android.graphics.Color
import id.psw.vshlauncher.VSH
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class BitmapManager {
    companion object {
        lateinit var instance : BitmapManager
    }

    private data class BitmapCache (
        val id : String,
        var bitmap : Bitmap?,
        var loadMutex : Mutex,
        var isLoading : Boolean = false,
        var refCount : Int = 0
            )

    private val cacheAccessLock = Mutex(false)
    private val caches = arrayListOf<BitmapCache>()
    private lateinit var dclWhite : Bitmap
    private lateinit var dclBlack : Bitmap
    private lateinit var dclTrans : Bitmap
    private var keepLoader : Boolean = false

    fun init(vsh: VSH){
        instance = this

        vsh.threadPool.execute { loaderThreadMain() }
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
                    }catch (e:Exception){}

                    h.isLoading = false

                    queueMutex.lock(this)
                    loadQueue.removeAt(0)
                }
                queueMutex.unlock(this)
                delay(10L)
            }
        }
    }

    private fun addHandle(ref: BitmapRef) : BitmapCache {
        return runBlocking {
            cacheAccessLock.withLock {
                val h = BitmapCache(
                ref.id,
                null,
                Mutex(false), true, 0
                )
                caches.add(h)
                h
            }
        }
    }
    private fun remHandle(h: BitmapCache) : Boolean {
        return runBlocking {
            cacheAccessLock.withLock {
                caches.remove(h)
            }
        }
    }

    private fun findHandle(ref: BitmapRef) : BitmapCache? {
        return runBlocking {
            cacheAccessLock.withLock {
                caches.find {it.id == ref.id}
            }
        }
    }

    fun load(bitmapRef: BitmapRef) {
        val handle : BitmapCache = findHandle(bitmapRef) ?: addHandle(bitmapRef)

        handle.refCount++
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

            if(handle.refCount <= 0){
                // Recycle Bitmap Inside Handle
                val bmp = handle.bitmap
                handle.bitmap = null
                remHandle(handle)
                bmp?.recycle()
            }
        }
    }

    fun releaseAll(){
        for(handle in caches){
            handle.bitmap?.recycle()
            handle.loadMutex.unlock(this)
        }
        caches.clear()
    }
}