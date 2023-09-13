package id.psw.vshlauncher.views

import android.app.Dialog
import android.graphics.*
import android.text.TextPaint
import android.view.MotionEvent
import androidx.core.graphics.contains
import androidx.core.graphics.withClip
import androidx.core.graphics.withTranslation
import id.psw.vshlauncher.*
import id.psw.vshlauncher.submodules.GamepadSubmodule
import id.psw.vshlauncher.submodules.PadKey
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.typography.MultifontSpan
import id.psw.vshlauncher.typography.drawText
import id.psw.vshlauncher.typography.toButtonSpan
import java.util.*
