package com.quyt.mqttchat.domain.model

import com.google.gson.annotations.SerializedName

//data class Message(
//    var id: Int,
//    var conversationId : Int,
//    var sender: User,
//    var content : String,
//    var sendTime : String,
//)

class Message {
    @SerializedName("_id")
    var id: String? = null
    var conversation: String? = null
    var sender: User? = null
    var content: String? = null
    var createdAt: String? = null
    var updatedAt: String? = null
    var sendTime: Long = 0

    var isTyping: Boolean = false
    var state: Int = MessageState.SENT.value
}

enum class MessageState(val value: Int) {
    SENDING(0),
    SENT(1),
    SEEN(2),
    FAILED(3),
}
