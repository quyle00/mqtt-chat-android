package com.quyt.mqttchat.di.module

import android.content.Context
import androidx.room.Room
import com.google.gson.GsonBuilder
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3WillPublishBuilder
import com.quyt.mqttchat.data.datasource.local.db.AppDatabase
import com.quyt.mqttchat.data.datasource.remote.interceptor.ConnectivityInterceptor
import com.quyt.mqttchat.data.datasource.remote.interceptor.HeaderInterceptor
import com.quyt.mqttchat.data.datasource.remote.interceptor.TokenInterceptor
import com.quyt.mqttchat.data.repository.MqttClientImpl
import com.quyt.mqttchat.data.repository.SharedPreferencesImpl
import com.quyt.mqttchat.domain.repository.IMqttClient
import com.quyt.mqttchat.domain.repository.SharedPreferences
import com.quyt.mqttchat.utils.network.NetworkChecker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.UUID
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {

//    @Provides
//    @Singleton
//    fun provideOkHttpClient(headerInterceptor: HeaderInterceptor) = if (BuildConfig.DEBUG) {
//        val loggingInterceptor = HttpLoggingInterceptor()
//        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
//        OkHttpClient.Builder()
//            .addInterceptor(loggingInterceptor)
//            .addInterceptor(headerInterceptor)
//            .build()
//    } else {
//        OkHttpClient.Builder()
//            .addInterceptor(headerInterceptor)
//            .build()
//    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext ctx: Context): AppDatabase {
        return Room.databaseBuilder(
            ctx,
            AppDatabase::class.java,
            "my_app_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        headerInterceptor: HeaderInterceptor,
        tokenInterceptor: TokenInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(headerInterceptor)
            .addInterceptor(tokenInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideHeaderInterceptor(): HeaderInterceptor {
        return HeaderInterceptor()
    }

    @Provides
    @Singleton
    fun provideTokenInterceptor(sharedPreferences: SharedPreferences): TokenInterceptor {
        return TokenInterceptor(sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideConnectivityInterceptor(networkChecker: NetworkChecker): ConnectivityInterceptor {
        return ConnectivityInterceptor(networkChecker)
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder().serializeNulls().create()
        return Retrofit.Builder()
            .baseUrl("http://172.17.12.122:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideMqttClient(sharedPreferences: SharedPreferences): Mqtt3AsyncClient {
        return Mqtt3Client.builder()
            .identifier(sharedPreferences.getCurrentUser()?.id?: UUID.randomUUID().toString())
            .serverHost("172.17.12.122")
            .buildAsync()
    }

    @Provides
    @Singleton
    fun provideIMqttClient(client: Mqtt3AsyncClient): IMqttClient {
        return MqttClientImpl(client)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return SharedPreferencesImpl(context)
    }
}
