package id.psw.vshlauncher.views.VshServerSubcomponent

import android.graphics.Bitmap
import android.graphics.Canvas

object Notification {
    var isEnabled= true
    const val NOTIF_LONG = 15f
    const val NOTIF_MEDIUM = 10f
    const val NOTIF_SHORT = 5f
    class NotifItem (val content:String, val icon: Bitmap, val title:String = "", var duration:Float = NOTIF_MEDIUM){
        val isNoTitle get() = title.isBlank()
    }

    private val notifications = arrayListOf<NotifItem>()

    fun postNotification(content:String, icon: Bitmap, title:String = "", duration: Float = NOTIF_MEDIUM){
        notifications.add(NotifItem(content, icon, "", duration))
    }

    fun lNotification (canvas: Canvas, deltaTime:Float){
        if(!isEnabled) return
        val deadNotif = arrayListOf<NotifItem>()
        notifications.forEach {
            it.duration -= deltaTime
            if(it.duration < 0.0f) deadNotif.add(it)
        }
        notifications.removeAll(deadNotif)
    }
}
