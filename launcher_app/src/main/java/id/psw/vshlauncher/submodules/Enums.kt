package id.psw.vshlauncher.submodules

import id.psw.vshlauncher.select

enum class SfxType {
    Selection,
    Confirm,
    Cancel
}

enum class PadType {
    Unknown,
    PlayStation,
    Xbox,
    Nintendo,
    Android
}

enum class PadKey(val base:UByte) {
    None (0b0u),
    PS              (0b00000001u),
    Start           (0b00000010u),
    Select          (0b00000100u),
    Triangle        (0b00010001u),
    Square          (0b00010010u),
    Circle          (0b00010100u),
    Cross           (0b00011000u),
    PadU            (0b00100001u),
    PadD            (0b00100010u),
    PadL            (0b00100100u),
    PadR            (0b00101000u),
    L1              (0b01000001u),
    L2              (0b01000010u),
    L3              (0b01000100u),
    R1              (0b10000001u),
    R2              (0b10000010u),
    R3              (0b10000100u),
    /** Non-swappable Confirm Button */
    StaticConfirm   (0b01001000u),
    /** Non-swappable Cancel Button */
    StaticCancel    (0b10001000u),
    ;

    companion object {
        /** Let the Asian and Western Console users to have their preference in here. they have their preference and culture
         *
         * And also... Who maps Triangle as Back button? isn't that adds more complexity than just swapping Circle and Cross? */
        var spotMarkedByX = false
        /** Standard Confirm Button */
        val Confirm get() = spotMarkedByX.select(Cross, Circle)
        /** Standard Cancel Button */
        val Cancel get() = spotMarkedByX.select(Circle, Cross)

        fun isCancel(k: PadKey) = k == Cancel || k == StaticCancel
        fun isConfirm(k:PadKey) = k == Confirm || k == StaticConfirm
    }
}