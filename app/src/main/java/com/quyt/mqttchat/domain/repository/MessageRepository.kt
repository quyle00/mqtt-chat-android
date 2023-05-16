package com.quyt.mqttchat.domain.repository

import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.Result

interface MessageRepository {
    suspend fun getListMessage(conversationId: String, page: Int): Result<List<Message>>
    suspend fun createMessage(conversationId: String, message: Message): Result<Message>
    suspend fun insertMessage(messageList: List<Message>)
    suspend fun updateSeenMessage(conversationId: String, messageIds: List<String>): Result<String>
}