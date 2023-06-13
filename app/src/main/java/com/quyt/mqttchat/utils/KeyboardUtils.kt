package com.quyt.mqttchat.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService


object KeyboardUtils {
    fun showKeyboard(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

//    fun hideKeyboard(context: Activity) {
//        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
//        imm!!.hideSoftInputFromWindow(context.getWindowToken(), 0)
//    }
}
