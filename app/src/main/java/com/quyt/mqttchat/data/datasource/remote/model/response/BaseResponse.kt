package com.quyt.mqttchat.data.datasource.remote.model.response

import retrofit2.Response

open class BaseResponse<T>{
    var code: Int? = null
    var message: String? = null
    var error: Boolean = false
    var data : T? = null
}