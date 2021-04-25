package id.psw.vshlauncher.icontypes

open class XMBMenuEntry (val id:String){
    open var name:String = "Unnamed"
    open var onClick:Runnable = Runnable {  }
    open var selectable : Boolean = false
}
