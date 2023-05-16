package com.quyt.mqttchat.data.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.quyt.mqttchat.data.datasource.remote.service.MessageService
import com.quyt.mqttchat.domain.model.Message

class MessagePagingSource(private val service: MessageService) : PagingSource<Int, Message>() {
    override fun getRefreshKey(state: PagingState<Int, Message>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Message> {
        return try {
            val nextPageNumber = params.key ?: 1
            val response = service.getListMessage2(nextPageNumber)
            val pagingData = response.body()?.data
            LoadResult.Page(
                data = pagingData?.data ?: emptyList(),
                prevKey = pagingData?.pagination?.prev,
                nextKey = pagingData?.pagination?.next
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }


}