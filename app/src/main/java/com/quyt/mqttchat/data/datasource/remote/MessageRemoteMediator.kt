package com.quyt.mqttchat.data.datasource.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.quyt.mqttchat.data.datasource.local.db.AppDatabase
import com.quyt.mqttchat.data.datasource.local.entity.MessageEntity
import com.quyt.mqttchat.data.datasource.remote.service.MessageService
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.toEntity
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class MessageRemoteMediator(
    private val service: MessageService,
    private val appDatabase: AppDatabase,
    private val conversationId : String
) : RemoteMediator<Int, MessageEntity>() {

    private var nextPageKey: Int = 1
    override suspend fun load(loadType: LoadType, state: PagingState<Int, MessageEntity>): MediatorResult {
        return try {
            val messageDao = appDatabase.messageDao()
            val loadKey = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> nextPageKey
            }

            val response = service.getListMessage2(loadKey, state.config.pageSize)
            nextPageKey = response.body()?.data?.pagination?.next ?: 1

            appDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    messageDao.clearAll()
                }
                messageDao.insertAll(response.body()?.data?.data?.map { it.toEntity() } ?: emptyList())
            }

            MediatorResult.Success(
                endOfPaginationReached = response.body()?.data?.pagination?.hasNextPage == false
            )
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }


}