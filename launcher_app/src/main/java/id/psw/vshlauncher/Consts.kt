package id.psw.vshlauncher

object Consts {
    const val XMB_DEFAULT_ITEM_ID: String = "id_null"
    const val XMB_DEFAULT_ITEM_DISPLAY_NAME : String = "No Name"
    const val XMB_DEFAULT_ITEM_DESCRIPTION : String = "No Description"
    const val XMB_DEFAULT_ITEM_VALUE : String = "No Value"
    const val XMB_KEY_APP_SORT_MODE = "sort_mode"
    const val XMB_DEFAULT_RESOLUTION = 0x050002D0
}


@Suppress("SpellCheckingInspection")
object PrefEntry{
    /** Int (0 = PS3, 1 = PSP, 2 = Bravia, 3 = PSX) */
    const val MENU_LAYOUT = "/crosslauncher/xmbview/layout"
    /** Boolean */
    const val DISABLE_EPILEPSY_WARNING = "/crosslauncher/coldboot/skipEpilepsyWarning"
    /** String, Format : "lang|country|variant", Empty = System */
    const val SYSTEM_LANGUAGE = "/setting/system/language"
    /** Int, 0 = O Confirm, 1 = X Confirm*/
    const val CONFIRM_BUTTON = "/setting/system/buttonAssign"
    /** Int, Format : ((Short)w << 16 | (Short)h) */
    const val REFERENCE_RESOLUTION = "/setting/display/0/resolution"
    /** Int, See [android.content.pm.ActivityInfo.screenOrientation] */
    const val DISPLAY_ORIENTATION = "/setting/display/0/orientation"
    /** Boolean */
    const val DISPLAY_VIDEO_ICON = "/crosslauncher/menu/playVideoIcon"
    /** Boolean */
    const val DISPLAY_BACKDROP = "/crosslauncher/menu/showBackdrop"
    /** Boolean */
    const val PLAY_BACK_SOUND = "/crosslauncher/menu/playBgSound"
    /** Boolean */
    const val DISPLAY_DISABLE_STATUS_BAR = "/crosslauncher/menu/noStatusBar"
    /** String, Data:
     * - ```{network}``` : Connected Network Name
     * - ```{battery}``` : Current Battery Percent
     * - ```{datetime}``` : Date Time
     * - Other value : Keep
     * */
    const val DISPLAY_STATUS_BAR_FORMAT = "/crosslauncher/menu/statusBarFormat"
    /** String, See [SimpleDateFormat](https://developer.android.com/reference/java/text/SimpleDateFormat) */
    const val DISPLAY_DIGITAL_CLOCK_FORMAT = "/crosslauncher/menu/clockFormat"
    /** Boolean */
    const val DISPLAY_SHOW_CLOCK_SECOND = "/crosslauncher/menu/ps3/analogSecond"
    /** Boolean */
    const val DISPLAY_PSP_PAD_STATUS = "/crosslauncher/menu/psp/padStatus"
    /** Int,
     * - 0 = None
     * - 1 = Analog Clock
     * - 2 = Battery
     * - 3 = Network Strength
     */
    const val DISPLAY_STATUS_DISPLAY = "/crosslauncher/menu/statusBarDisplay"
    /** Int, SurfaceView FPS Limit, 0 = Infinite */
    const val SURFACEVIEW_FPS_LIMIT = "/crosslauncher/xmb/fps"
}

enum class FittingMode {
    STRETCH,
    FIT,
    FILL
}

enum class AppItemSorting{
    Name,
    PackageName,
    UpdateTime,
    FileSize
}