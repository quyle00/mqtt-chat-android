package com.quyt.mqttchat.domain.model

class Event(
    var publisherId: String?,
    var type: Int?,
    var message: Message?,
    var inConversationScreen : Boolean = false
)

enum class EventType(val value: Int) {
    NEW_MESSAGE(0),
    TYPING(1),
    SEEN(2),
    IN_CONVERSATION_SCREEN(3),
}