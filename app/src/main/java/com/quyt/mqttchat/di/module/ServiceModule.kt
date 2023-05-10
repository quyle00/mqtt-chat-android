package com.quyt.mqttchat.di.module

import com.quyt.mqttchat.data.datasource.remote.service.AccessService
import com.quyt.mqttchat.data.datasource.remote.service.ContactService
import com.quyt.mqttchat.data.datasource.remote.service.ConversationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ServiceModule {
    @Provides
    @Singleton
    fun provideAccessServices(retrofit: Retrofit): AccessService = retrofit.create(AccessService::class.java)

    @Provides
    @Singleton
    fun provideContactServices(retrofit: Retrofit): ContactService = retrofit.create(ContactService::class.java)

    @Provides
    @Singleton
    fun provideConversationServices(retrofit: Retrofit): ConversationService = retrofit.create(ConversationService::class.java)
}