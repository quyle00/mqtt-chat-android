package com.quyt.mqttchat.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.quyt.mqttchat.data.datasource.local.db.AppDatabase
import com.quyt.mqttchat.data.datasource.local.entity.MessageEntity
import com.quyt.mqttchat.data.datasource.local.entity.toMessage
import com.quyt.mqttchat.data.datasource.remote.MessageRemoteMediator
import com.quyt.mqttchat.data.datasource.remote.extension.getError
import com.quyt.mqttchat.data.datasource.remote.service.MessageService
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MessageRepositoryImpl(
    private val service: MessageService,
    private val appDatabase: AppDatabase
) : MessageRepository {
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

    @OptIn(ExperimentalPagingApi::class)
    override suspend fun getListMessage2(conversationId: String): Flow<PagingData<Message>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = false,
            ),
            remoteMediator = MessageRemoteMediator(service, appDatabase,conversationId),
        ) {
            appDatabase.messageDao().pagingSource(conversationId)
        }.flow.map {
            it.map { messageEntity ->
                messageEntity.toMessage()
            }
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

    override suspend fun insertMessage(message: MessageEntity) {
        appDatabase.messageDao().insertAll(listOf(message))
    }
}