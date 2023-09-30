package id.psw.vshlauncher.submodules

import android.os.Build
import android.view.KeyEvent.*
import android.view.MotionEvent
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.VshBaseDirs
import id.psw.vshlauncher.select
import id.psw.vshlauncher.types.FileQuery
import java.lang.Math.abs

/**
 * ## Keymapping Format
 * Format : {android_key_code} = {crosslauncher_ps_keycode}
 * e.g : 108 = X, 66 = Confirm
 *
 * ### Keymap Path Location:
 * `${DATA_ROOT}/dev_flash/vsh/resource/keymap/{device_id}.txt`
 * `device_id` matches Device VID and PID in `{VID:xxxx}{PID:xxxx}`
 * - e.g : `054C0CE6.txt` = 054C = Sony, 0CE6 = Wireless Controller (DualSense)
 * #### Reserved Device IDs:
 * - `00000000` - Fallback Keyboard
 * - `054C0CE6`
 *
 * ### android_key_code
 * see [KeyEvent](https://developer.android.com/reference/android/view/KeyEvent),
 * the `KEYCODE_` key constants, with Axis access prefixed with `AXIS_` with `+/-` prefix
 * e.g : `AXIS_HAT_X+` = Axis Hat X (D-Pad) Positive Value, `AXIS_X-` = Axis X (Negative Value)
 *
 * ### crosslauncher_ps_keycode:
 * Implementation is case-insensitive
 * - `PS, Guide` -> PlayStation Button
 * - `Sta, Start, Menu` -> Start
 * - `Sel, Select, Back` -> Select
 * - `Tri, Triangle, Y` -> Triangle
 * - `Sqr, Square, X` -> Square
 * - `Cir, Circle, !O, B` -> Circle
 * - `Crs, Cross, !X, A` -> Cross
 * - `Cnf, Confirm` -> Static Confirm (Non-swappable Confirm, e.g Enter Button)
 * - `Cnc, Cancel` -> Static Cancel (Non-swappable Cancel, e.g Escape Button)
 * - `PUp, Up, PadU, PadUp` -> D-Pad Up
 * - `PLf, Left, PadL, PadLeft` -> D-Pad Left
 * - `PRg, Right, PadR, PadRight` -> D-Pad Right
 * - `PDn, Down, PadD, PadDown` -> D-Pad Down
 * - `L1, LB, LBump` -> L1 / Left Bumper /  Upper Left Shoulder Button
 * - `L2, LT, LTrigger` -> L2 / Left Trigger /  Lower Left Shoulder Button or Trigger
 * - `L3, LAnalog, LS, LStick` -> L3 / Left Analog Click
 * - `R1, RB, RBump` -> R1 / Right Bumper / Upper Right Shoulder Button
 * - `R2, RT, RTrigger` -> R2 / Right Trigger / Lower Right Should Button or Trigger
 * - `R3, RAnalog, RS, RStick` -> R3 / Right Analog Click
 * */

class GamepadSubmodule(private val ctx: Vsh) : IVshSubmodule {
    data class AxisInfo(
        val negative:PadKey,
        val positive:PadKey
    )

    data class KeyRemap (
        val hidId : Int,
        val map :  MutableMap<Int, PadKey>,
        val shouldUse : (() -> Boolean)? = null)

    override fun onCreate() {
        // Do nothing

        FileQuery(VshBaseDirs.VSH_RESOURCES_DIR).atPath("keymap")
            .onlyIncludeExists(true)
            .execute(ctx)
            .forEach {
                if(it.isDirectory){
                    it.list { _, name -> name.endsWith(".txt", ignoreCase = true)}?.forEach {

                    }
                }
            }
    }

    override fun onDestroy() {
        // Do nothing
    }

