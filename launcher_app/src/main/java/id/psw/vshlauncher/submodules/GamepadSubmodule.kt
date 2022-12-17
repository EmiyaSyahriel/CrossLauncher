package id.psw.vshlauncher.submodules

import android.os.Build
import android.view.KeyEvent.*
import android.view.MotionEvent
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.VshBaseDirs
import id.psw.vshlauncher.getAllPathsFor
import id.psw.vshlauncher.select
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

class GamepadSubmodule(ctx: VSH) {
    enum class Key (val base:UByte){
        None (0b0u),
        PS              (0b00000001u),
        Start           (0b00000010u),
        Select          (0b00000100u),
        Triangle        (0b00010001u),
        Square          (0b00010010u),
        Circle          (0b00010100u),
        Cross           (0b00011000u),
        PadU            (0b00100001u),
        PadD            (0b00100010u),
        PadL            (0b00100100u),
        PadR            (0b00101000u),
        L1              (0b01000001u),
        L2              (0b01000010u),
        L3              (0b01000100u),
        R1              (0b10000001u),
        R2              (0b10000010u),
        R3              (0b10000100u),
        /** Non-swappable Confirm Button */
        StaticConfirm   (0b01001000u),
        /** Non-swappable Cancel Button */
        StaticCancel    (0b10001000u),
        ;

        companion object {
            /** Let the Asian and Western Console users to have their preference in here. they have their preference and culture
             *
             * And also... Who maps Triangle as Back button? isn't that adds more complexity than just swapping Circle and Cross? */
            var spotMarkedByX = false
            /** Standard Confirm Button */
            val Confirm get() = spotMarkedByX.select(Cross, Circle)
            /** Standard Cancel Button */
            val Cancel get() = spotMarkedByX.select(Circle, Cross)

            fun isCancel(k:Key) = k == Cancel || k == StaticCancel
            fun isConfirm(k:Key) = k == Confirm || k == StaticConfirm
        }
    }

    data class AxisInfo(
        val negative:Key,
        val positive:Key
    )

    data class KeyRemap (
        val hidId : Int,
        val map :  MutableMap<Int, GamepadSubmodule.Key>,
        val shouldUse : (() -> Boolean)? = null)


    companion object {
        private val defaultPre12DualSenseReMap = mutableMapOf(
            KEYCODE_BUTTON_MODE to Key.PS,
            KEYCODE_BUTTON_L2 to Key.Select,
            KEYCODE_BUTTON_R2 to Key.Start,
            KEYCODE_BUTTON_A to Key.Square,
            KEYCODE_BUTTON_B to Key.Cross,
            KEYCODE_BUTTON_C to Key.Circle,
            KEYCODE_BUTTON_X to Key.Triangle,
            KEYCODE_BUTTON_START to Key.R3,
            KEYCODE_BUTTON_SELECT to Key.L3,
            KEYCODE_BUTTON_Y to Key.L1,
            KEYCODE_BUTTON_L1 to Key.L2,
            KEYCODE_BUTTON_Z to Key.R1,
            KEYCODE_BUTTON_R1 to Key.R2,
        )
        private val defaultAndroidGamepadAxisMap = mutableListOf(
            MotionEvent.AXIS_HAT_X to AxisInfo(Key.PadL, Key.PadR),
            MotionEvent.AXIS_HAT_Y to AxisInfo(Key.PadU, Key.PadD),
        )

        private val defaultAndroidGamepadMap = mutableMapOf(
            KEYCODE_BUTTON_A to Key.Cross,
            KEYCODE_BUTTON_B to Key.Circle,
            KEYCODE_BUTTON_X to Key.Square,
            KEYCODE_BUTTON_Y to Key.Triangle,
            KEYCODE_BUTTON_SELECT to Key.Select,
            KEYCODE_BUTTON_START to Key.Start,
            KEYCODE_BUTTON_MODE to Key.PS,
            KEYCODE_BUTTON_L1 to Key.L1,
            KEYCODE_BUTTON_L2 to Key.L2,
            KEYCODE_BUTTON_THUMBL to Key.L3,
            KEYCODE_BUTTON_R1 to Key.R1,
            KEYCODE_BUTTON_R2 to Key.R2,
            KEYCODE_BUTTON_THUMBR to Key.R3,
        )

        private val defaultAndroidKeyboardMap = mutableMapOf(
            KEYCODE_ESCAPE to Key.StaticCancel,
            KEYCODE_DEL to Key.StaticCancel,
            KEYCODE_ENTER to Key.StaticConfirm,
            KEYCODE_SPACE to Key.StaticConfirm,
            KEYCODE_DPAD_CENTER to Key.StaticConfirm,
            KEYCODE_GRAVE to Key.Square,
            KEYCODE_MENU to Key.Triangle,
            KEYCODE_TAB to Key.Triangle,
            KEYCODE_PAGE_UP to Key.Select,
            KEYCODE_PAGE_DOWN to Key.Start,
            KEYCODE_BREAK to Key.PS,
            KEYCODE_F1 to Key.L1,
            KEYCODE_F2 to Key.L2,
            KEYCODE_F3 to Key.L3,
            KEYCODE_F7 to Key.R1,
            KEYCODE_F8 to Key.R2,
            KEYCODE_F9 to Key.R3,
            KEYCODE_DPAD_UP to Key.PadU,
            KEYCODE_DPAD_DOWN to Key.PadD,
            KEYCODE_DPAD_LEFT to Key.PadL,
            KEYCODE_DPAD_RIGHT to Key.PadR,
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

    init {
        ctx.getAllPathsFor(VshBaseDirs.VSH_RESOURCES_DIR, "keymap").forEach {
            if(it.exists()){
                if(it.isDirectory){
                    it.list { _, name -> name.endsWith(".txt", ignoreCase = true)}?.forEach {
                    }
                }
            }
        }
    }

    fun translate(key: Int, devId:Int) : Key{
        val remap = keyRemaps.firstOrNull {
            (it.hidId == devId && (it.shouldUse?.invoke()) ?: true) || it.hidId == ANDROID_GAMEPAD
        }
        if(remap != null){
            if(remap.map.containsKey(key)) {
                return remap.map[key]!!
            }else{
                val kbdRemap = defaultAndroidKeyboardMap
                if(kbdRemap.containsKey(key)) return kbdRemap[key] ?: Key.None
            }
        }
        return Key.None
    }

    fun translate(key:Int, vid:Int = 0, pid:Int = 0) : Key {
        return translate(key, makePID(vid, pid))
    }

    fun translateAxis(axis:Int, value:Float, devId:Int) : Key {
        if(abs(value) > axisThreshold){
            val remap = axisRemaps[devId] ?: axisRemaps[ANDROID_GAMEPAD]
            val axe = remap?.find { it.first == axis }
            if(axe != null){
                return (value > 0.0).select(axe.second.positive, axe.second.negative)
            }
        }
        return Key.None
    }

    fun translateAxis(axis:Int, value:Float, vid:Int = 0, pid:Int = 0) : Key{
        return translateAxis(axis, value, makePID(vid, pid))
    }
}