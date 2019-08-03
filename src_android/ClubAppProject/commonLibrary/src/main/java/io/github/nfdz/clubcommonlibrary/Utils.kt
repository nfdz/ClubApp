package io.github.nfdz.clubcommonlibrary

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.annotation.WorkerThread
import com.google.android.material.snackbar.Snackbar
import androidx.core.text.HtmlCompat
import android.text.Spanned
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

//region View/ViewGroup utils

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun View.showSnackbar(text: CharSequence, duration: Int = Snackbar.LENGTH_LONG): Snackbar {
    val result = Snackbar.make(this, processSnackbarText(text), duration)
    processSnackbarLayout(result.view)
    result.show()
    return result
}

private fun processSnackbarLayout(snackbarLayout: View) {
    val textView: TextView? = snackbarLayout.findViewById(com.google.android.material.R.id.snackbar_text)
    textView?.setSingleLine(false)
}

private fun processSnackbarText(text: CharSequence): Spanned {
    return HtmlCompat.fromHtml("<font color=\"#FAFAFA\">$text</font>", HtmlCompat.FROM_HTML_OPTION_USE_CSS_COLORS)
}

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    this.requestFocus()
    imm?.showSoftInput(this, 0)
}

fun View.hideKeyboard() = try {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
} catch (ignored: RuntimeException) {
    false
}

fun View.fadeIn(durationInMillis: Long = 500L) {
    val fadeIn = AlphaAnimation(0f, 1f)
    fadeIn.duration = durationInMillis
    fadeIn.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {}
        override fun onAnimationEnd(animation: Animation?) {}
        override fun onAnimationStart(animation: Animation?) {
            visibility = View.VISIBLE
        }
    })
    startAnimation(fadeIn)
}

fun View.fadeOut(durationInMillis: Long = 500L) {
    val fadeOut = AlphaAnimation(1f, 0f)
    fadeOut.duration = durationInMillis
    fadeOut.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {}
        override fun onAnimationEnd(animation: Animation?) {
            visibility = View.INVISIBLE
        }
        override fun onAnimationStart(animation: Animation?) {
            visibility = View.VISIBLE
        }
    })
    startAnimation(fadeOut)
}

//endregion

//region Context utils

fun Context?.toast(@StringRes textId: Int, duration: Int = Toast.LENGTH_LONG) = this?.let { Toast.makeText(it, textId, duration).show() }
fun Context?.toast(text: String, duration: Int = Toast.LENGTH_LONG) = this?.let { Toast.makeText(it, text, duration).show() }

fun Context.getStringFromPreferences(@StringRes key: Int, @StringRes default: Int): String {
    val defaultString = getString(default)
    val result: String? = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(key), defaultString)
    return result ?: defaultString
}

@WorkerThread
fun Context.setStringInPreferences(@StringRes key: Int, value: String) {
    PreferenceManager.getDefaultSharedPreferences(this).edit().putString(getString(key), value).commit()
}

fun Context.getStringFromPreferences(key: String, default: String): String {
    val result: String? = PreferenceManager.getDefaultSharedPreferences(this).getString(key, default)
    return result ?: default
}

fun Context.setStringInPreferences(key: String, value: String) {
    PreferenceManager.getDefaultSharedPreferences(this).edit().putString(key, value).commit()
}

fun Context.getBooleanFromPreferences(@StringRes key: Int, @StringRes default: Int): Boolean {
    val defaultValue = "true" == getString(default)
    return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(key), defaultValue)
}

@WorkerThread
fun Context.setBooleanInPreferences(@StringRes key: Int, value: Boolean) {
    PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(getString(key), value).commit()
}

fun Context.getLongFromPreferences(@StringRes key: Int, default: Long): Long {
    return PreferenceManager.getDefaultSharedPreferences(this).getLong(getString(key), default)
}

@WorkerThread
fun Context.setLongInPreferences(@StringRes key: Int, value: Long) {
    PreferenceManager.getDefaultSharedPreferences(this).edit().putLong(getString(key), value).commit()
}

fun Intent.getStringExtra(name: String, defaultValue: String) = getStringExtra(name) ?: defaultValue

//endregion

//region Threading utils

class doAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
    init {
        execute()
    }
    override fun doInBackground(vararg params: Void?): Void? {
        handler()
        return null
    }
}

fun doMainThread(handler: () -> Unit) {
    Handler(Looper.getMainLooper()).post(handler)
}

fun postDelayed(delayMillis: Long, handler: () -> Unit) {
    Handler().postDelayed(handler, delayMillis)
}

//endregion

//region Time utils

fun Long.toDate() = Date(this)

fun Long.toCalendar() = Calendar.getInstance().also { it.timeInMillis = this }

fun Date.printHourMinDayMonthYear(): String {
    val simpleFormatter = SimpleDateFormat("HH:mm dd/MM/yy", Locale.getDefault())
    return simpleFormatter.format(this)
}

fun Date.printHourMin(): String {
    val simpleFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return simpleFormatter.format(this)
}

fun Date.printDayMonthYear(): String {
    val simpleFormatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    return simpleFormatter.format(this)
}

fun Date.toCalendar() = Calendar.getInstance().also { it.time = this }

fun Calendar.setFirstHourOfDay(): Calendar {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    return this
}
fun Calendar.setLastHourOfDay(): Calendar {
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    return this
}

//endregion

//region Others utils

fun EditText.onNextOrEnterListener(callback: () -> Unit) {
    setOnEditorActionListener { _, actionId, event ->
        if (event == null) {
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    // Capture soft enters in a singleLine EditText that is the last EditText.
                    callback()
                    true
                }
                EditorInfo.IME_ACTION_NEXT -> {
                    // Capture soft enters in other singleLine EditTexts
                    callback()
                    true
                }
                else -> false  // Let system handle all other null KeyEvents
            }
        } else if (actionId == EditorInfo.IME_NULL) {
            // Capture most soft enters in multi-line EditTexts and all hard enters.
            // They supply a zero actionId and a valid KeyEvent rather than
            // a non-zero actionId and a null event like the previous cases.
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                // We capture the event when key is first pressed.
                true
            } else {
                callback()
                true   // We consume the event when the key is released.
            }
        } else {
            // We let the system handle it when the listener
            // is triggered by something that wasn't an enter.
            false
        }
    }
}

fun Int.bound(fromInclusive: Int, toInclusive: Int): Int {
    return Math.min(Math.max(this, fromInclusive), toInclusive)
}

//endregion