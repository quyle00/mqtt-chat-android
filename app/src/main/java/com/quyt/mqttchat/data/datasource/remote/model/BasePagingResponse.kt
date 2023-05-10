package com.quyt.mqttchat.data.datasource.remote.model

open class BasePagingResponse {
    val page: Int? = null
    val total: Int? = null
    val limit: Int? = null
    var error : String? = null
}