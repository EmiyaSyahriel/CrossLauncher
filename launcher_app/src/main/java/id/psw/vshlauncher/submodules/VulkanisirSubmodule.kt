package id.psw.vshlauncher.submodules

import id.psw.vshlauncher.VSH

object VulkanisirSubmodule {
    init{
        System.loadLibrary("vulkanisir")
    }

    /** Check if Vulkan is supported, load Vulkan stub library if not yet loaded */
    external fun isSupported() : Boolean
    /** Get maximum supported Vulkan version, return null if not supported */
    external fun getVersion() : String
    /** Unload Vulkan stub library from process memory */
    external fun close()
}