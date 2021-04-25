package id.psw.vshlauncher.icontypes

import id.psw.vshlauncher.move
import id.psw.vshlauncher.removeIfTrue


fun XMBIcon.getContent(index:Int) : XMBIcon = content[index]
fun XMBIcon.getContent():XMBIcon = content[contentIndex]
fun XMBIcon.getContent(vararg indices : Int) : XMBIcon{
    var retval = this
    for(i in indices){
        if(retval.hasContent){
            retval = retval.getContent(i)
        }
    }
    return retval
}
fun XMBIcon.addContent(item:XMBIcon) = content.add(item)
fun XMBIcon.delContent(item:XMBIcon) = content.add(item)
fun XMBIcon.delContent(ID:String) = content.removeIfTrue { it.id.equals( ID, true) }
fun XMBIcon.findContent(ID:String) : XMBIcon? = content.find { it.id.equals( ID, true) }
fun XMBIcon.moveContent(from:Int, to:Int) = content.move(from, to)
fun XMBIcon.forEachContentIndexed (func : (Int, XMBIcon) -> Unit) = content.forEachIndexed(func)

fun XMBIcon.getMenu(index:Int): XMBMenuEntry = menu[index]
fun XMBIcon.getMenu():XMBMenuEntry = menu[menuIndex]
fun XMBIcon.addMenu(menuEntry:XMBMenuEntry) = menu.add(menuEntry)
fun XMBIcon.delMenu(menuEntry:XMBMenuEntry) = menu.remove(menuEntry)
fun XMBIcon.delMenu(ID:String)  = menu.removeIfTrue { it.id.equals(ID, true) }
fun XMBIcon.findMenu(ID:String) : XMBMenuEntry? = menu.find { it.id.equals( ID, true) }
fun XMBIcon.findMenu(menuIndex:Int) : XMBMenuEntry? = menu.find { it.id.equals( "menu_${id}_${menuIndex}", true) }
fun XMBIcon.moveMenu(from:Int, to:Int) = menu.move(from, to)
fun XMBIcon.createMenu() : XMBIcon.MenuEntryBuilder = XMBIcon.MenuEntryBuilder(this, "menu_$id")
fun XMBIcon.forEachMenuIndexed (func : (Int, XMBMenuEntry) -> Unit) = menu.forEachIndexed(func)