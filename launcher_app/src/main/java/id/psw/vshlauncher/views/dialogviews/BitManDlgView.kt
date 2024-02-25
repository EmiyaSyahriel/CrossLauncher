package id.psw.vshlauncher.views.dialogviews

import android.graphics.*
import android.view.MotionEvent
import androidx.core.graphics.withClip
import id.psw.vshlauncher.BuildConfig
import id.psw.vshlauncher.FittingMode
import id.psw.vshlauncher.R
import id.psw.vshlauncher.submodules.BitmapManager
import id.psw.vshlauncher.submodules.BitmapRef
import id.psw.vshlauncher.types.Ref
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.views.asBytes
import id.psw.vshlauncher.views.drawBitmap
import id.psw.vshlauncher.views.drawText


class BitManDlgView(v: XmbView) : XmbDialogSubview(v) {
    override val hasNegativeButton: Boolean = true
    override val hasPositiveButton: Boolean = true
    override val positiveButton: String = "Purge"
    override val negativeButton: String = "Close"
    override val useRefIcon: Boolean = true

    override val title: String
        get() = "Bitmap Cache Info"
    override val reficon: BitmapRef = BitmapRef("ic_log_view", { vsh.loadTexture(R.drawable.ic_close) }, BitmapRef.FallbackColor.Transparent)

    private var lines = arrayListOf<Pair<String, String>>()
    private var tPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply{
        color = Color.WHITE
        typeface = FontCollections.masterFont
        textSize = 20.0f
    }
    private var sPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply{
        color = Color.WHITE
        typeface = FontCollections.masterFont
        textSize = 15.0f
    }

    override fun onStart() {


    }

    private val cPt = PointF()

    data class BmpRef(val id : String, val bmp : Bitmap, val ref : Int)
    private val clipRct = RectF()
    private var dbgNumAt = 0
    private val rndrRct = RectF()

    private fun drawDebugLine(
        ctx: Canvas,
        drawBound: RectF,
        cy: Ref<Float>
    ){
        try {
            val bmps = arrayListOf<BmpRef>()
            val bmpCls = BitmapManager.instance.cache
            for(cls in bmpCls){
                bmps.add(BmpRef(cls.id, cls.bitmap!!, cls.refCount))
            }

            clipRct.set(
                drawBound.left,
                cy.p,
                drawBound.right,
                drawBound.bottom
            )
            rndrRct.bottom = 0.0f

            dbgNumAt = dbgNumAt.coerceIn(0, bmps.size)

            ctx.withClip(clipRct){
                for(ri in bmps.indices){
                    val bmp = bmps[ri]
                    val i = ri - dbgNumAt

                    if(i < 0) continue

                    if(rndrRct.bottom > drawBound.bottom) break

                    rndrRct.set(view.scaling.target.left + 30, cy.p + 10, view.scaling.target.left + 130, cy.p + 100)

                    ctx.withClip(rndrRct){
                        ctx.drawARGB(64, 0, 0,0)
                    }

                    ctx.drawBitmap(bmp.bmp, null, rndrRct, null, FittingMode.FIT)


                    tPaint.textAlign = Paint.Align.LEFT
                    ctx.drawText("[${ri}] ${bmp.id}", rndrRct.right + 10.0f, rndrRct.top, tPaint, 1.0f)
                    ctx.drawText("References : ${bmp.ref}", rndrRct.right + 10.0f, rndrRct.top + 30.0f, sPaint, 1.0f)
                    cy.p += 105
                }
            }
        }catch(e:Exception){
            doNothing()
        }
    }


    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime: Float) {
        try {
            lines.clear()
            lines.add(
                "Bitmap Count" to "${BitmapManager.instance.bitmapCount}"
            )
            lines.add(
                "Cache Memory Usage" to BitmapManager.instance.totalCacheSize.asBytes()
            )
            lines.add(
                "Bitmap Load Queue" to "${BitmapManager.instance.queueCount}"
            )

            val h = (lines.size * tPaint.textSize)
            val y = Ref(drawBound.top)
            lines.forEach {
                cPt.set(drawBound.centerX(), y.p)
                tPaint.textAlign = Paint.Align.RIGHT
                ctx.drawText(it.first, cPt.x - 20.0f, cPt.y, tPaint, 1.0f)
                tPaint.textAlign = Paint.Align.LEFT
                ctx.drawText(it.second, cPt.x + 20.0f, cPt.y, tPaint, 1.0f)
                y.p += tPaint.textSize
            }
            if(BuildConfig.DEBUG){
                drawDebugLine(ctx, drawBound, y)
            }
        }catch(cme:ConcurrentModificationException){
            return
        }
    }
    
    internal fun doNothing(){}

    override fun onTouch(a: PointF, b: PointF, act: Int) {
        if(act == MotionEvent.ACTION_MOVE){
            val df = b.y - a.y
            val slp = view.scaling.fitScale * 20.0f
            if(df > slp){
                dbgNumAt--
                a.set(b)
            }else if(df < -slp){
                dbgNumAt++
                a.set(b)
            }
        }
        super.onTouch(a, b, act)
    }

    override fun onDialogButton(isPositive: Boolean) {
        if(!isPositive){
            finish(view.screens.mainMenu)
        }else{
            BitmapManager.instance.cleanup()
        }
    }

    override fun onClose() {
        reficon.release()
    }
}