package id.psw.vshlauncher.activities

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import id.psw.vshlauncher.R
import id.psw.vshlauncher.views.VshDialogLayout

class AppInfoActivity : AppCompatActivity() {

    companion object{
        const val ARG_INFO_PKG_NAME_KEY = "vshinfo_pkg_name";
    }

    lateinit var table : TableLayout
    private var sd : Float = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dialogLayout = VshDialogLayout(this)
        dialogLayout.addButton(android.R.string.cancel) { finish() }
        dialogLayout.setTitle(getString(R.string.view_app_info))
        setContentView(dialogLayout)
        table = TableLayout(this)
        dialogLayout.addView(table)

        val srcPkg = intent.getStringExtra(ARG_INFO_PKG_NAME_KEY)
        if(srcPkg != null){
            val appinfo = packageManager.getApplicationInfo(srcPkg, 0)
            val appname = packageManager.getApplicationLabel(appinfo)
            addKvp("Label", appname.toString())
            addKvp("Package Name", srcPkg)
        }

        table.gravity = Gravity.CENTER_VERTICAL or Gravity.START

        dialogLayout.requestLayout()
        sd =resources.displayMetrics.scaledDensity
    }


    private fun TextView.applyDefaultStyle(align: Int) : TextView{
        setTextColor(Color.WHITE)
        textSize = 18 * sd
        textAlignment = align
        gravity = Gravity.FILL_HORIZONTAL

        val oldPar = TableRow.LayoutParams( TableRow.LayoutParams.WRAP_CONTENT,  TableRow.LayoutParams.WRAP_CONTENT)


        oldPar.height = TableRow.LayoutParams.WRAP_CONTENT
        layoutParams = oldPar

        return this
    }

    private fun addKvp(key:String, value:String){
        val row= TableRow(this)
        val keytxt = TextView(this).applyDefaultStyle(TextView.TEXT_ALIGNMENT_TEXT_END)
        val valtxt = TextView(this).applyDefaultStyle(TextView.TEXT_ALIGNMENT_TEXT_START)
        keytxt.text =key
        keytxt.setPadding(20,0,20,0)
        valtxt.text =value

        val rowLParam = ViewGroup.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,  TableRow.LayoutParams.WRAP_CONTENT)
        row.layoutParams = rowLParam
        keytxt.width = (sd * 300).toInt();

        row.gravity = Gravity.FILL_HORIZONTAL
        row.addView(keytxt)
        row.addView(valtxt)
        table.addView(row)
    }

}