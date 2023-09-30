package id.psw.vshlauncher

import id.psw.vshlauncher.types.XMBLoadingHandle

/**
 * Check if there is a process currently in loading,
 * Any concurrent loading will be indicated by the analog clock icon when on PS3 Layout
 */
val Vsh.hasConcurrentLoading : Boolean get() {
    synchronized(loadingHandles){
        loadingHandles.removeAll { it.hasFinished }
        return loadingHandles.size > 0
    }
}

/**
 * Add a loading handle
 */
fun Vsh.addLoadHandle() : Long {
    synchronized(loadingHandles){
        var hWnd = 0L
        while(loadingHandles.find { it.handle == hWnd } != null){
            hWnd++
        }
        loadingHandles.add(XMBLoadingHandle(hWnd, false))
        return hWnd
    }
}

/**
 * Set the handle to be finished
 */
fun Vsh.setLoadingFinished(hWnd: Long){
    synchronized(loadingHandles){
        loadingHandles.filter { it.handle == hWnd }.forEach { it.hasFinished = true }
    }
}
