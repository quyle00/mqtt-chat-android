package com.quyt.mqttchat.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quyt.mqttchat.data.datasource.local.entity.MessageEntity

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(message: List<MessageEntity>)

    @Query("SELECT * FROM message WHERE conversation LIKE :conversationId")
    fun getMessage(conversationId: String): List<MessageEntity>

    @Query("SELECT * FROM message WHERE conversation LIKE :conversationId ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    fun getMessageByPage(conversationId: String, offset: Int, limit: Int = 20): List<MessageEntity>

    @Query("SELECT * FROM message WHERE conversation LIKE :conversationId ORDER BY createdAt DESC LIMIT 1")
    fun getLatestMessage(conversationId: String): MessageEntity?

    @Query("DELETE FROM message WHERE conversation LIKE :conversationId")
    suspend fun clearAll(conversationId: String)

    @Query("UPDATE message SET state = :newState WHERE id IN (:messageIds)")
    suspend fun updateMessagesState(messageIds: List<String>, newState: Int)
}
