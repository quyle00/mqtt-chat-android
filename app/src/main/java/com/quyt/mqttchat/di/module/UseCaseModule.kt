package com.quyt.mqttchat.di.module

import com.quyt.mqttchat.domain.mapper.EventMapper
import com.quyt.mqttchat.domain.repository.AccessRepository
import com.quyt.mqttchat.domain.repository.ConversationRepository
import com.quyt.mqttchat.domain.repository.IMqttClient
import com.quyt.mqttchat.domain.repository.MessageRepository
import com.quyt.mqttchat.domain.repository.SharedPreferences
import com.quyt.mqttchat.domain.repository.UserRepository
import com.quyt.mqttchat.domain.usecase.access.LoginUseCase
import com.quyt.mqttchat.domain.usecase.contact.GetListContactUseCase
import com.quyt.mqttchat.domain.usecase.conversation.CreateConversationUseCase
import com.quyt.mqttchat.domain.usecase.conversation.GetConversationDetailUseCase
import com.quyt.mqttchat.domain.usecase.conversation.GetListConversationUseCase
import com.quyt.mqttchat.domain.usecase.conversation.ListenConversationEventUseCase
import com.quyt.mqttchat.domain.usecase.message.ClearRetainMessageEventUseCase
import com.quyt.mqttchat.domain.usecase.message.CreateMessageUseCase
import com.quyt.mqttchat.domain.usecase.message.DeleteMessageUseCase
import com.quyt.mqttchat.domain.usecase.message.GetListMessageUseCase
import com.quyt.mqttchat.domain.usecase.message.InsertMessageUseCase
import com.quyt.mqttchat.domain.usecase.message.ListenMessageEventUseCase
import com.quyt.mqttchat.domain.usecase.message.SeenMessageUseCase
import com.quyt.mqttchat.domain.usecase.message.SendDeleteMessageEventUseCase
import com.quyt.mqttchat.domain.usecase.message.SendEditMessageEventUseCase
import com.quyt.mqttchat.domain.usecase.message.SendMarkReadEventUseCase
import com.quyt.mqttchat.domain.usecase.message.SendNewMessageEventUseCase
import com.quyt.mqttchat.domain.usecase.message.SendTypingEventUseCase
import com.quyt.mqttchat.domain.usecase.message.UnsubscribeMessageEventUseCase
import com.quyt.mqttchat.domain.usecase.message.UpdateLocalMessageStateUseCase
import com.quyt.mqttchat.domain.usecase.message.UpdateMessageUseCase
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
    fun provideSendNewMessageEventUseCase(
        sharedPreferences: SharedPreferences,
        client: IMqttClient,
        mapper: EventMapper
    ): SendNewMessageEventUseCase {
        return SendNewMessageEventUseCase(sharedPreferences, client, mapper)
    }

    @Provides
    @Singleton
    fun provideSendMarkReadEventUseCase(
        sharedPreferences: SharedPreferences,
        client: IMqttClient,
        mapper: EventMapper
    ): SendMarkReadEventUseCase {
        return SendMarkReadEventUseCase(sharedPreferences, client, mapper)
    }

    @Provides
    @Singleton
    fun provideSendTypingEventUseCase(
        sharedPreferences: SharedPreferences,
        client: IMqttClient,
        mapper: EventMapper
    ): SendTypingEventUseCase {
        return SendTypingEventUseCase(sharedPreferences, client, mapper)
    }

    @Provides
    @Singleton
    fun provideListenMessageEventUseCase(
        client: IMqttClient,
        mapper: EventMapper
    ): ListenMessageEventUseCase {
        return ListenMessageEventUseCase(client, mapper)
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

    @Provides
    @Singleton
    fun provideInsertMessageUseCase(repository: MessageRepository): InsertMessageUseCase {
        return InsertMessageUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSeenMessageUseCase(repository: MessageRepository): SeenMessageUseCase {
        return SeenMessageUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateLocalMessageStateUseCase(repository: MessageRepository): UpdateLocalMessageStateUseCase {
        return UpdateLocalMessageStateUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateMessageUseCase(repository: MessageRepository): UpdateMessageUseCase {
        return UpdateMessageUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteMessageUseCase(repository: MessageRepository): DeleteMessageUseCase {
        return DeleteMessageUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSendEditMessageEventUseCase(
        sharedPreferences: SharedPreferences,
        client: IMqttClient,
        mapper: EventMapper
    ): SendEditMessageEventUseCase {
        return SendEditMessageEventUseCase(sharedPreferences, client, mapper)
    }

    @Singleton
    @Provides
    fun provideSendDeleteMessageEventUseCase(
        sharedPreferences: SharedPreferences,
        client: IMqttClient,
        mapper: EventMapper
    ): SendDeleteMessageEventUseCase {
        return SendDeleteMessageEventUseCase(sharedPreferences, client, mapper)
    }

    @Singleton
    @Provides
    fun provideUnsubscribeMessageEventUseCase(
        client: IMqttClient,
    ): UnsubscribeMessageEventUseCase {
        return UnsubscribeMessageEventUseCase(client)
    }

    @Singleton
    @Provides
    fun provideClearRetainMessageEventUseCase(
        client: IMqttClient,
    ): ClearRetainMessageEventUseCase {
        return ClearRetainMessageEventUseCase(client)
    }

    @Provides
    @Singleton
    fun provideListenConversationEventUseCase(
        sharedPreferences: SharedPreferences,
        client: IMqttClient,
        mapper: EventMapper
    ): ListenConversationEventUseCase {
        return ListenConversationEventUseCase(sharedPreferences, client, mapper)
    }
}
