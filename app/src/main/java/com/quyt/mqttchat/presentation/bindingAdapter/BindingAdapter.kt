package com.quyt.mqttchat.presentation.bindingAdapter

import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.quyt.mqttchat.utils.DateUtils
import java.util.concurrent.TimeUnit

@BindingAdapter("imageUrl")
fun loadImage(view: AppCompatImageView, url: String?) {
    if (url != null) {
        Glide.with(view.context).load(url).into(view)
    }
}

@BindingAdapter("setTime")
fun setTimeStringToTextView(view: AppCompatTextView, time: String?) {
    if (!time.isNullOrEmpty()) {
        view.text = DateUtils.formatTime(time, "HH:mm")
    }
}

@BindingAdapter("setAvatar")
fun setAvatar(view: ImageView, url: String?) {
    if (url != null) {
        Glide.with(view.context).load(url).into(view)
    }
}

@BindingAdapter("setTimeAgo")
fun setTimeAgo(textView : TextView, timestamp: Long) {
    val currentTime = DateUtils.currentTimestamp()
    val timeDiff = currentTime - timestamp

    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff)
    val hours = TimeUnit.MILLISECONDS.toHours(timeDiff)
    val days = TimeUnit.MILLISECONDS.toDays(timeDiff)

    textView.text = when {
        minutes < 1 -> "Vừa xong"
        minutes < 60 -> "$minutes phút trước"
        hours < 24 -> "$hours giờ trước"
        days > 100 -> "Loading..."
        else -> "$days ngày trước"
    }
}

