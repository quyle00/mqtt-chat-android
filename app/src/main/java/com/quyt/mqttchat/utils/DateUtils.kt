package com.quyt.mqttchat.utils

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

    fun formatTime(dateTimeString: String,format: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val dateTime = LocalDateTime.parse(dateTimeString, formatter)

        val outputFormatter = DateTimeFormatter.ofPattern(format)
        return dateTime.format(outputFormatter)
    }

    fun compareInMinutes(previousTime: String?, currentTime: String?): Int {
        if (previousTime == null) return 0
        if (currentTime == null) return Int.MAX_VALUE
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val previousDateTime = format.parse(previousTime) ?: return 0
        val currentDateTime = format.parse(currentTime) ?: return Int.MAX_VALUE
        val timeDifference = currentDateTime.time - previousDateTime.time
        val minutesDifference = TimeUnit.MILLISECONDS.toMinutes(timeDifference)

        return minutesDifference.toInt()
    }
}