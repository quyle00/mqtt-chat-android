package com.quyt.mqttchat.data.datasource.remote.extension

import com.google.gson.Gson
import com.quyt.mqttchat.data.datasource.remote.model.response.BaseResponse
import retrofit2.Response

fun <T> Response<T>.getError(): Exception {
    val error = Gson().fromJson(errorBody()?.string(), BaseResponse::class.java)
    return Exception(error.message)
}