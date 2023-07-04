package id.psw.vshlauncher.views.dialogviews

import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import id.psw.vshlauncher.*
import id.psw.vshlauncher.types.FileQuery
import id.psw.vshlauncher.views.VshViewPage
import id.psw.vshlauncher.views.XmbDialogSubview
import java.io.File

class CustomResourceListDialogView(private val vsh: VSH) :  XmbDialogSubview(vsh) {
    private val totalScrollRectF = RectF()
    private var offsetScroll = 0.0f
    private val lastPointF = PointF()
    private var maxHeight = 0.0f

    override val hasNegativeButton: Boolean
        get() = true

    override val title: String
        get() = vsh.getString(R.string.debug_custom_resource_dialog_title)

    private val textList = arrayListOf<Pair<String, String>>()

    private fun findUsage(usage:String, files:ArrayList<File>){
        var found = ""
        files.forEach {
            if(found.isEmpty()){
                if(it.exists()){
                    found = it.absolutePath
                }
            }
        }
        found = found.isEmpty().select(vsh.getString(R.string.common_not_found), "Found : $found")
        textList.add(usage to found)
    }

    override fun onStart() {
        arrayListOf(
            SFXType.Selection to "select",
            SFXType.Confirm to "confirm",
            SFXType.Cancel to "cancel"
        ).forEach {
            findUsage("SFX - ${it.first}", FileQuery(VshBaseDirs.VSH_RESOURCES_DIR).atPath("sfx").withNames(it.second).withExtensionArray(VshResTypes.SOUNDS).execute(vsh))
        }
    }

    override fun onTouch(a: PointF, b: PointF, act: Int) {
        if(act == MotionEvent.ACTION_MOVE){
            val delta = b.x - lastPointF.x
            offsetScroll += delta
            lastPointF.set(b)
        }
    }

    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime: Float) {
        maxHeight = drawBound.height()
    }

    override fun onDialogButton(isPositive: Boolean) {
        finish(VshViewPage.MainMenu)
    }
}