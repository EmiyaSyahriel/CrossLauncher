package id.psw.vshlauncher.livewallpaper

object NativeGL {
    init{
        System.loadLibrary("wave")
    }
    external fun create()
    external fun draw(deltaTime:Float)
    external fun setup(w:Int, h:Int)
    external fun destroy()
    external fun setPaused(paused : Boolean)
    external fun getPaused() : Boolean
    external fun setWaveStyle(style: Byte)
    external fun setBackgroundColor(top:Int, bottom:Int)
    external fun setForegroundColor(edge:Int, center:Int)
    external fun setSpeed(speed:Float)
}