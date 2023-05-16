package com.quyt.mqttchat.data.datasource.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.quyt.mqttchat.data.datasource.local.dao.MessageDao
import com.quyt.mqttchat.data.datasource.local.entity.MessageEntity

@Database(entities = [MessageEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}
