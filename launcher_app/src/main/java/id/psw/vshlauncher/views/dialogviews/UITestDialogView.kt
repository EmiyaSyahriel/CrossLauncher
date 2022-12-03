package id.psw.vshlauncher.views.dialogviews

import android.graphics.*
import android.os.Build
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.postNotification
import id.psw.vshlauncher.submodules.GamepadSubmodule
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.typography.drawText
import id.psw.vshlauncher.typography.toButtonSpan
import id.psw.vshlauncher.views.VshViewPage
import id.psw.vshlauncher.views.XmbDialogSubview
import kotlin.random.Random

class UITestDialogView(private val vsh: VSH) : XmbDialogSubview(vsh) {
    override val title: String = "::TEST::"

    override val hasNegativeButton: Boolean = true

    override val negativeButton: String = "Back"

    override fun onDialogButton(isPositive: Boolean) {
        finish(VshViewPage.MainMenu)
    }
    private var testBarValue = 50.0f
    private var testVScrollPc = 0.5f
    private var testVScrollSz = 0.2f
    private var testHScrollPc = 0.5f
    private var testHScrollSz = 0.2f
    private val rctF = RectF()
    private val btnDspLines = arrayOf(
        "{confirm} Active Button Type : ${vsh._gamepadUi.activeGamepad} {confirm}".toButtonSpan(vsh),
        "{confirm} Confirm | {cancel} Cancel".toButtonSpan(vsh),
        "{triangle}{circle}{cross}{square}{triangle}{circle}{cross}{square}{triangle}{circle}{cross}{square}".toButtonSpan(vsh),
        "{triangle} Triangle | {square} Square | {circle} Circle | {cross} Cross".toButtonSpan(vsh),
        "{up} D-Pad Up | {down} D-Pad Down | {left} D-Pad Left | {right} D-Pad Right".toButtonSpan(vsh),
        "{ps} System Button | {start} Start | {select} Select".toButtonSpan(vsh),
        "{l1} L1 | {l2} L2 | {r1} R1 | {r2} R2".toButtonSpan(vsh),
        "{l3} L3 | {r3} L3".toButtonSpan(vsh),
        "{l3}+{r3}+{r2} Enable PSWPatch | {l2}+{r2}+{square} Enable reActPSW".toButtonSpan(vsh),
        "{confirm}{cancel}{confirm}{cancel}{confirm}{cancel}{confirm}{cancel}{confirm}{cancel}".toButtonSpan(vsh),
        "{up}{down}{left}{right}{up}{down}{left}{right}{up}{down}{left}{right}{up}{down}{left}{right}".toButtonSpan(vsh)
    )

