package com.quyt.mqttchat.extensions

import android.widget.TextView
import com.quyt.mqttchat.utils.DateUtils

fun TextView.setShortTime(time: String) {
    if (time.isNotEmpty()) {
        this.text = DateUtils.formatTime(time, "HH:mm")
    }
}