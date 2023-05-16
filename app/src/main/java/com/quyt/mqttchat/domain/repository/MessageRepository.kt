package com.quyt.mqttchat.domain.repository

import androidx.paging.PagingData
import com.quyt.mqttchat.data.datasource.local.entity.MessageEntity
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun getListMessage(conversationId: String): Result<List<Message>>
    suspend fun getListMessage2(conversationId: String): Flow<PagingData<Message>>
    suspend fun createMessage(conversationId: String, message: Message): Result<Message>
    suspend fun updateSeenMessage(conversationId: String,messageIds : List<String>): Result<String>
    suspend fun insertMessage(message: MessageEntity)
}