    companion object {
        private val defaultPre12DualSenseReMap = mutableMapOf(
            KEYCODE_BUTTON_MODE to PadKey.PS,
            KEYCODE_BUTTON_L2 to PadKey.Select,
            KEYCODE_BUTTON_R2 to PadKey.Start,
            KEYCODE_BUTTON_A to PadKey.Square,
            KEYCODE_BUTTON_B to PadKey.Cross,
            KEYCODE_BUTTON_C to PadKey.Circle,
            KEYCODE_BUTTON_X to PadKey.Triangle,
            KEYCODE_BUTTON_START to PadKey.R3,
            KEYCODE_BUTTON_SELECT to PadKey.L3,
            KEYCODE_BUTTON_Y to PadKey.L1,
            KEYCODE_BUTTON_L1 to PadKey.L2,
            KEYCODE_BUTTON_Z to PadKey.R1,
            KEYCODE_BUTTON_R1 to PadKey.R2,
        )
        private val defaultAndroidGamepadAxisMap = mutableListOf(
            MotionEvent.AXIS_HAT_X to AxisInfo(PadKey.PadL, PadKey.PadR),
            MotionEvent.AXIS_HAT_Y to AxisInfo(PadKey.PadU, PadKey.PadD),
        )

        private val defaultAndroidGamepadMap = mutableMapOf(
            KEYCODE_BUTTON_A to PadKey.Cross,
            KEYCODE_BUTTON_B to PadKey.Circle,
            KEYCODE_BUTTON_X to PadKey.Square,
            KEYCODE_BUTTON_Y to PadKey.Triangle,
            KEYCODE_BUTTON_SELECT to PadKey.Select,
            KEYCODE_BUTTON_START to PadKey.Start,
            KEYCODE_BUTTON_MODE to PadKey.PS,
            KEYCODE_BUTTON_L1 to PadKey.L1,
            KEYCODE_BUTTON_L2 to PadKey.L2,
            KEYCODE_BUTTON_THUMBL to PadKey.L3,
            KEYCODE_BUTTON_R1 to PadKey.R1,
            KEYCODE_BUTTON_R2 to PadKey.R2,
            KEYCODE_BUTTON_THUMBR to PadKey.R3,
        )

        private val defaultAndroidKeyboardMap = mutableMapOf(
            KEYCODE_ESCAPE to PadKey.StaticCancel,
            KEYCODE_DEL to PadKey.StaticCancel,
            KEYCODE_ENTER to PadKey.StaticConfirm,
            KEYCODE_SPACE to PadKey.StaticConfirm,
            KEYCODE_DPAD_CENTER to PadKey.StaticConfirm,
            KEYCODE_GRAVE to PadKey.Square,
            KEYCODE_MENU to PadKey.Triangle,
            KEYCODE_TAB to PadKey.Triangle,
            KEYCODE_PAGE_UP to PadKey.Select,
            KEYCODE_PAGE_DOWN to PadKey.Start,
            KEYCODE_BREAK to PadKey.PS,
            KEYCODE_F1 to PadKey.L1,
            KEYCODE_F2 to PadKey.L2,
            KEYCODE_F3 to PadKey.L3,
            KEYCODE_F7 to PadKey.R1,
            KEYCODE_F8 to PadKey.R2,
            KEYCODE_F9 to PadKey.R3,
            KEYCODE_DPAD_UP to PadKey.PadU,
            KEYCODE_DPAD_DOWN to PadKey.PadD,
            KEYCODE_DPAD_LEFT to PadKey.PadL,
            KEYCODE_DPAD_RIGHT to PadKey.PadR,
        )

        const val ANDROID_GAMEPAD = 0x00000001
        const val ANDROID_KEYBOARD = 0x00000000
        const val SONY_DUALSHOCK_3_HID_ID = 0x054C0268
        const val SONY_DUALSHOCK_4_V1_HID_ID = 0x054C05C4
        const val SONY_DUALSHOCK_4_V2_HID_ID = 0x054C09CC
        const val SONY_DUALSENSE_HID_ID = 0x054C0CE6
        var axisThreshold = 0.5

        fun makePID( vid:Int = 0, pid:Int = 0) = (vid shl 16) or pid
    }

    private val keyRemaps = listOf(
        KeyRemap(SONY_DUALSENSE_HID_ID, defaultPre12DualSenseReMap) // only for Android < 12
            { Build.VERSION.SDK_INT < Build.VERSION_CODES.R },
        KeyRemap(ANDROID_GAMEPAD, defaultAndroidGamepadMap), // DualSense is fine on Android > 12
        KeyRemap(ANDROID_KEYBOARD, defaultAndroidKeyboardMap)
    )

    private val axisRemaps = mutableMapOf(ANDROID_GAMEPAD to defaultAndroidGamepadAxisMap)

    fun translate(key: Int, devId:Int) : PadKey{
        val remap = keyRemaps.firstOrNull {
            (it.hidId == devId && (it.shouldUse?.invoke()) ?: true) || it.hidId == ANDROID_GAMEPAD
        }
        if(remap != null){
            if(remap.map.containsKey(key)) {
                return remap.map[key]!!
            }else{
                val kbdRemap = defaultAndroidKeyboardMap
                if(kbdRemap.containsKey(key)) return kbdRemap[key] ?: PadKey.None
            }
        }
        return PadKey.None
    }

    fun translate(key:Int, vid:Int = 0, pid:Int = 0) : PadKey {
        return translate(key, makePID(vid, pid))
    }

    fun translateAxis(axis:Int, value:Float, devId:Int) : PadKey {
        if(abs(value) > axisThreshold){
            val remap = axisRemaps[devId] ?: axisRemaps[ANDROID_GAMEPAD]
            val axe = remap?.find { it.first == axis }
            if(axe != null){
                return (value > 0.0).select(axe.second.positive, axe.second.negative)
            }
        }
        return PadKey.None
    }

    fun translateAxis(axis:Int, value:Float, vid:Int = 0, pid:Int = 0) : PadKey {
        return translateAxis(axis, value, makePID(vid, pid))
    }
}