package com.quyt.mqttchat.data.datasource.local

import com.quyt.mqttchat.domain.model.Message

interface MessageLocalDataSource {

    suspend fun insertMessage(message: List<Message>)

    suspend fun getMessage(conversationId: String): List<Message>

    suspend fun getMessageByPage(conversationId: String, page: Int): List<Message>

    suspend fun getLatestMessage(conversationId: String): Message?

    suspend fun clearMessage(conversationId: String)

    suspend fun updateMessageState(messageIds: List<String>, newState: Int)
}
