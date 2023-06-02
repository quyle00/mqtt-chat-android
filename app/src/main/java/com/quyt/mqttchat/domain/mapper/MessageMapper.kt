package com.quyt.mqttchat.domain.mapper

import com.google.gson.Gson
import com.quyt.mqttchat.domain.model.Message

class MessageMapper : PayloadMapper<Message> {
    override fun toModel(payload: String): Message {
        return try {
            Gson().fromJson(payload, Message::class.java)
        } catch (e: Exception) {
            Message()
        }
    }
    override fun toPayload(model: Message): String {
        return Gson().toJson(model)
    }
}
