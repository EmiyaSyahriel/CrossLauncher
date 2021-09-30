package id.psw.vshlauncher.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.setPadding
import id.psw.vshlauncher.R
import id.psw.vshlauncher.floorToInt
import id.psw.vshlauncher.toSize
import id.psw.vshlauncher.views.VshDialogLayout
import java.io.File

class AppInfoActivity : AppCompatActivity() {

    companion object{
        const val ARG_INFO_PKG_NAME_KEY = "vshinfo_pkg_name";
    }

    lateinit var table : TableLayout
    private var sd : Float = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dialogLayout = VshDialogLayout(this)
        dialogLayout.addButton(getString(R.string.common_back)) { finish() }
        dialogLayout.setTitle(getString(R.string.view_app_info))
        setContentView(dialogLayout)

        val scroller = ScrollView(this)
        dialogLayout.addView(scroller)

        table = TableLayout(this)
        scroller.addView(table)

        val srcPkg = intent.getStringExtra(ARG_INFO_PKG_NAME_KEY)
        if(srcPkg != null){
            try{
                dialogLayout.addButton(R.string.app_info_system_activity){ showDetailFromSystem(srcPkg)  }
                val appinfo = packageManager.getApplicationInfo(srcPkg, 0)
                val pkginfo = packageManager.getPackageInfo(srcPkg, 0)
                val appname = packageManager.getApplicationLabel(appinfo)

                val appPkg = File(appinfo.sourceDir)

                addKvpImage(appinfo.loadIcon(packageManager))
                addKvp("Label", appname.toString())
                addKvp("Package Name", srcPkg)
                addKvp("Data Path", appinfo.dataDir)

                val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pkginfo.longVersionCode
                }else{
                    pkginfo.versionCode.toLong()
                }

                addKvp("Version", pkginfo.versionName)
                addKvp("Package Size", appPkg.length().toSize())
            }catch(exc:Exception){
                addKvp("ERROR",exc.message ?: "Unknown Error")
            }
        }else{
            addKvp("ERROR","No package name is given as parameter.")
        }

        table.gravity = Gravity.CENTER_VERTICAL or Gravity.START

        table.setPadding((30 * sd).floorToInt())

        dialogLayout.requestLayout()
        sd =resources.displayMetrics.scaledDensity
    }

    private fun showDetailFromSystem(srcPkg: String) {
        try{
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$srcPkg")
            startActivity(intent)
        }catch(exc:Exception){
            exc.printStackTrace()
        }
    }


    private fun TextView.applyDefaultStyle(align: Int) : TextView{
        setTextColor(Color.WHITE)
        textSize = 18 * sd
        textAlignment = align
        gravity = Gravity.FILL_HORIZONTAL or Gravity.CENTER_VERTICAL

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

        val boldTf = Typeface.create(keytxt.typeface, Typeface.BOLD)
        keytxt.typeface = boldTf

        val rowLParam = ViewGroup.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,  TableRow.LayoutParams.WRAP_CONTENT)
        row.layoutParams = rowLParam
        keytxt.width = (sd * 300).toInt();

        row.gravity = Gravity.FILL_HORIZONTAL
        row.addView(keytxt)
        row.addView(valtxt)
        table.addView(row)
    }
    private fun addKvpImage(value: Drawable){
        val row= TableRow(this)
        val valimg = ImageView(this).apply {
            maxWidth = (150 * sd).toInt()
            maxHeight = (150 * sd).toInt()
        }
        valimg.setImageDrawable(value)

        val rowLParam = ViewGroup.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,  TableRow.LayoutParams.WRAP_CONTENT)
        row.layoutParams = rowLParam

        row.gravity = Gravity.FILL_HORIZONTAL or Gravity.CENTER
        row.addView(valimg)
        table.addView(row)
    }

}