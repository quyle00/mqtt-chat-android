package com.quyt.mqttchat.data.datasource.remote.service

import com.quyt.mqttchat.data.datasource.remote.model.response.BaseResponse
import com.quyt.mqttchat.domain.model.User
import retrofit2.Response
import retrofit2.http.GET

interface ContactService {
    @GET("user")
    suspend fun getListContact(): Response<BaseResponse<List<User>>>
}
