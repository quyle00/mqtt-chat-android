package com.quyt.mqttchat.di.module

import com.quyt.mqttchat.domain.mapper.EventMapper
import com.quyt.mqttchat.domain.repository.AccessRepository
import com.quyt.mqttchat.domain.repository.ConversationRepository
import com.quyt.mqttchat.domain.repository.IMqttClient
import com.quyt.mqttchat.domain.repository.MessageRepository
import com.quyt.mqttchat.domain.repository.UserRepository
import com.quyt.mqttchat.domain.usecase.ListenConversationEventUseCase
import com.quyt.mqttchat.domain.usecase.SendConversationEventUseCase
import com.quyt.mqttchat.domain.usecase.access.LoginUseCase
import com.quyt.mqttchat.domain.usecase.contact.GetListContactUseCase
import com.quyt.mqttchat.domain.usecase.conversation.CreateConversationUseCase
import com.quyt.mqttchat.domain.usecase.conversation.GetConversationDetailUseCase
import com.quyt.mqttchat.domain.usecase.conversation.GetListConversationUseCase
import com.quyt.mqttchat.domain.usecase.message.CreateMessageUseCase
import com.quyt.mqttchat.domain.usecase.message.GetListMessageUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {

    @Provides
    @Singleton
    fun provideSendConversationEventUseCase(client: IMqttClient, mapper: EventMapper): SendConversationEventUseCase {
        return SendConversationEventUseCase(client, mapper)
    }

    @Provides
    @Singleton
    fun provideListenConversationEventUseCase(client: IMqttClient, mapper: EventMapper): ListenConversationEventUseCase {
        return ListenConversationEventUseCase(client, mapper)
    }

    @Provides
    @Singleton
    fun provideLoginUseCase(repository: AccessRepository): LoginUseCase {
        return LoginUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetListContactUseCase(repository: UserRepository): GetListContactUseCase {
        return GetListContactUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideCreateConversationUseCase(repository: ConversationRepository): CreateConversationUseCase {
        return CreateConversationUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetListConversationUseCase(repository: ConversationRepository): GetListConversationUseCase {
        return GetListConversationUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetConversationDetailUseCase(repository: ConversationRepository): GetConversationDetailUseCase {
        return GetConversationDetailUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetListMessageUseCase(repository: MessageRepository): GetListMessageUseCase {
        return GetListMessageUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideCreateMessageUseCase(repository: MessageRepository): CreateMessageUseCase {
        return CreateMessageUseCase(repository)
    }

}