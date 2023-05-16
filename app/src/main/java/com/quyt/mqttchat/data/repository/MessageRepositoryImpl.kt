package com.quyt.mqttchat.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.quyt.mqttchat.data.datasource.MessagePagingSource
import com.quyt.mqttchat.data.datasource.remote.extension.getError
import com.quyt.mqttchat.data.datasource.remote.service.MessageService
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
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

    override suspend fun getListMessage2(): Flow<PagingData<Message>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 1
            ),
            pagingSourceFactory = { MessagePagingSource(service) }
        ).flow
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

    override suspend fun updateSeenMessage(conversationId: String, messageIds: List<String>): Result<String> {
        return try {
            val res = service.updateSeenMessage(conversationId, messageIds)
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