package id.psw.vshlauncher.submodules

import id.psw.vshlauncher.select

class GamepadUISubmodule {
    var activeGamepad : PadType = PadType.PlayStation

    private val gamepadIcons = mapOf(
        PadType.PlayStation to mapOf(
            PadKey.Circle to   '\uF880',
            PadKey.Cross to    '\uF881',
            PadKey.Square to   '\uF882',
            PadKey.Triangle to '\uF883',
            PadKey.PadU to     '\uF884',
            PadKey.PadD to     '\uF885',
            PadKey.PadL to     '\uF886',
            PadKey.PadR to     '\uF887',
            PadKey.L1 to       '\uF888',
            PadKey.L2 to       '\uF889',
            PadKey.L3 to       '\uF88A',
            PadKey.R1 to       '\uF88B',
            PadKey.R2 to       '\uF88C',
            PadKey.R3 to       '\uF88D',
            PadKey.Select to   '\uF88E',
            PadKey.Start to    '\uF88F',
            PadKey.PS to       '\uF892',
        ),
        PadType.Xbox to mapOf(
            PadKey.Circle to   '\uE012',
            PadKey.Cross to    '\uE013',
            PadKey.Square to   '\uE014',
            PadKey.Triangle to '\uE015',
            PadKey.PadU to     '\uE016',
            PadKey.PadD to     '\uE017',
            PadKey.PadL to     '\uE018',
            PadKey.PadR to     '\uE019',
            PadKey.L1 to       '\uE01A',
            PadKey.L2 to       '\uE01B',
            PadKey.L3 to       '\uE01C',
            PadKey.R1 to       '\uE01D',
            PadKey.R2 to       '\uE01E',
            PadKey.R3 to       '\uE01F',
            PadKey.Select to   '\uE020',
            PadKey.Start to    '\uE021',
            PadKey.PS to       '\uE022',
        ),
        PadType.Nintendo to mapOf(
            PadKey.Circle to   '\uE023',
            PadKey.Cross to    '\uE024',
            PadKey.Square to   '\uE025',
            PadKey.Triangle to '\uE026',
            PadKey.PadU to     '\uE027',
            PadKey.PadD to     '\uE028',
            PadKey.PadL to     '\uE029',
            PadKey.PadR to     '\uE02A',
            PadKey.L1 to       '\uE02B',
            PadKey.L2 to       '\uE02C',
            PadKey.L3 to       '\uE02D',
            PadKey.R1 to       '\uE02E',
            PadKey.R2 to       '\uE02F',
            PadKey.R3 to       '\uE030',
            PadKey.Select to   '\uE031',
            PadKey.Start to    '\uE032',
            PadKey.PS to       '\uE033',
        ),
        PadType.Android to mapOf(
            PadKey.Circle to   '\uE034',
            PadKey.Cross to    '\uE035',
            PadKey.Square to   '\uE036',
            PadKey.Triangle to '\uE037',
            PadKey.PadU to     '\uE038',
            PadKey.PadD to     '\uE039',
            PadKey.PadL to     '\uE03A',
            PadKey.PadR to     '\uE03B',
            PadKey.L1 to       '\uE03C',
            PadKey.L2 to       '\uE03D',
            PadKey.L3 to       '\uE03E',
            PadKey.R1 to       '\uE03F',
            PadKey.R2 to       '\uE040',
            PadKey.R3 to       '\uE041',
            PadKey.Select to   '\uE042',
            PadKey.Start to    '\uE043',
            PadKey.PS to       '\uE044',
        ),
    )

    fun getGamepadChar(k:PadKey, pad:PadType = PadType.Unknown): Char {
        val selPad = (pad == PadType.Unknown).select(activeGamepad, pad)
        return gamepadIcons[selPad]?.get(k) ?: '\u0000'
    }
}