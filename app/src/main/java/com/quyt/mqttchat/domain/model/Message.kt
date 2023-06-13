package com.quyt.mqttchat.domain.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.quyt.mqttchat.data.datasource.local.entity.MessageEntity

// data class Message(
//    var id: Int,
//    var conversationId : Int,
//    var sender: User,
//    var content : String,
//    var sendTime : String,
// )

data class Message(
    @SerializedName("_id")
    var id: String,
    var conversation: String?,
    var sender: User?,
    var content: String?,
    var createdAt: String?,
    var updatedAt: String?,
    var sendTime: Long,
    var isMine: Boolean,
    var isTyping: Boolean,
    var state: Int = MessageState.SENT.value,
    var type: Int = MessageContentType.TEXT.value,
    var images: List<String>?,
    var reply: Message?,
    var edited : Boolean
) {
    constructor() : this(
        id = "",
        conversation = "",
        sender = User(),
        content = "",
        createdAt = "",
        updatedAt = "",
        sendTime = 0,
        isMine = false,
        isTyping = false,
        state = MessageState.SENT.value,
        type = MessageContentType.TEXT.value,
        images = listOf(),
        reply = null,
        edited = false
    )
}

fun Message.toEntity() = MessageEntity(
    id = id,
    conversation = conversation,
    sender = Gson().toJson(sender),
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    sendTime = sendTime,
    isMine = isMine,
    isTyping = isTyping,
    state = state,
    type = type,
    images = Gson().toJson(images),
    reply = Gson().toJson(reply),
    edited = edited
)

enum class MessageState(val value: Int) {
    SENDING(0),
    SENT(1),
    SEEN(2),
    FAILED(3)
}

enum class MessageContentType(val value: Int) {
    TEXT(0),
    IMAGE(1)
}
