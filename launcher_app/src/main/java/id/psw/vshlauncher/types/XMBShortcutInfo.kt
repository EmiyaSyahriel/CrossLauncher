package id.psw.vshlauncher.types

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.os.Build
import android.os.Parcel
import android.os.UserHandle
import android.util.Base64
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.select
import java.io.File

class XMBShortcutInfo {

    companion object {
        const val INI_TYPE = "CrossLauncher.ShortcutInfo"
        const val INI_ID = "ID"
        const val INI_CXL_ID = "CXL_ID"
        const val INI_NAME = "SNAME"
        const val INI_LNAME = "LNAME"
        const val INI_PKG = "PACKAGE"
        const val INI_CATEGORY = "CATEGORY"
        const val INI_ENABLED = "BOOTABLE"
        const val INI_DISABLED_MSG = "NBOOTMSG"
        const val INI_SHORTCUT_INFO = "SHELLDATA"
        const val INI_HANDLE = "USER"

        const val DEF_ID = "ko.id"
        const val DEF_CXL_ID = "ko.id"
        const val DEF_NAME = "Unknown"
        const val DEF_LNAME = "Unknown Shortcut"
        const val DEF_CATEGORY = ""
        const val DEF_SHORTCUT_INFO = "00"
        const val DEF_PKG = "id.psw.vshlauncher"
        const val DEF_DISABLED_MSG = "???"
        const val DEF_HANDLE = ""
    }

    var icon : Bitmap = XMBItem.TRANSPARENT_BITMAP
    var id : String = DEF_ID
    var idInLauncher : String = DEF_CXL_ID
    var name : String = DEF_NAME
    var longName : String = DEF_LNAME
    var packageName : String = DEF_PKG
    var enabled : Boolean = false
    var category : String = DEF_CATEGORY
    var disabledMsg : String = DEF_DISABLED_MSG
    var userHandle : UserHandle? = null
    var pinItem : LauncherApps.PinItemRequest? = null

    private val ini = INIFile()

    constructor(vsh: Vsh, intent: Intent){
        var isNextGen = false

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if(intent.action!!.equals(LauncherApps.ACTION_CONFIRM_PIN_SHORTCUT, true)){
                val lcher = vsh.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
                val cPinItem = lcher.getPinItemRequest(intent)

                val shInfo = cPinItem.shortcutInfo
                if(shInfo != null){
                    icon = lcher
                        .getShortcutIconDrawable(shInfo, vsh.resources.displayMetrics.densityDpi)
                        .toBitmap(100,100)
                    id = shInfo.id
                    name = shInfo.shortLabel?.toString() ?: name
                    longName = shInfo.longLabel?.toString() ?: shInfo.shortLabel?.toString() ?: longName
                    packageName = shInfo.activity?.packageName ?: packageName
                    enabled = shInfo.isEnabled
                    category = DEF_CATEGORY
                    disabledMsg = shInfo.disabledMessage?.toString() ?: DEF_DISABLED_MSG
                    userHandle = shInfo.userHandle
                    isNextGen = true
                }

                pinItem = cPinItem
            }
        }

        // Use old Android static shortcut installation
        if(!isNextGen){
            if(intent.action!!.equals(Intent.ACTION_CREATE_SHORTCUT, true)){
                name = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME) ?: name
                icon = intent.getParcelableExtra<Bitmap>(Intent.EXTRA_SHORTCUT_ICON) ?: XMBItem.WHITE_BITMAP
                packageName = intent.component?.toShortString() ?: packageName
            }
        }
    }

    fun saveIcon(iconFile:File){
        if(!iconFile.exists()){
            iconFile.createNewFile()
        }
        val outStream = iconFile.outputStream()
        icon.compress(Bitmap.CompressFormat.PNG, 100, outStream)
    }

    constructor(vsh:Vsh, iniPath: File){
        ini.parseFile(iniPath.absolutePath)
        id = ini[INI_TYPE, INI_ID] ?: DEF_ID
        idInLauncher = ini[INI_TYPE, INI_CXL_ID] ?: DEF_CXL_ID
        name = ini[INI_TYPE, INI_NAME] ?: DEF_NAME
        longName = ini[INI_TYPE, INI_LNAME] ?: DEF_LNAME
        packageName = ini[INI_TYPE, INI_PKG] ?: DEF_PKG
        category = ini[INI_TYPE, INI_CATEGORY] ?: DEF_CATEGORY
        enabled = ini[INI_TYPE, INI_ENABLED] == "true"
        disabledMsg = ini[INI_TYPE, INI_DISABLED_MSG] ?: DEF_DISABLED_MSG
        val uHandleStr = ini[INI_TYPE, INI_HANDLE] ?: ""

        if(uHandleStr.isNotEmpty()){
            val p = Parcel.obtain()
            val dat = Base64.decode(uHandleStr, Base64.DEFAULT)
            p.writeByteArray(dat)
            p.setDataPosition(0)
            userHandle = UserHandle.readFromParcel(p)
            p.recycle()
        }
    }

    fun write(iniPath: File){
        ini[INI_TYPE, INI_ID] = id
        ini[INI_TYPE, INI_NAME] = name
        ini[INI_TYPE, INI_CATEGORY] = category
        ini[INI_TYPE, INI_CXL_ID] = idInLauncher
        ini[INI_TYPE, INI_LNAME] = longName
        ini[INI_TYPE, INI_PKG] = packageName
        ini[INI_TYPE, INI_DISABLED_MSG] = disabledMsg
        ini[INI_TYPE, INI_ENABLED] = enabled.select("true","false")

        val h = userHandle
        var uHandleStr = ""
        if(h != null){
            val p = Parcel.obtain()
            val len = p.dataSize()
            val ba = ByteArray(len)
            h.writeToParcel(p, 0)
            p.setDataPosition(0)
            p.readByteArray(ba)
            uHandleStr = Base64.encodeToString(ba, Base64.DEFAULT)
            p.recycle()
        }

        ini[INI_TYPE, INI_HANDLE] = uHandleStr
        ini.write(iniPath.absolutePath)
    }
}