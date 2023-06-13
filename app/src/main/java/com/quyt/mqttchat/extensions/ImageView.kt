package com.quyt.mqttchat.extensions

import android.widget.ImageView
import com.bumptech.glide.Glide


fun ImageView.setAvatar(url: String?) {
    Glide.with(this).load(url).into(this)
}