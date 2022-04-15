package id.psw.vshlauncher.views.dialogviews

import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.submodules.GamepadSubmodule
import id.psw.vshlauncher.views.VshViewPage
import id.psw.vshlauncher.views.XmbDialogSubview

class UITestDialogView(vsh: VSH) : XmbDialogSubview(vsh) {
    override val title: String = "::TEST::"

    override val hasNegativeButton: Boolean = true

    override val negativeButton: String = "Back"

    override fun onDialogButton(isPositive: Boolean) {
        finish(VshViewPage.MainMenu)
    }
    private var testBarValue = 50.0f

    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime:Float) {
        val xCenterL = drawBound.centerX() - 100f
        val xCenterR = drawBound.centerX() + 100f
        SubDialogUI.progressBar(ctx,0.0f, 100.0f, testBarValue, xCenterR, 100.0f, 200.0f)
    }

    override fun onGamepad(key: GamepadSubmodule.Key, isPress: Boolean): Boolean {
        var retval = false
        if(isPress){
            when(key){
                GamepadSubmodule.Key.PadR -> {
                    testBarValue = (testBarValue + 1.0f).coerceIn(0.0f, 100.0f)
                    retval = true
                }
                GamepadSubmodule.Key.PadL -> {
                    testBarValue = (testBarValue - 1.0f).coerceIn(0.0f, 100.0f)
                    retval = true
                }
                else -> {}
            }
        }
        return retval
    }
}