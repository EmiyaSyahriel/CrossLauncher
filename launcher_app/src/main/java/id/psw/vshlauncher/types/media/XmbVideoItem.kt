package id.psw.vshlauncher.types.media

import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.types.XmbItem
import id.psw.vshlauncher.views.asBytes

class XmbVideoItem(vsh: Vsh, val data : VideoData) : XmbItem(vsh) {
    override val displayName: String
        get() = data.displayName

    override val description: String
        get() = data.size.asBytes()
}