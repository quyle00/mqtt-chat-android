package com.quyt.mqttchat.data.repository

import com.quyt.mqttchat.data.datasource.remote.extension.getError
import com.quyt.mqttchat.data.datasource.remote.service.MessageService
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.repository.MessageRepository
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(private val service: MessageService) : MessageRepository {
    override suspend fun getListMessage(conversationId: String): Result<List<Message>> {
        return try {
            val res = service.getListMessage(conversationId)
            if (res.isSuccessful) {
                Result.Success(res.body()?.data!!)
            } else {
                Result.Error(res.getError())
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun createMessage(conversationId: String, message: Message): Result<Message> {
        return try {
            val res = service.createMessage(conversationId, message)
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