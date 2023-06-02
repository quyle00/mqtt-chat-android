package com.quyt.mqttchat.domain.model

import com.google.gson.annotations.SerializedName

/*
data class Conversation(
    var id: Int,
    var participants: List<User>,
    var lastMessage: Message,
)
*/

class Conversation {
    @SerializedName("_id")
    var id: String? = null
    var name: String? = null
    var participants: List<User>? = null
    var lastMessage: Message? = null
}