    private var pageNum = 0
    private var tPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply{
        typeface = FontCollections.masterFont
        textSize = 20.0f
        textAlign = Paint.Align.CENTER
    }
    private var pageNames = arrayOf("Progress Bar","Scroll Bar", "Button Display","Glow Edge")
    private var tmpRectF = RectF()
    private var currentTime = 0.0f

    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime:Float) {
        currentTime += deltaTime
        if(currentTime > 3600.0f) currentTime = 0.0f
        val xCenterL = drawBound.centerX() - 100f
        val xCenterR = drawBound.centerX() + 100f
        val maxTabW = pageNames.size * 200.0f
        val tabL = drawBound.centerX() - (maxTabW / 2.0f)
        pageNames.forEachIndexed { i, s ->
            tmpRectF.set((i * 200.0f) + tabL, drawBound.top, ((i + 1) * 200.0f) + tabL, drawBound.top + 40.0f)
            if(i == pageNum){
                tPaint.color = Color.argb(64,255,255,255)
                ctx.drawRect(tmpRectF, tPaint)
            }
            tPaint.color = Color.argb(255,255,255,255)
            ctx.drawText(s, tmpRectF.centerX(), tmpRectF.centerY(), tPaint)
        }

        when(pageNum){
            0 -> {
                SubDialogUI.progressBar(ctx,0.0f, 100.0f, testBarValue, xCenterL, 100.0f, 300.0f)
            }
            1 -> {
                rctF.set(drawBound.right - 20.0f, drawBound.top, drawBound.right, drawBound.bottom)
                SubDialogUI.scrollBar(ctx,
                    rctF,
                    testVScrollPc, testVScrollSz)
                rctF.set(drawBound.left, drawBound.bottom - 20.0f, drawBound.right, drawBound.bottom)
                SubDialogUI.scrollBar(ctx,
                    rctF,
                    testHScrollPc, testHScrollSz)
            }
            2 -> {
                val lineSz = (tPaint.textSize * 1.25f)
                val centerY = drawBound.centerY() - (btnDspLines.size * lineSz)
                val centerX = drawBound.centerX()
                val lastAlign = tPaint.textAlign
                tPaint.textAlign = Paint.Align.CENTER
                btnDspLines.forEachIndexed { i, s ->
                    ctx.drawText(s, centerX, centerY + (i * lineSz), 1f, tPaint)
                }
                tPaint.textAlign = lastAlign
            }
            3 -> {
                rctF.set(drawBound.left + 30.0f, drawBound.centerY() - 15.0f,drawBound.right - 30.0f,drawBound.centerY() + 15.0f)
                SubDialogUI.glowOverlay(ctx, rctF, 10, null, true, currentTime)
            }
        }
    }
    private val cfwEEBtnCombo = mutableMapOf(
        GamepadSubmodule.Key.R3 to false,
        GamepadSubmodule.Key.L3 to false,
        GamepadSubmodule.Key.R2 to false,
        GamepadSubmodule.Key.L2 to false,
        GamepadSubmodule.Key.Square to false,
    )
    private fun isDown(key:GamepadSubmodule.Key) : Boolean = cfwEEBtnCombo[key] == true

    @Suppress("SpellCheckingInspection")
    private fun cfwEasterEgg(key: GamepadSubmodule.Key, isPress: Boolean) {
        if(cfwEEBtnCombo.containsKey(key)){
            cfwEEBtnCombo[key] = isPress

            if( isDown(GamepadSubmodule.Key.Square) &&
                isDown(GamepadSubmodule.Key.R2) &&
                isDown(GamepadSubmodule.Key.L2)){
                    val rapCount = vsh.categories.find { it.id == VSH.ITEM_CATEGORY_APPS }?.contentCount ?: (Random.nextInt() % 128)
                    val edatCount = vsh.categories.find { it.id == VSH.ITEM_CATEGORY_GAME }?.contentCount ?: (Random.nextInt() % 72)
                vsh.postNotification(null, "reActPSW v1.35.23","CFW : JKSEMBUG ${Build.VERSION.RELEASE}.1, WELING 4.84, " +
                        "activated : $rapCount RAPs, $edatCount EDATs", 10.0f)
            }

            if( isDown(GamepadSubmodule.Key.L3) &&
                isDown(GamepadSubmodule.Key.R3) &&
                isDown(GamepadSubmodule.Key.R2)){
                vsh.postNotification(null, "PSWPatch 2021.01/CX","IDPS & PSID Spoofed! Now you'll be safe (even without spoofing) to access Google Play Store from your Network", 10.0f)
            }
        }
    }

    override fun onGamepad(key: GamepadSubmodule.Key, isPress: Boolean): Boolean {
        var retval = false
        if(pageNum == 2){
            cfwEasterEgg(key, isPress)
        }

        if(isPress){
            when(key){
                GamepadSubmodule.Key.L1 -> {
                    pageNum = (pageNum - 1).coerceIn(0, 3)
                    retval = true
                }
                GamepadSubmodule.Key.R1 -> {
                    pageNum = (pageNum + 1).coerceIn(0, 3)
                    retval = true
                }
                GamepadSubmodule.Key.PadR -> {
                    retval = when(pageNum){
                        0 -> { testBarValue = (testBarValue + 1.0f).coerceIn(0.0f, 100.0f); true }
                        1 -> { testHScrollPc = (testHScrollPc + 0.1f).coerceIn(0.0f, 1.0f); true }
                        else -> false
                    }
                }
                GamepadSubmodule.Key.PadL -> {
                    retval = when(pageNum){
                        0 -> { testBarValue = (testBarValue - 1.0f).coerceIn(0.0f, 100.0f); true }
                        1 -> { testHScrollPc = (testHScrollPc - 0.1f).coerceIn(0.0f, 1.0f); true }
                        else -> false
                    }
                }
                GamepadSubmodule.Key.PadU -> {
                    retval = when(pageNum){
                        1 -> { testVScrollPc = (testVScrollPc - 0.1f).coerceIn(0.0f, 1.0f); true }
                        else -> false
                    }
                }
                GamepadSubmodule.Key.PadD -> {
                    retval = when(pageNum){
                        1 -> { testVScrollPc = (testVScrollPc + 0.1f).coerceIn(0.0f, 1.0f); true }
                        else -> false
                    }
                }
                else -> {}
            }
        }
        return retval
    }

}