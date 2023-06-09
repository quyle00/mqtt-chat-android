package com.quyt.mqttchat.di.module

import com.quyt.mqttchat.data.datasource.local.MessageLocalDataSource
import com.quyt.mqttchat.data.datasource.remote.service.AccessService
import com.quyt.mqttchat.data.datasource.remote.service.ContactService
import com.quyt.mqttchat.data.datasource.remote.service.ConversationService
import com.quyt.mqttchat.data.datasource.remote.service.MessageService
import com.quyt.mqttchat.data.repository.AccessRepositoryImpl
import com.quyt.mqttchat.data.repository.ConversationRepositoryImpl
import com.quyt.mqttchat.data.repository.MessageRepositoryImpl
import com.quyt.mqttchat.data.repository.UserRepositoryImpl
import com.quyt.mqttchat.domain.repository.AccessRepository
import com.quyt.mqttchat.domain.repository.ConversationRepository
import com.quyt.mqttchat.domain.repository.MessageRepository
import com.quyt.mqttchat.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideAccessRepository(
        service: AccessService
    ): AccessRepository {
        return AccessRepositoryImpl(service)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        service: ContactService
    ): UserRepository {
        return UserRepositoryImpl(service)
    }

    @Provides
    @Singleton
    fun provideConversationRepository(
        service: ConversationService
    ): ConversationRepository {
        return ConversationRepositoryImpl(service)
    }

    @Provides
    @Singleton
    fun provideMessageRepository(
        service: MessageService,
        conversationService: ConversationService,
        messageLocalDataSource: MessageLocalDataSource
    ): MessageRepository {
        return MessageRepositoryImpl(service,conversationService,messageLocalDataSource)
    }
}