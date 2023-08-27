package id.psw.vshlauncher.views.dialogviews

import android.graphics.*
import android.view.KeyEvent.ACTION_DOWN
import id.psw.vshlauncher.*
import id.psw.vshlauncher.submodules.GamepadSubmodule
import id.psw.vshlauncher.submodules.PadKey
import id.psw.vshlauncher.types.items.XMBItemCategory
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.DrawExtension
import id.psw.vshlauncher.views.VshViewPage
import id.psw.vshlauncher.views.XmbDialogSubview
import kotlin.math.abs

class ArrangeCategoryDialogView(private val vsh: VSH) :  XmbDialogSubview(vsh) {
    private var activeIndex = 0
    private var activeIndexF = 0.0f
    private var liftOffset = 0.0f
    private var liftItemId = ""
    private var doLift = false
    private val iconSize = PointF(80.0f, 80.0f)
    private val tmpIconPf = PointF(0.0f, 0.0f)
    private val tmpRectPf = RectF()
    private lateinit var tPaint : Paint
    override val hasNegativeButton: Boolean = true
    override val hasPositiveButton: Boolean = true
    override val negativeButton: String
        get() = vsh.getString(doLift.select(android.R.string.cancel, R.string.common_back))
    override val positiveButton: String
        get() = vsh.getString(doLift.select(R.string.rearrange_end, R.string.rearrange_start))
    private val drawBound: RectF = RectF()
    private val pathArrowDown = Path()
    private val selBound = RectF()
    private var dTime = 0.0f

    override val title: String
        get() = vsh.getString(R.string.dlg_rearrange_categories)

    override fun onClose() {
        super.onClose()
    }

    override fun onStart() {
        tPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 15.0f
            typeface = FontCollections.masterFont
            color = Color.WHITE
        }
        super.onStart()
    }

    private fun drawIcon(ctx:Canvas, cat: XMBItemCategory, pos:PointF, alpha:Float){
        val hSizeY = iconSize.y / 2.0f
        tPaint.alpha = (alpha * 255).toInt()
        if(cat.hasIcon){
            val hSizeX = iconSize.x / 2.0f
            tmpRectPf.set(
                pos.x - hSizeX, pos.y - hSizeY,
                pos.x + hSizeX, pos.y + hSizeY
            )
            ctx.drawBitmap(cat.icon, null, tmpRectPf, tPaint)
        }
        ctx.drawText(cat.displayName, pos.x, pos.y + hSizeY + 15.0f, tPaint)
    }

    private val visibleItems : List<XMBItemCategory> get() = vsh.categories.sortedBy { (it).sortIndex }

    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime: Float) {
        this.drawBound.set(drawBound)
        dTime += deltaTime
        tPaint.textAlign = Paint.Align.CENTER
        selBound.left = drawBound.centerX() - iconSize.x
        selBound.right = drawBound.centerX() + iconSize.x
        visibleItems.forEachIndexed { i, it ->
            val xi = i - activeIndexF
            val x = drawBound.centerX() + (xi * 2.0f * iconSize.x)
            val y = drawBound.centerY() + (iconSize.y * liftOffset)
            tmpIconPf.set(x, y)
            var alpha = 1.0f - (abs(x - drawBound.centerX()) / (drawBound.width() / 2.0f))
            alpha = (alpha * 2.0f).coerceIn(0.0f, 1.0f)

            if(it.isHidden)
                alpha * 0.5f

            selBound.top = y - (iconSize.y * 0.75f)
            selBound.bottom = y + iconSize.y

            drawIcon(ctx, it, tmpIconPf, alpha)
        }

        activeIndexF = 0.5f.toLerp(activeIndexF, activeIndex * 1.0f)
        liftOffset = 0.3f.toLerp(liftOffset, doLift.select(1.0f, 0.0f))

        if(doLift){
            tPaint.alpha = (liftOffset * 255).toInt()
            ctx.drawPath(pathArrowDown, tPaint)
        }

        if(liftOffset > 0.1f){
            val x = drawBound.centerX()
            val y = drawBound.centerY() - (iconSize.y * liftOffset)

            selBound.top = y - (iconSize.y * 0.75f)

            val item = vsh.categories.firstOrNull { it.id == liftItemId }
            tmpIconPf.set(x, y)
            if(item != null){
                drawIcon(ctx, item, tmpIconPf, liftOffset)
            }
        }

        DrawExtension.glowOverlay(ctx, selBound, 20, null, true, dTime)
    }

    private fun setActiveIndex(num: Int){
        activeIndex = (num).coerceIn(0, visibleItems.size - 1)
    }

    override fun onTouch(a: PointF, b: PointF, act: Int) {
        if(act == ACTION_DOWN){
            val w = drawBound.width() / 3.0f
            if(a.x < (w * 1.0f)){
                setActiveIndex(activeIndex - 1)
            }else if(a.x < (w * 2.0f))
            {
                if(doLift) endLifting(false) else startLifting()
            }
            else if(a.x < w * 3.0f){
                setActiveIndex(activeIndex + 1)
            }
        }
        super.onTouch(a, b, act)
    }

    private fun startLifting() {
        liftItemId = visibleItems[activeIndex].id
        doLift = true

        val arrowSize = 10.0f
        pathArrowDown.reset()
        pathArrowDown.moveTo(drawBound.centerX() - arrowSize, drawBound.centerY() - arrowSize)
        pathArrowDown.lineTo(drawBound.centerX() + arrowSize, drawBound.centerY() - arrowSize)
        pathArrowDown.lineTo(drawBound.centerX() + 0.0f, drawBound.centerY() + arrowSize)
        pathArrowDown.close()
    }

    private fun endLifting(cancel:Boolean){
        doLift = false
        pathArrowDown.reset()

        if(!cancel){ // apply
            val aItem = vsh.categories.firstOrNull { it.id == liftItemId }
            val bItem = visibleItems[activeIndex] as XMBItemCategory?

            if(aItem != null && bItem != null){
                val iA = aItem.sortIndex
                val iB = bItem.sortIndex
                aItem.sortIndex = iB
                bItem.sortIndex = iA
                vsh.categories.sortBy { it.sortIndex }
            }

            liftItemId = ""
        }
    }

    override fun onGamepad(key: PadKey, isPress: Boolean): Boolean {
        when(key){
            PadKey.PadL -> {
                setActiveIndex(activeIndex - 1)
            }
            PadKey.PadR -> {
                setActiveIndex(activeIndex + 1)
            }
            else -> { } // Nothing to do
        }
        return super.onGamepad(key, isPress)
    }

    override fun onDialogButton(isPositive: Boolean) {
        if(doLift){
            endLifting(!isPositive)
        }else{
            if(isPositive) startLifting()
            else finish(VshViewPage.Dialog)
        }
    }
}