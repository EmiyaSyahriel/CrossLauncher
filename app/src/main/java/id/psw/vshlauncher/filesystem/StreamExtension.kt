package id.psw.vshlauncher.filesystem

import java.io.InputStream
import java.nio.ByteBuffer

private object Buffer
{
    val b2 = ByteBuffer.allocate(2)
    val b4 = ByteBuffer.allocate(4)
    val b8 = ByteBuffer.allocate(8)
}

fun InputStream.readShort() : Short{
    read(Buffer.b2.array())
    return Buffer.b2.short
}

fun InputStream.readInt() : Int{
    read(Buffer.b4.array())
    return Buffer.b4.int
}

fun InputStream.readLong() : Long{
    read(Buffer.b8.array())
    return Buffer.b8.long
}

@ExperimentalUnsignedTypes
fun InputStream.readUShort() : UShort{
    read(Buffer.b2.array())
    return Buffer.b2.short.toUShort()
}

@ExperimentalUnsignedTypes
fun InputStream.readUInt() : UInt{
    read(Buffer.b4.array())
    return Buffer.b4.int.toUInt()
}

@ExperimentalUnsignedTypes
fun InputStream.readULong() : ULong{
    read(Buffer.b8.array())
    return Buffer.b8.long.toULong()
}