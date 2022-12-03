/**
 * Created by EmiyaSyahriel, 30/04/2022.
 * Vulkan Check File, uses dynamic link instead of included Vulkan library
 *
 * Btw, Vulkanisir, in case you don't know, is about putting new outer layer (grips) to an old tire
 * or when the tire outer grip have become flat, it's actually dangerous if the tire is too old or
 * it's inner layer is have become too thin. in which this library does a vulkan test in a way to
 * be usable on old device and is deemed dangerous because not using Vulkan stub library header.
 * (<vulkan/vulkan.h>)
 */
#include <jni.h>
#include <dlfcn.h>
#include <cstring>
#include <cstdio>

#ifdef __cplusplus
#define _API extern "C" JNIEXPORT
#elif
#define _API JNIEXPORT
#endif

//
#define BUFSZ 128

#define VK_MAKE_VERSION(major, minor, patch) (((major) << 22) | ((minor) << 12) | (patch))
#define VK_API_VERSION_1_0 VK_MAKE_VERSION(1, 0, 0)
#define VK_VERSION_MAJOR(version) ((uint32_t)(version) >> 22)
#define VK_VERSION_MINOR(version) (((uint32_t)(version) >> 12) & 0x3ff)
#define VK_VERSION_PATCH(version) ((uint32_t)(version) & 0xfff)

#define VKAPI_PTR
typedef int VkResult;
typedef VkResult (VKAPI_PTR *PFN_vkCreateInstance)(const void* pCreateInfo, const void* pAllocator, void* pInstance);
typedef VkResult (VKAPI_PTR *PFN_vkEnumerateInstanceVersion)(uint32_t* pApiVersion);
typedef void* PFN_vkVoidFunction;
typedef PFN_vkVoidFunction (VKAPI_PTR *PFN_vkGetInstanceProcAddr)(void* instance, const char* pName);

struct vkFuncPtr_t {
    PFN_vkGetInstanceProcAddr getInstanceProcAddr;
    PFN_vkCreateInstance createInstance;
    PFN_vkEnumerateInstanceVersion enumerateInstanceVersion;
} vk ;

struct vkCheckStates_t {
    char version_str_buffer[BUFSZ];
    void* vkLib;
} vks ;

const struct vkCheckConst_t {
    const char *libName, *GetInstanceProcAddr,* EnumerateInstanceVersion, *CreateInstance, *vkVerFmt;
} vkc = {
        "libvulkan.so","vkGetInstanceProcAddr",
    "vkEnumerateInstanceVersion","vkCreateInstance", "%i.%i.%i"
};

/** Check if Vulkan is supported, and load the library if not yet loaded */
_API jboolean JNICALL Java_id_psw_vshlauncher_submodules_VulkanisirSubmodule_isSupported(JNIEnv *env, jobject _) {
    if(vks.vkLib == nullptr)
        vks.vkLib = dlopen(vkc.libName, RTLD_LAZY);

    if(vks.vkLib == nullptr) return false;

    if(vk.getInstanceProcAddr == nullptr)
        vk.getInstanceProcAddr = (PFN_vkGetInstanceProcAddr)dlsym(vks.vkLib, vkc.GetInstanceProcAddr);
    if(vk.getInstanceProcAddr != nullptr)
        vk.createInstance = (PFN_vkCreateInstance)vk.getInstanceProcAddr(nullptr, vkc.CreateInstance);
    else return false;
    return vk.createInstance != nullptr;
}

/**  Get Vulkan API Version, returns null if not supported */
_API jstring JNICALL Java_id_psw_vshlauncher_submodules_VulkanisirSubmodule_getVersion(JNIEnv *env, jobject _) {
    // Return null if not supported
    if(vk.getInstanceProcAddr == nullptr) return nullptr;

    uint32_t ver = VK_API_VERSION_1_0;
    if(vk.enumerateInstanceVersion == nullptr)
        vk.enumerateInstanceVersion = PFN_vkEnumerateInstanceVersion(vk.getInstanceProcAddr(nullptr, vkc.EnumerateInstanceVersion));

    if(vk.enumerateInstanceVersion) vk.enumerateInstanceVersion(&ver);

    snprintf(vks.version_str_buffer, BUFSZ, vkc.vkVerFmt,
             VK_VERSION_MAJOR(ver), VK_VERSION_MINOR(ver), VK_VERSION_PATCH(ver));
    return (env)->NewStringUTF(vks.version_str_buffer);
}

/** Unload Vulkan lib from process memory */
_API void JNICALL Java_id_psw_vshlauncher_submodules_VulkanisirSubmodule_close(JNIEnv* env, jobject _){
    if(vks.vkLib != nullptr) dlclose(vks.vkLib);
}