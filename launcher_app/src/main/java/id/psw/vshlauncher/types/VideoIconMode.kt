package id.psw.vshlauncher.types

enum class VideoIconMode {
    Disabled,
    AllTime,
    SelectedOnly;

    companion object {
        fun fromInt(i:Int) : VideoIconMode{
            return when(i){
                1 -> AllTime
                2 -> SelectedOnly
                else -> Disabled
            }
        }

        fun toInt(e:VideoIconMode) : Int {
            return when(e){
                AllTime -> 1
                SelectedOnly -> 2
                else -> 0
            }
        }
    }
}