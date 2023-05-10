package com.quyt.mqttchat.domain.repository

import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.Result

interface MessageRepository {
    suspend fun getListMessage(conversationId: String): Result<List<Message>>
    suspend fun createMessage(conversationId: String, message: Message): Result<Message>
}