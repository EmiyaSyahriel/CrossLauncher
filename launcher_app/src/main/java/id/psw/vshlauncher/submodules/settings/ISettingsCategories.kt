package id.psw.vshlauncher.submodules.settings

import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.types.items.XmbSettingsCategory

abstract class ISettingsCategories(private val vsh: Vsh) {
    protected val M get() = vsh.M
    abstract fun createCategory() : XmbSettingsCategory
}