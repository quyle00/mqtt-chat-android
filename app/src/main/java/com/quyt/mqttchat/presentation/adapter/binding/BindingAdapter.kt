package com.quyt.mqttchat.presentation.adapter.binding

import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.quyt.mqttchat.R
import com.quyt.mqttchat.domain.model.MessageState
import com.quyt.mqttchat.utils.DateUtils

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