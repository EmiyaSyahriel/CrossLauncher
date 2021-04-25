package id.psw.vshlauncher.icontypes

import android.content.Context
import id.psw.vshlauncher.VSH

class VshOptionedSettingIcon(
    itemID: Int,
    context: VSH,
    name: String,
    iconId: String,
    private var onClick: () -> Unit,
    valueStr: () -> String
) : VshSettingIcon(
    itemID, context, name, iconId, onClick, valueStr
) {
}