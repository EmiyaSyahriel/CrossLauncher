package id.psw.vshlauncher.submodules

import id.psw.vshlauncher.select

class GamepadUISubmodule {
    enum class PadType {
        Default,
        PlayStation,
        Xbox,
        Nintendo,
        Android
    }

    var activeGamepad : PadType = PadType.PlayStation

    val gamepadIcons = mapOf(
        PadType.PlayStation to mapOf(
            GamepadSubmodule.Key.Circle to   '\uF880',
            GamepadSubmodule.Key.Cross to    '\uF881',
            GamepadSubmodule.Key.Square to   '\uF882',
            GamepadSubmodule.Key.Triangle to '\uF883',
            GamepadSubmodule.Key.PadU to     '\uF884',
            GamepadSubmodule.Key.PadD to     '\uF885',
            GamepadSubmodule.Key.PadL to     '\uF886',
            GamepadSubmodule.Key.PadR to     '\uF887',
            GamepadSubmodule.Key.L1 to       '\uF888',
            GamepadSubmodule.Key.L2 to       '\uF889',
            GamepadSubmodule.Key.L3 to       '\uF88A',
            GamepadSubmodule.Key.R1 to       '\uF88B',
            GamepadSubmodule.Key.R2 to       '\uF88C',
            GamepadSubmodule.Key.R3 to       '\uF88D',
            GamepadSubmodule.Key.Select to   '\uF88E',
            GamepadSubmodule.Key.Start to    '\uF88F',
            GamepadSubmodule.Key.PS to       '\uF892',
        ),
        PadType.Xbox to mapOf(
            GamepadSubmodule.Key.Circle to   '\uE012',
            GamepadSubmodule.Key.Cross to    '\uE013',
            GamepadSubmodule.Key.Square to   '\uE014',
            GamepadSubmodule.Key.Triangle to '\uE015',
            GamepadSubmodule.Key.PadU to     '\uE016',
            GamepadSubmodule.Key.PadD to     '\uE017',
            GamepadSubmodule.Key.PadL to     '\uE018',
            GamepadSubmodule.Key.PadR to     '\uE019',
            GamepadSubmodule.Key.L1 to       '\uE01A',
            GamepadSubmodule.Key.L2 to       '\uE01B',
            GamepadSubmodule.Key.L3 to       '\uE01C',
            GamepadSubmodule.Key.R1 to       '\uE01D',
            GamepadSubmodule.Key.R2 to       '\uE01E',
            GamepadSubmodule.Key.R3 to       '\uE01F',
            GamepadSubmodule.Key.Select to   '\uE020',
            GamepadSubmodule.Key.Start to    '\uE021',
            GamepadSubmodule.Key.PS to       '\uE022',
        ),
        PadType.Nintendo to mapOf(
            GamepadSubmodule.Key.Circle to   '\uE023',
            GamepadSubmodule.Key.Cross to    '\uE024',
            GamepadSubmodule.Key.Square to   '\uE025',
            GamepadSubmodule.Key.Triangle to '\uE026',
            GamepadSubmodule.Key.PadU to     '\uE027',
            GamepadSubmodule.Key.PadD to     '\uE028',
            GamepadSubmodule.Key.PadL to     '\uE029',
            GamepadSubmodule.Key.PadR to     '\uE02A',
            GamepadSubmodule.Key.L1 to       '\uE02B',
            GamepadSubmodule.Key.L2 to       '\uE02C',
            GamepadSubmodule.Key.L3 to       '\uE02D',
            GamepadSubmodule.Key.R1 to       '\uE02E',
            GamepadSubmodule.Key.R2 to       '\uE02F',
            GamepadSubmodule.Key.R3 to       '\uE030',
            GamepadSubmodule.Key.Select to   '\uE031',
            GamepadSubmodule.Key.Start to    '\uE032',
            GamepadSubmodule.Key.PS to       '\uE033',
        ),
        PadType.Android to mapOf(
            GamepadSubmodule.Key.Circle to   '\uE034',
            GamepadSubmodule.Key.Cross to    '\uE035',
            GamepadSubmodule.Key.Square to   '\uE036',
            GamepadSubmodule.Key.Triangle to '\uE037',
            GamepadSubmodule.Key.PadU to     '\uE038',
            GamepadSubmodule.Key.PadD to     '\uE039',
            GamepadSubmodule.Key.PadL to     '\uE03A',
            GamepadSubmodule.Key.PadR to     '\uE03B',
            GamepadSubmodule.Key.L1 to       '\uE03C',
            GamepadSubmodule.Key.L2 to       '\uE03D',
            GamepadSubmodule.Key.L3 to       '\uE03E',
            GamepadSubmodule.Key.R1 to       '\uE03F',
            GamepadSubmodule.Key.R2 to       '\uE040',
            GamepadSubmodule.Key.R3 to       '\uE041',
            GamepadSubmodule.Key.Select to   '\uE042',
            GamepadSubmodule.Key.Start to    '\uE043',
            GamepadSubmodule.Key.PS to       '\uE044',
        ),
    )

    fun getGamepadChar(k:GamepadSubmodule.Key, pad:PadType = PadType.Default): Char {
        val selPad = (pad == PadType.Default).select(activeGamepad, pad)
        return gamepadIcons[selPad]?.get(k) ?: '\u0000'
    }
}