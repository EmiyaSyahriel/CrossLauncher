/***
 * Temporary collection object, contains buffers and temporary instances of object
 **/

package id.psw.vshlauncher

import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF

object Temp {
    var tempRectF = RectF(0f,0f,0f,0f)
    var tempRect = Rect(0,0,0,0)
    var buffer4 = ByteArray(4)
    var buffer2 = ByteArray(2)
    var tempPointF = PointF(0f,0f)
    var tempPoint = Point(0,0)
}