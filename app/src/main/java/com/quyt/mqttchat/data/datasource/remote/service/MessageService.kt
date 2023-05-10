package com.quyt.mqttchat.data.datasource.remote.service

import com.quyt.mqttchat.data.datasource.remote.model.response.BaseResponse
import com.quyt.mqttchat.domain.model.Conversation
import com.quyt.mqttchat.domain.model.Message
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MessageService {

    @GET("conversation/{conversationId}/message")
    suspend fun getListMessage(
        @Path("conversationId") conversationId: String,
    ): Response<BaseResponse<List<Message>>>

    @POST("conversation/{conversationId}/message")
    suspend fun createMessage(
        @Path("conversationId") conversationId: String,
        @Body message: Message
    ) : Response<BaseResponse<Message>>

}