package com.quyt.mqttchat.data.datasource.remote.service

import com.quyt.mqttchat.data.datasource.remote.model.response.BaseResponse
import com.quyt.mqttchat.data.datasource.remote.model.response.MessagePagingResponse
import com.quyt.mqttchat.domain.model.Message
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface MessageService {
    @GET("conversation/{conversationId}/message")
    suspend fun getListMessage(
        @Path("conversationId") conversationId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20
    ): Response<BaseResponse<MessagePagingResponse>>

    @POST("conversation/{conversationId}/message")
    suspend fun createMessage(
        @Path("conversationId") conversationId: String,
        @Body message: Message
    ): Response<BaseResponse<Message>>

    @Multipart
    @POST("conversation/{conversationId}/message")
    suspend fun createMessage2(
        @Path("conversationId") conversationId: String,
        @Part("message") message: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Response<BaseResponse<Message>>

    @PUT("conversation/{conversationId}/message/seen")
    @FormUrlEncoded
    suspend fun updateSeenMessage(
        @Path("conversationId") conversationId: String,
        @Field("messageIds") messageIds: List<String>
    ): Response<BaseResponse<String>>

    @PUT("conversation/{conversationId}/message/")
    suspend fun updateMessage(
        @Path("conversationId") conversationId: String,
        @Body message: Message
    ): Response<BaseResponse<Message>>

    @DELETE("conversation/{conversationId}/message/{messageId}")
    suspend fun deleteMessage(
        @Path("conversationId") conversationId: String,
        @Path("messageId") messageId: String
    ): Response<BaseResponse<String>>
}
