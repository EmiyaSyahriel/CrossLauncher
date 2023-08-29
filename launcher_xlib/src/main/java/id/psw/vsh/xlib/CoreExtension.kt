package id.psw.vsh.xlib

import android.app.Activity
import android.os.Bundle

// Dummy class, used as query filter decoy
class CoreExtension : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish() // Just quit
    }
}