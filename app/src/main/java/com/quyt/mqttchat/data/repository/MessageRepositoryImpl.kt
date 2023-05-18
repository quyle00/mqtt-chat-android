package com.quyt.mqttchat.data.repository

import com.quyt.mqttchat.data.datasource.local.MessageLocalDataSource
import com.quyt.mqttchat.data.datasource.remote.extension.getError
import com.quyt.mqttchat.data.datasource.remote.service.ConversationService
import com.quyt.mqttchat.data.datasource.remote.service.MessageService
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.repository.MessageRepository

class MessageRepositoryImpl(
    private val messageService: MessageService,
    private val conversationService: ConversationService,
    private val messageLocalDatasource: MessageLocalDataSource
) : MessageRepository {
    override suspend fun getListMessage(conversationId: String, page: Int): Result<List<Message>> {
        return try {
            if (page == 1) {
                val conversationRes = conversationService.getConversationLastMessage(conversationId)
                if (conversationRes.isSuccessful) {
                    val remoteLastMessage = conversationRes.body()?.data
                    if (remoteLastMessage != null) {
                        val localLatestMessage = messageLocalDatasource.getLatestMessage(conversationId)
                        if (localLatestMessage.id != remoteLastMessage.id) {
                            messageLocalDatasource.clearMessage(conversationId)
                        }
                    }
                } else {
                    Result.Error(conversationRes.getError())
                }
            }

            val localMessage = messageLocalDatasource.getMessageByPage(conversationId, page)
            if (localMessage.isNotEmpty()) {
                Result.Success(localMessage)
            } else {
                val res = messageService.getListMessage(conversationId, page)
                if (res.isSuccessful) {
                    if (res.body()?.data?.data?.isNotEmpty() == true) {
                        messageLocalDatasource.insertMessage(res.body()?.data?.data ?: listOf())
                    }
                    Result.Success(res.body()?.data?.data ?: listOf())
                } else {
                    Result.Error(res.getError())
                }
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun createMessage(conversationId: String, message: Message): Result<Message> {
        return try {
            val res = messageService.createMessage(conversationId, message)
            if (res.isSuccessful) {
                Result.Success(res.body()?.data!!)
            } else {
                Result.Error(res.getError())
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun insertMessage(messageList: List<Message>) {
        messageLocalDatasource.insertMessage(messageList)
    }

    override suspend fun updateSeenMessage(conversationId: String, messageIds: List<String>): Result<String> {
        return try {
            val res = messageService.updateSeenMessage(conversationId, messageIds)
            if (res.isSuccessful) {
                Result.Success(res.body()?.data!!)
            } else {
                Result.Error(res.getError())
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}