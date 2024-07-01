package id.psw.vshlauncher.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import id.psw.vshlauncher.Logger
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar

class XmbExportLog: Activity() {
    companion object
    {
        const val RQ_PATH_ASKING = 10241
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivityForResult(Logger.createExportIntent(), RQ_PATH_ASKING)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == RQ_PATH_ASKING)
        {
            if(resultCode == RESULT_OK)
            {
                data?.data?.also {
                    Logger.exportLog(this, it)
                }
            }

            finish()
        }
    }
}