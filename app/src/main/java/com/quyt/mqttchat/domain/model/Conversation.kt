package com.quyt.mqttchat.domain.model

/*
data class Conversation(
    var id: Int,
    var participants: List<User>,
    var lastMessage: Message,
)
*/

class Conversation {
    var id: Int = 0
    var name: String? = null
    var participants: List<User> = listOf()
    var lastMessage: Message? = null
}

