package com.quyt.mqttchat.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.MessageContentType
import com.quyt.mqttchat.domain.model.MessageState
import com.quyt.mqttchat.domain.model.User

@Entity(tableName = "Message")
data class MessageEntity(
    @PrimaryKey
    @SerializedName("_id")
    var id: String,
    var conversation: String?,
    var sender: String?,
    var content: String?,
    var createdAt: String?,
    var updatedAt: String?,
    var sendTime: Long = 0,
    var isMine: Boolean?,
    var isTyping: Boolean,
    var state: Int?,
    var type: Int= MessageContentType.TEXT.value,
    var images: String?,
)

fun MessageEntity.toMessage() = Message(
    id = id,
    conversation = conversation,
    sender = Gson().fromJson(sender, User::class.java),
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    sendTime = sendTime,
    isMine = isMine ?: false,
    isTyping = isTyping,
    state = state ?: MessageState.SENT.value,
    type = type,
    images = Gson().fromJson(images, Array<String>::class.java).toList()
)
