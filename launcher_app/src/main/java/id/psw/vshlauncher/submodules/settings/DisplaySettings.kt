package id.psw.vshlauncher.submodules.settings

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import id.psw.vshlauncher.PrefEntry
import id.psw.vshlauncher.R
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.addAllV
import id.psw.vshlauncher.select
import id.psw.vshlauncher.submodules.PadType
import id.psw.vshlauncher.submodules.SettingsSubmodule
import id.psw.vshlauncher.types.items.XmbMenuItem
import id.psw.vshlauncher.types.items.XmbSettingsCategory
import id.psw.vshlauncher.types.items.XmbSettingsItem
import id.psw.vshlauncher.views.XmbLayoutType
import id.psw.vshlauncher.views.dialogviews.CustomAspectRatioDialogView
import id.psw.vshlauncher.views.dialogviews.StatusBarFormatDialogView

class DisplaySettings(private val vsh: Vsh): ISettingsCategories(vsh) {

    private fun saveButtonDisplaySetting() {
        val view = vsh.xmbView
        if(view != null) {
            val srlzButton = when (M.gamepadUi.activeGamepad) {
                PadType.PlayStation -> 0
                PadType.Xbox -> 1
                PadType.Nintendo -> 2
                PadType.Android -> 3
                else -> 0
            }
            M.pref.set(PrefEntry.BUTTON_DISPLAY_TYPE, srlzButton)
        }
    }

    private fun saveLayoutSetting() {
        val view = vsh.xmbView
        if(view != null) {
            val srlzLayout = when (view.screens.mainMenu.layoutMode) {
                XmbLayoutType.PS3 -> 0
                XmbLayoutType.PSP -> 1
                XmbLayoutType.Bravia -> 2
                XmbLayoutType.PSX -> 3
                else -> 0
            }
            M.pref.set(PrefEntry.MENU_LAYOUT, srlzLayout)
        }
    }

    val wave = WaveSettings(vsh)

    private fun mkItemDisplayLayout() : XmbSettingsItem {
        val text = {
                when(vsh.xmbView?.screens?.mainMenu?.layoutMode){
                    XmbLayoutType.PSP -> "PlayStation Portable"
                    XmbLayoutType.PS3 -> "PlayStation 3"
                    XmbLayoutType.PSX -> "PSX DVR"
                    XmbLayoutType.Bravia -> "Bravia TV"
                    else -> vsh.getString(R.string.unknown)
                }
            }

        val click ={
            val view = vsh.xmbView
            if(view != null){
                view.screens.mainMenu.layoutMode = when(view.screens.mainMenu.layoutMode){
                    XmbLayoutType.PSP -> XmbLayoutType.PS3
                    XmbLayoutType.PS3 -> XmbLayoutType.PSP
                    else -> XmbLayoutType.PS3
                }
                saveLayoutSetting()
            }
        }

        val menu = { it : XmbSettingsItem ->
            it.hasMenu = true
            val dMenu = ArrayList<XmbMenuItem>()

            dMenu.add(
                XmbMenuItem.XmbMenuItemLambda(
                    {"PlayStation Portable"}, {false}, 1)
                {
                    vsh.xmbView?.screens?.mainMenu?.layoutMode = XmbLayoutType.PSP
                    saveLayoutSetting()
                })
            dMenu.add(
                XmbMenuItem.XmbMenuItemLambda(
                    {"PlayStation 3"}, {false}, 0)
                {
                    vsh.xmbView?.screens?.mainMenu?.layoutMode = XmbLayoutType.PS3
                    saveLayoutSetting()
                })
            it.menuItems = dMenu
        }

        return XmbSettingsItem(vsh,
            "setting_display_layout_0",
            R.string.setting_display_layout_type,
            R.string.setting_display_layout_type_desc,
            R.drawable.icon_orientation, text, click
        ) .apply(menu)
    }

