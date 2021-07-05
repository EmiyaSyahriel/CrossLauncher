package id.psw.vshlauncher.views.VshServerSubcomponent

object Time {
    private var lastTime = 0L
    var deltaTime = 0f
    var currentTime = 0f
    fun updateTime(){
        val cmillis = System.currentTimeMillis()
        deltaTime = (cmillis - lastTime) / 1000f
        lastTime = cmillis
        // avoiding overflow
        currentTime = (currentTime + deltaTime) % 131072f
    }
}
