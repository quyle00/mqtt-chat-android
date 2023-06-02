package com.quyt.mqttchat.data.datasource.remote.service

import com.quyt.mqttchat.data.datasource.remote.model.response.BaseResponse
import com.quyt.mqttchat.domain.model.User
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AccessService {
    @POST("access/login")
    @FormUrlEncoded
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<BaseResponse<User>>
}