    private fun mkItemVshStatusBar() : XmbSettingsItem {
        val text =  {
            val id = (vsh.xmbView?.widgets?.statusBar?.disabled == true).select(R.string.common_yes, R.string.common_no)
            vsh.getString(id)
        }

        val click = {
            val x = vsh.xmbView
            if (x != null) {
                x.widgets.statusBar.disabled = !x.widgets.statusBar.disabled
                M.pref.set(
                    PrefEntry.DISPLAY_DISABLE_STATUS_BAR,
                    x.widgets.statusBar.disabled.select(1, 0)
                )
            }
        }
        return XmbSettingsItem(vsh, "settings_display_hide_bar",
            R.string.settings_display_hide_statusbar_name,
            R.string.settings_display_hide_statusbar_desc,
            R.drawable.icon_hidden,text, click)
    }

    private fun mkItemBtnDisplay() : XmbSettingsItem {
        val text = {
                when(M.gamepadUi.activeGamepad){
                    PadType.Unknown -> vsh.getString(R.string.common_default)
                    PadType.PlayStation -> "PlayStation"
                    PadType.Android -> "Android"
                    PadType.Xbox -> "Xbox"
                    PadType.Nintendo -> "Nintendo Switch"
                    else -> vsh.getString(R.string.unknown)
                }
            }

        val click : () -> Unit = {
            vsh.xmbView?.showSideMenu(true)
        }

        val menu = { m : XmbSettingsItem ->
            m.hasMenu = true
            val dMenu = ArrayList<XmbMenuItem>()

            dMenu.add(
                XmbMenuItem.XmbMenuItemLambda(
                    {"PlayStation"}, {false}, 0)
                {
                    M.gamepadUi.activeGamepad = PadType.PlayStation
                    saveButtonDisplaySetting()
                })
            dMenu.add(
                XmbMenuItem.XmbMenuItemLambda(
                    {"Xbox"}, {false}, 1)
                {
                    M.gamepadUi.activeGamepad = PadType.Xbox
                    saveLayoutSetting()
                })
            dMenu.add(
                XmbMenuItem.XmbMenuItemLambda(
                    {"Nintendo Switch"}, {false}, 2)
                {
                    M.gamepadUi.activeGamepad = PadType.Nintendo
                    saveLayoutSetting()
                })
            dMenu.add(
                XmbMenuItem.XmbMenuItemLambda(
                    {"Android"}, {false}, -1)
                {
                    M.gamepadUi.activeGamepad = PadType.Android
                    saveLayoutSetting()
                })
            m.menuItems= dMenu
        }

        return XmbSettingsItem(vsh, "settings_display_button_type",
            R.string.setting_display_button_type_name,
            R.string.setting_display_button_type_desc,
            R.drawable.icon_button_display, text, click
        ).apply (menu)
    }

    private fun mkItemAnalogSecond() : XmbSettingsItem {
        return XmbSettingsItem(vsh, "settings_display_analog_second",
            R.string.settings_display_clock_second_analog_name,
            R.string.settings_display_clock_second_analog_desc,
            R.drawable.icon_clock,
            { (vsh.xmbView?.widgets?.analogClock?.showSecondHand == true).select(vsh.getString(R.string.common_yes),vsh.getString(
                R.string.common_no))  }
        ){
            val x = vsh.xmbView
            if(x != null){
                x.widgets.analogClock.showSecondHand = !x.widgets.analogClock.showSecondHand
                M.pref.set(PrefEntry.DISPLAY_SHOW_CLOCK_SECOND, x.widgets.analogClock.showSecondHand.select(1, 0))

            }
        }
    }

    private fun mkItemStatusTextFormat(): XmbSettingsItem{
        return XmbSettingsItem(vsh, "settings_display_statusbar_fmt",
            R.string.settings_display_statusbar_fmt_name,
            R.string.settings_display_statusbar_fmt_desc,
            R.drawable.icon_clock,
            {
                val xmb = vsh.xmbView
                xmb?.widgets?.statusBar?.format(xmb.widgets.statusBar.dateTimeFormat) ?: ""
            }
        ){
            val xmb = vsh.xmbView
            xmb?.showDialog(StatusBarFormatDialogView(xmb))
        }
    }

