package com.quyt.mqttchat.domain.model

class Event {
    var type: Int? = 0
    var message: Message? = null
}

enum class EventType(val value: Int) {
    NEW_MESSAGE(0),
    TYPING(1),
}