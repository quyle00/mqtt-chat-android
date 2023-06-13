package com.quyt.mqttchat.data.datasource.local

import com.quyt.mqttchat.data.datasource.local.db.AppDatabase
import com.quyt.mqttchat.data.datasource.local.entity.toMessage
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.toEntity

class MessageLocalDataSourceImpl(private val appDatabase: AppDatabase) : MessageLocalDataSource {

    override suspend fun insertMessage(message: List<Message>) {
        appDatabase.messageDao().insertAll(message.map { it.toEntity() })
    }

    override suspend fun getMessage(conversationId: String): List<Message> {
        val localMessage = appDatabase.messageDao().getMessage(conversationId)
        return localMessage.map { it.toMessage() }
    }

    override suspend fun getMessageByPage(conversationId: String, page: Int): List<Message> {
        val offset = (page - 1) * 20
        val localMessage = appDatabase.messageDao().getMessageByPage(conversationId, offset, 20)
        return localMessage.map { it.toMessage() }
    }

    override suspend fun getLatestMessage(conversationId: String): Message? {
        val localMessage = appDatabase.messageDao().getLatestMessage(conversationId)
        return localMessage.let {
            it?.toMessage()
        }
    }

    override suspend fun clearMessage(conversationId: String) {
        appDatabase.messageDao().clearAll(conversationId)
    }

    override suspend fun updateMessageState(messageIds: List<String>, newState: Int) {
        appDatabase.messageDao().updateMessagesState(messageIds, newState)
    }

    override suspend fun updateMessage(message: Message) {
        appDatabase.messageDao().updateMessage(message.toEntity())
    }

    override suspend fun deleteMessage(message: Message) {
        appDatabase.messageDao().deleteMessage(message.toEntity())
    }
}