    private fun mkDisplayOperatorName() : XmbSettingsItem {
        val text = { (vsh.xmbView?.widgets?.statusBar?.showMobileOperator == true).select(vsh.getString(
            R.string.common_yes),vsh.getString(R.string.common_no))
        }

        val click = {
            val x = vsh.xmbView
            if(x != null){
                val v = x.widgets.statusBar.showMobileOperator

                if(!v){
                    if(Build.VERSION.SDK_INT >= 23){
                        if(
                            vsh.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED ||
                            vsh.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        ){
                            x.widgets.statusBar.showMobileOperator = true
                        }else{
                            ActivityCompat.requestPermissions(x.context as Activity, arrayOf(
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ), 1999)
                        }
                    }else{
                        x.widgets.statusBar.showMobileOperator = true
                    }
                }else{
                    x.widgets.statusBar.showMobileOperator = false
                }
            }
        }

        return XmbSettingsItem(vsh, "settings_display_operator",
            R.string.settings_display_show_operator_name,
            R.string.settings_display_show_operator_desc,
            R.drawable.icon_network,
            text, click
        )
    }

    private fun mkItemReferenceSize() : XmbSettingsItem {
        val text = {
            var r = ""
            val v = vsh.xmbView
            if(v != null){
                val w = v.scaling.landTarget.width().toInt()
                val h = v.scaling.landTarget.height().toInt()
                r = "${w}x$h"
            }
            r
        }

        val click : () -> Unit = {
            val xv = vsh.xmbView
            xv?.showDialog(CustomAspectRatioDialogView(xv))
        }
        return XmbSettingsItem(vsh, "settings_screen_reference_size",
            R.string.settings_display_refsize_name,
            R.string.settings_display_refsize_desc,
            R.drawable.ic_fullscreen,
            text, click
        )
    }

    private fun mkItemBackgroundDim() : XmbSettingsItem {
        val text = {
            (vsh.xmbView?.screens?.mainMenu?.dimOpacity?: 0).toString()
        }

        val click : () -> Unit = {
            vsh.xmbView?.showSideMenu(true)
        }

        val menu = { m : XmbSettingsItem ->
            m.hasMenu = true
            val dMenu = arrayListOf<XmbMenuItem>()
            for(i in 0 .. 10){
                dMenu.add(
                    XmbMenuItem.XmbMenuItemLambda(
                        { i.toString() }, {false}, i)
                    {
                        vsh.xmbView?.screens?.mainMenu?.dimOpacity = i
                        M.pref.set(PrefEntry.BACKGROUND_DIM_OPACITY, vsh.xmbView?.screens?.mainMenu?.dimOpacity ?: 0)
                    })
            }
            m.menuItems = dMenu
        }

        return XmbSettingsItem(vsh, "settings_display_bg_dim",
            R.string.settings_display_background_dim_name,
            R.string.settings_display_background_dim_desc,
            R.drawable.icon_brightness, text, click
        ).apply(menu)

    }

    override fun createCategory() : XmbSettingsCategory {

        return XmbSettingsCategory(vsh,
            SettingsSubmodule.CATEGORY_SETTINGS_DISPLAY,
            R.drawable.settings_category_display,
            R.string.settings_category_display_name,
            R.string.settings_category_display_desc
        ).apply {
            content.addAllV(mkItemDisplayLayout(),
                mkItemVshStatusBar(),
                mkItemBtnDisplay(),
                mkItemAnalogSecond(),
                mkItemStatusTextFormat(),
                mkDisplayOperatorName(),
                mkItemReferenceSize(),
                mkItemBackgroundDim(),
                wave.createCategory()
            )
        }
    }
}