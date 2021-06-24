package id.psw.vshlauncher.livewallpaper

import android.content.res.AssetManager

object NativeGL {
    init{
        System.loadLibrary("wave")
    }
    external fun create()
    external fun draw(deltaTime:Float)
    external fun setAssetManager(mgr:AssetManager)
    external fun setup(w:Int, h:Int)
    external fun destroy()
    external fun setPaused(paused : Boolean)
    external fun getPaused() : Boolean
}