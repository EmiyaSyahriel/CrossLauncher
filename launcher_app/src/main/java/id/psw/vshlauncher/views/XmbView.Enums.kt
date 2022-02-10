package id.psw.vshlauncher.views

enum class VshViewPage {
    ColdBoot,
    MainMenu,
    Dialog,
    GameBoot,
}

data class VshViewStates (
    val coldBoot : VshViewColdBootState = VshViewColdBootState(),
    val gameBoot : VshViewGameBootState = VshViewGameBootState(),
    val crossMenu : VshViewMainMenuState = VshViewMainMenuState(),
    val dialog : VshViewDialogState = VshViewDialogState(),
    val itemMenu : ItemMenuState = ItemMenuState()
        )

enum class XMBLayoutType{
    PS3,
    PSP,
    PSX,
    Bravia,
}
