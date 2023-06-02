package com.quyt.mqttchat.data.datasource.remote.extension

import com.google.gson.Gson
import com.quyt.mqttchat.data.datasource.remote.model.response.BaseResponse
import retrofit2.Response

fun <T> Response<T>.getError(): Throwable {
    val error = Gson().fromJson(errorBody()?.string(), BaseResponse::class.java)
    return CustomException(error.code ?: 0, error.message)
}

class CustomException(val code: Int, message: String?) : Exception(message)
