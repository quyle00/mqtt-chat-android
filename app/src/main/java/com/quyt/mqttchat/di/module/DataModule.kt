package com.quyt.mqttchat.di.module

import android.content.Context
import com.quyt.mqttchat.data.datasource.local.MessageLocalDataSource
import com.quyt.mqttchat.data.datasource.local.MessageLocalDataSourceImpl
import com.quyt.mqttchat.data.datasource.local.db.AppDatabase
import com.quyt.mqttchat.domain.mapper.EventMapper
import com.quyt.mqttchat.domain.mapper.MessageMapper
import com.quyt.mqttchat.domain.mapper.UserMapper
import com.quyt.mqttchat.utils.network.NetworkChecker
import com.quyt.mqttchat.utils.network.NetworkCheckerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    @Provides
    @Singleton
    fun provideNetworkChecker(@ApplicationContext ctx: Context): NetworkChecker {
        return NetworkCheckerImpl(ctx)
    }

    @Provides
    @Singleton
    fun provideMessageMapper(): MessageMapper {
        return MessageMapper()
    }

    @Provides
    @Singleton
    fun provideEventMapper(): EventMapper {
        return EventMapper()
    }

    @Provides
    @Singleton
    fun provideUserMapper(): UserMapper {
        return UserMapper()
    }

    @Provides
    @Singleton
    fun provideMessageLocalDataSource(appDatabase: AppDatabase): MessageLocalDataSource {
        return MessageLocalDataSourceImpl(appDatabase)
    }
}
