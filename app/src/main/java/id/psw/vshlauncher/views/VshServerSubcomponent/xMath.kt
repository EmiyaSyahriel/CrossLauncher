package id.psw.vshlauncher.views.VshServerSubcomponent

import kotlin.math.abs

object xMath{
    fun max(vararg numbers: Float):Float{
        var retval = numbers[0]
        numbers.forEach { if(it > retval) retval = it }
        return retval
    }

    fun min(vararg numbers: Float):Float{
        var retval = numbers[0]
        numbers.forEach { if(it < retval) retval = it }
        return retval
    }
    fun maxAbs(vararg numbers: Float):Float{
        var retval = abs(numbers[0])
        numbers.forEach { if(abs(it) > abs(retval)) retval = abs(it) }
        return retval
    }

    fun minAbs(vararg numbers: Float):Float{
        var retval = abs(numbers[0])
        numbers.forEach { if(abs(it) < abs(retval)) retval = abs(it) }
        return retval
    }

    fun lerp(a: Float, b: Float, t: Float): Float = a + ((b - a) * t)
}
