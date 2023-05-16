package com.quyt.mqttchat.data.datasource.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quyt.mqttchat.data.datasource.local.entity.MessageEntity
import com.quyt.mqttchat.domain.model.User

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<MessageEntity>)

    @Query("SELECT * FROM message WHERE conversation LIKE :conversationId")
    fun pagingSource(conversationId: String): PagingSource<Int, MessageEntity>

    @Query("DELETE FROM message")
    suspend fun clearAll()
}
