package com.quyt.mqttchat.data.datasource.remote.service

import com.quyt.mqttchat.data.datasource.remote.model.response.BaseResponse
import com.quyt.mqttchat.domain.model.Conversation
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ConversationService {
    @POST("conversation")
    @FormUrlEncoded
    suspend fun createConversation(
        @Field("participants") participants: List<String>,
    ): Response<BaseResponse<Conversation>>

    @GET("conversation")
    suspend fun getListConversation(): Response<BaseResponse<List<Conversation>>>

    @GET("conversation/{id}")
    suspend fun getConversationDetail(
        @Path("id") id: String,
    ): Response<BaseResponse<Conversation>>

    @GET("conversation/by-partner/{partnerId}")
    suspend fun getConversationDetailByPartnerId(
        @Path("partnerId") id: String,
    ): Response<BaseResponse<Conversation>>
}