package id.psw.vshlauncher

object MimeTypeDict {
    val maps = mapOf(
        Pair("mp3","audio/mpeg"),
        Pair("midi","audio/midi"),
        Pair("mid","audio/midi"),
        Pair("jpg","image/jpeg"),
        Pair("jpeg","image/jpeg"),
        Pair("png","image/png"),
        Pair("gif","image/gif")
    )

    val sizeMap = arrayOf(
        "B",
        "kB",
        "MB",
        "GB",
        "TB",
        "PB",
        "EB"
    )
}