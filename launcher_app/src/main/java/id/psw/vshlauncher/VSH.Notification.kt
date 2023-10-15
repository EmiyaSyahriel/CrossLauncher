package id.psw.vshlauncher

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.Vsh.Companion.TAG
import id.psw.vshlauncher.types.XmbNotification

fun Vsh.postNotification(icon: Bitmap?, title:String, description:String, time:Float = 3.0f, destroy:Boolean = false) : Long {
    synchronized(notifications){
        var hwnd = 0L
        while(notifications.find{it.handle==hwnd} != null){
            hwnd++;
        }
        val notif = XmbNotification(hwnd, icon, title, description, time, destroy);
        notifications.add(notif)
        return hwnd
    }
}

fun Vsh.postNotification(@DrawableRes drawableId:Int, title:String, description:String, time:Float = 3.0f) : Long {
    val drw = ResourcesCompat.getDrawable(resources, drawableId, null)
    val bmp = drw?.toBitmap(50,50)
    return postNotification(bmp, title, description, time, true)
}

fun Vsh.setNotificationTitle(hwnd:Long, title:String) =      synchronized(notifications) { notifications.filter{it.handle == hwnd}.forEach { it.title = title }}
fun Vsh.setNotificationDescription(hwnd:Long, desc:String) = synchronized(notifications) { notifications.filter { it.handle == hwnd }.forEach { it.desc = desc } }
fun Vsh.setNotificationIcon(hwnd:Long, icon:Bitmap?) =       synchronized(notifications) { notifications.filter{it.handle == hwnd}.forEach { it.icon = icon } }
fun Vsh.setNotificationTime(hwnd:Long, time:Float) =         synchronized(notifications) { notifications.filter{it.handle == hwnd}.forEach { it.remainingTime = time } }
fun Vsh.isNotificationExists(hWnd:Long) : Boolean =          synchronized(notifications) { notifications.indexOfFirst { it.handle == hWnd } >= 0 }
fun Vsh.removeNotification(hWnd:Long) : Boolean =            synchronized(notifications) { notifications.removeAll { it.handle == hWnd } }

fun Vsh.getUpdatedNotification() : ArrayList<XmbNotification> {
    synchronized(notifications){
        val cTime = SystemClock.uptimeMillis()
        val dTime = cTime - notificationLastCheckTime
        val fdTime = dTime / 1000.0f
        notificationLastCheckTime = cTime
        notifications.forEach { it.remainingTime -= fdTime }
        val lastNotifCount = notifications.size
        notifications.filter { it.destroy && it.remainingTime <= 0 }.forEach {
            it.icon?.recycle()
        }

        if(notifications.removeAll { it.remainingTime <= 0 }){
            Logger.d(TAG, "Removed ${lastNotifCount - notifications.size} Notification due to expired")
        }
        return notifications
    }
}