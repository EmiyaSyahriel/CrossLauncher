package id.psw.vshlauncher.services

import android.app.Notification
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.R
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.postNotification
import id.psw.vshlauncher.types.XmbItem
import id.psw.vshlauncher.vsh

class SystemNotificationListener : NotificationListenerService() {
    companion object {
        private var _componentName : ComponentName = ComponentName("id.psw.vshlauncher.services", "SystemNotificationListener")
        val componentName : ComponentName get() = _componentName

        fun getIsAllowed(vsh: Vsh) : Boolean {
            _componentName =  ComponentName(vsh, SystemNotificationListener::class.java)
            val svc = vsh.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                svc.isNotificationListenerAccessGranted(componentName)
            }else{
                val enabledListeners =
                    Settings.Secure.getString(vsh.contentResolver, "enabled_notification_listeners")

                if (enabledListeners.isEmpty()) return false

                return enabledListeners.split(":").map {
                    ComponentName.unflattenFromString(it)
                }.any {cName ->
                    componentName == cName
                }
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        // Skip onGoing or null
        if(sbn?.isOngoing != false) return

        val title = sbn.notification.extras.getString(Notification.EXTRA_TITLE) ?: return // Skip if title is null


        val desc =
            sbn.notification.extras.getCharSequence(Notification.EXTRA_TEXT_LINES)?.toString() ?:
            sbn.notification.extras.getString(Notification.EXTRA_TEXT) ?:
            getString(R.string.sysnotif_no_info)
        val icon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // TODO : I'm still not sure, this
            sbn.notification?.getLargeIcon()?.loadDrawable(this)?.toBitmap(300, 300, Bitmap.Config.ARGB_8888) ?:
            sbn.notification?.smallIcon?.loadDrawable(this)?.toBitmap(300, 300, Bitmap.Config.ARGB_8888)
        }else{
            sbn.notification.extras.getParcelable(Notification.EXTRA_LARGE_ICON)
        }
        vsh.postNotification(icon, title, desc, 10.0f, true)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // Do nothing, it is abstract up until API 21
    }
}