package com.quyt.mqttchat.data.datasource.remote.model.response

import com.google.gson.annotations.SerializedName
import com.quyt.mqttchat.domain.model.Message

class MessagePagingResponse {
    @SerializedName("data")
    var data: List<Message>? = null

    @SerializedName("pagination")
    var pagination: Pagination? = null
}

class Pagination {
    @SerializedName("total_rows")
    var totalRows: Int? = null

    @SerializedName("total_pages")
    var totalPages: Int? = null

    @SerializedName("current_page")
    var currentPage: Int? = null

    @SerializedName("hasPrevPage")
    var hasPrevPage: Boolean? = null

    @SerializedName("hasNextPage")
    var hasNextPage: Boolean? = null

    @SerializedName("prev")
    var prev: Int? = null

    @SerializedName("next")
    var next: Int? = null
}
