package com.quyt.mqttchat.domain.model

import com.google.gson.annotations.SerializedName

// data class User(
//    var id : Int,
//    var name : String,
//    var username : String,
//    var avatarUrl : String,
// )

class User {
    @SerializedName("_id")
    var id: String? = null
    var fullname: String? = null
    var username: String? = null
    var avatar: String? = null
    var token: String? = null
    var lastSeen = 0L
    var isOnline = false
}
