package id.psw.vshlauncher.views

enum class VshViewPage {
    ColdBoot,
    MainMenu,
    HomeScreen,
    Dialog,
    GameBoot,
}

data class VshViewStates (
    val coldBoot : VshViewColdBootState = VshViewColdBootState(),
    val gameBoot : VshViewGameBootState = VshViewGameBootState(),
    val crossMenu : VshViewMainMenuState = VshViewMainMenuState(),
    val dialog : VshViewDialogState = VshViewDialogState(),
    val itemMenu : ItemMenuState = ItemMenuState(),
    val home : HomeScreenState = HomeScreenState(),
        )

enum class XMBLayoutType{
    PS3,
    PSP,
    PSX,
    Bravia,
}
