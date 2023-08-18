package com.quyt.mqttchat.domain.model

class Media(
    var localUri: String = "",
    var url: String = "",
    var type: Int = 0,
)

enum class MediaType(val value: Int) {
    IMAGE(0), VIDEO(1),
}