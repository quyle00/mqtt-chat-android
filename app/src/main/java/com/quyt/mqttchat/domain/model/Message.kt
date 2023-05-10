package com.quyt.mqttchat.domain.model

//data class Message(
//    var id: Int,
//    var conversationId : Int,
//    var sender: User,
//    var content : String,
//    var sendTime : String,
//)

class Message {
    var id: Int = 0
    var conversationId: Int = 0
    var sender: User? = null
    var content: String? = null
    var sendTime: String? = null
    var isTyping: Boolean = false
}
