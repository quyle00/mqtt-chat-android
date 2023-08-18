package com.quyt.mqttchat.domain.model

import android.icu.text.ListFormatter.Width
import com.quyt.mqttchat.constant.Constant

class Media(
    var localUri: String = "",
    var url: String = "",
    var type: Int = 0,
    var width: Int = 0,
    var height: Int = 0,
) {
    fun isImage() = type == MediaType.IMAGE.value
    fun isVideo() = type == MediaType.VIDEO.value

    fun getThumbnailSize(): Pair<Int, Int> {
        return if (width > height) {
            Pair(100, 100 * height / width)
        } else {
            Pair(100 * width / height, 100)
        }
    }

    fun getSizeOnScreen(screenWidth: Int, screenHeight : Int): Pair<Int,Int>{
        val result = Pair(width,height)
        if (width > screenWidth){
            val ratio = width.toFloat() / screenWidth.toFloat()
            return Pair(screenWidth, (height / ratio).toInt())
        }
        if (height > screenHeight){
            val ratio = height.toFloat() / screenHeight.toFloat()
            return Pair((width / ratio).toInt(), screenHeight)
        }
        return result
    }

    fun getFullUrl(): String {
        return if (url.isEmpty()) localUri else "${Constant.API_HOST}$url"
    }
}

enum class MediaType(val value: Int) {
    IMAGE(0), VIDEO(1),
}