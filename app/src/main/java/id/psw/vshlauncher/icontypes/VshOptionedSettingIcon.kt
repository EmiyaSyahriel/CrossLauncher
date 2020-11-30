package id.psw.vshlauncher.icontypes

import android.content.Context

class VshOptionedSettingIcon(
    itemID: Int,
    private var context: Context,
    name: String,
    iconId: String,
    private var onClick: () -> Unit,
    valueStr: () -> String,
    var objOptions: () -> ArrayList<VshOption>
) : VshSettingIcon(
    itemID, context, name, iconId, onClick, valueStr
) {

    override val options: ArrayList<VshOption>
        get() = objOptions()

    override val hasOptions: Boolean
        get() = options.size > 0
}