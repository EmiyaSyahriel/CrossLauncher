package id.psw.vshlauncher.views.nativedlg

import android.app.AlertDialog
import android.text.InputType
import android.widget.EditText
import id.psw.vshlauncher.VSH

class NativeEditTextDialog(val vsh: VSH) {
    private val internalDlgBld = AlertDialog.Builder(vsh)

    private var onFinishCallback : (String) -> Unit = { }
    private var onCancelCallback : () -> Unit = { }
    private val editText = EditText(vsh)

    init {
        internalDlgBld
            .setNegativeButton(android.R.string.cancel){
                _,_ -> onCancelCallback()
            }
            .setPositiveButton(android.R.string.ok) { a, b ->
                onFinishCallback(editText.text.toString())
            }
            .setView(editText)
    }

    fun setValue(value:String) : NativeEditTextDialog{
        editText.setText(value)
        return this
    }

    fun setFilter(type:Int) : NativeEditTextDialog{
        editText.inputType = type
        return this
    }

    fun setTitle(title:String) : NativeEditTextDialog {
        internalDlgBld.setTitle(title)
        return this
    }

    fun setOnFinish(callback: (String) -> Unit) : NativeEditTextDialog{
        onFinishCallback = callback
        return this
    }

    fun setOnCancel(callback: () -> Unit) : NativeEditTextDialog{
        onCancelCallback = callback
        return this
    }

    fun show() : AlertDialog {
        return internalDlgBld.show()
    }
}