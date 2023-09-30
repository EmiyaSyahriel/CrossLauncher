package id.psw.vshlauncher.views.dialogviews

import android.app.ActivityManager
import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.text.format.Formatter
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.BuildConfig
import id.psw.vshlauncher.R
import id.psw.vshlauncher.select
import id.psw.vshlauncher.submodules.VulkanisirSubmodule
import id.psw.vshlauncher.types.XmbItem
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.XmbDialogSubview
import id.psw.vshlauncher.views.XmbView

class AboutDeviceDialogView(v: XmbView) : XmbDialogSubview(v) {
    override val hasNegativeButton: Boolean = true
    override val hasPositiveButton: Boolean = false
    override val negativeButton: String = vsh.getString(R.string.common_back)
    override val icon: Bitmap = ResourcesCompat.getDrawable(vsh.resources, R.drawable.icon_info, null)?.toBitmap(64,64) ?: XmbItem.TRANSPARENT_BITMAP
    override val title: String
        get() = vsh.getString(R.string.setting_systeminfo_name)

    private val strData = mutableMapOf<String, String>()
    private val strPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = FontCollections.masterFont
        textSize = 20.0f
        color = Color.WHITE
    }

    override fun onStart() {
        val actSvc = vsh.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val isGo = actSvc.isLowRamDevice.select(" (Go Edition)","")

        strData[vsh.getString(R.string.string_systeminfo_androidver_name)] = "${Build.VERSION.RELEASE}$isGo (API ${Build.VERSION.SDK_INT})"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            strData[vsh.getString(R.string.string_systeminfo_android_security_patch)] = Build.VERSION.SECURITY_PATCH
        }
        strData[vsh.getString(R.string.string_systeminfo_launcher_ver)] = "${BuildConfig.BUILD_TYPE} | ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        strData[vsh.getString(R.string.string_systeminfo_androidmdl_name)] = "${Build.MANUFACTURER} ${Build.MODEL}"
        strData[vsh.getString(R.string.string_systeminfo_board)] = Build.BOARD
        strData[vsh.getString(R.string.string_systeminfo_hardware)] = Build.HARDWARE
        val configInfo = actSvc.deviceConfigurationInfo
        val memInfo = ActivityManager.MemoryInfo()
        actSvc.getMemoryInfo(memInfo)
        val mbFreeMem = Formatter.formatFileSize(vsh, memInfo.availMem)
        val mbTotalMem = Formatter.formatFileSize(vsh, memInfo.totalMem)

        val statFs = StatFs(Environment.getDataDirectory().path)
        val romFree = Formatter.formatFileSize(vsh, statFs.freeBytes)
        val romTotal = Formatter.formatFileSize(vsh, statFs.totalBytes)

        strData[vsh.getString(R.string.settings_systeminfo_opengles_ver)] = configInfo.glEsVersion

        var vulkanVer = vsh.getString(R.string.common_not_supported)
        if(VulkanisirSubmodule.isSupported()){
            vulkanVer = VulkanisirSubmodule.getVersion()
        }

        strData[vsh.getString(R.string.settings_systeminfo_vulkan_ver)] = vulkanVer
        strData[vsh.getString(R.string.settings_systeminfo_bootloader)] = Build.BOOTLOADER
        strData[vsh.getString(R.string.settings_systeminfo_storage_usage)] = "$romFree / $romTotal"
        strData[vsh.getString(R.string.settings_systeminfo_ram_usage)] = "$mbFreeMem / $mbTotalMem"
    }

    override fun onClose() {
        if(icon != XmbItem.TRANSPARENT_BITMAP){
            icon.recycle()
        }
    }

    override fun onDialogButton(isPositive: Boolean) {
        if(!isPositive){
            finish(view.screens.mainMenu)
        }
    }

    override fun onDraw(ctx: Canvas, drawBound: RectF, deltaTime: Float) {
        val lineSz= (strPaint.textSize * 1.25f)
        val totalSz = strData.size * lineSz
        var y = drawBound.centerY() - (totalSz / 2.0f)
        val centerL = drawBound.centerX() - 10.0f
        val centerR = drawBound.centerX() + 10.0f
        try{
            strData.forEach {
                strPaint.textAlign = Paint.Align.RIGHT
                ctx.drawText(it.key, centerL, y, strPaint)
                strPaint.textAlign = Paint.Align.LEFT
                ctx.drawText(it.value, centerR, y, strPaint)
                y += lineSz
            }
        }catch(e:ConcurrentModificationException){ }
        super.onDraw(ctx, drawBound, deltaTime)
    }
}