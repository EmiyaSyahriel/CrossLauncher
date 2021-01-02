package id.psw.vshlauncher.typography


enum class ButtonType {
    PlayStation,
    Xbox,
    NSwitch,
    AndroidTV,
    AndroidTouch,
    PC
}

data class FontMacroKey ( val macro:String, val btnType:ButtonType )

val confirmFontMacros = mapOf(
    Pair(FontMacroKey("{btn:confirm}", ButtonType.PlayStation), "\uf880"),
    Pair(FontMacroKey("{btn:cancel}",  ButtonType.PlayStation), "\uf881"),
    Pair(FontMacroKey("{btn:confirm}", ButtonType.Xbox), "\ue012"),
    Pair(FontMacroKey("{btn:cancel}",  ButtonType.Xbox), "\ue013"),
    Pair(FontMacroKey("{btn:confirm}", ButtonType.NSwitch), "\ue023"),
    Pair(FontMacroKey("{btn:cancel}",  ButtonType.NSwitch), "\ue024"),
    Pair(FontMacroKey("{btn:confirm}", ButtonType.AndroidTV), "\ue035"),
    Pair(FontMacroKey("{btn:cancel}",  ButtonType.AndroidTV), "\ue036"),
    Pair(FontMacroKey("{btn:confirm}", ButtonType.AndroidTouch), "\ue046"),
    Pair(FontMacroKey("{btn:cancel}",  ButtonType.AndroidTouch), "\ue047"),
    Pair(FontMacroKey("{btn:confirm}", ButtonType.PC), "[Enter]"),
    Pair(FontMacroKey("{btn:cancel}",  ButtonType.PC), "[Esc]"),
)

val swapConfirmFontMacro = mapOf(
    Pair(FontMacroKey("{btn:confirm}", ButtonType.PlayStation), "\uf881"),
    Pair(FontMacroKey("{btn:cancel}",  ButtonType.PlayStation), "\uf880"),
    Pair(FontMacroKey("{btn:confirm}", ButtonType.Xbox), "\ue013"),
    Pair(FontMacroKey("{btn:cancel}",  ButtonType.Xbox), "\ue012"),
    Pair(FontMacroKey("{btn:confirm}", ButtonType.NSwitch), "\ue024"),
    Pair(FontMacroKey("{btn:cancel}",  ButtonType.NSwitch), "\ue023"),
    Pair(FontMacroKey("{btn:confirm}", ButtonType.AndroidTV), "\ue035"),
    Pair(FontMacroKey("{btn:cancel}",  ButtonType.AndroidTV), "\ue036"),
    Pair(FontMacroKey("{btn:confirm}", ButtonType.AndroidTouch), "\ue046"),
    Pair(FontMacroKey("{btn:cancel}",  ButtonType.AndroidTouch), "\ue047"),
    Pair(FontMacroKey("{btn:confirm}", ButtonType.PC), "[Enter]"),
    Pair(FontMacroKey("{btn:cancel}",  ButtonType.PC), "[Esc]"),
)

val fontMacros = mapOf(
    Pair(FontMacroKey("{btn:menu}", ButtonType.PC), "[Tab]"),
    Pair(FontMacroKey("{btn:sort}", ButtonType.PC), "[F1]"),
    Pair(FontMacroKey("{btn:home}", ButtonType.PC), "[Windows / Super]"),
    Pair(FontMacroKey("{btn:menu}", ButtonType.PlayStation), "\uf883"),
    Pair(FontMacroKey("{btn:sort}", ButtonType.PlayStation), "\uf882"),
    Pair(FontMacroKey("{btn:home}", ButtonType.PlayStation), "\uf892"),
    Pair(FontMacroKey("{btn:menu}", ButtonType.Xbox), "\ue015"),
    Pair(FontMacroKey("{btn:sort}", ButtonType.Xbox), "\ue014"),
    Pair(FontMacroKey("{btn:home}", ButtonType.Xbox), "\ue022"),
    Pair(FontMacroKey("{btn:menu}", ButtonType.NSwitch), "\ue026"),
    Pair(FontMacroKey("{btn:sort}", ButtonType.NSwitch), "\ue025"),
    Pair(FontMacroKey("{btn:home}", ButtonType.NSwitch), "\ue034"),
    Pair(FontMacroKey("{btn:menu}", ButtonType.AndroidTV), "\ue038"),
    Pair(FontMacroKey("{btn:sort}", ButtonType.AndroidTV), "\ue037"),
    Pair(FontMacroKey("{btn:home}", ButtonType.AndroidTV), "\ue044"),
    Pair(FontMacroKey("{btn:menu}", ButtonType.AndroidTouch), "\ue049"),
    Pair(FontMacroKey("{btn:sort}", ButtonType.AndroidTouch), "\ue048"),
    Pair(FontMacroKey("{btn:home}", ButtonType.AndroidTouch), "\ue056"),
)
