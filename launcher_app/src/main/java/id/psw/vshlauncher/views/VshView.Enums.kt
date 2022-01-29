package id.psw.vshlauncher.views

enum class VshViewPage {
    ColdBoot,
    MainMenu,
    GameBoot
}

data class VshViewStates (
    val coldBoot : VshViewColdBootState = VshViewColdBootState(),
    val gameBoot : VshViewGameBootState = VshViewGameBootState(),
    val menu : VshViewMainMenuState = VshViewMainMenuState()
        )

enum class XMBLayoutType{
    PS3,
    PSP,
    PSX,
    Bravia,
}
