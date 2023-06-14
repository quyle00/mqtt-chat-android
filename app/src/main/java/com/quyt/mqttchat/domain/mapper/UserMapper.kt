package com.quyt.mqttchat.domain.mapper

import com.google.gson.Gson
import com.quyt.mqttchat.domain.model.Event
import com.quyt.mqttchat.domain.model.User

class UserMapper : PayloadMapper<User> {
    override fun toModel(payload: String): User {
        return try {
            Gson().fromJson(payload, User::class.java)
        } catch (e: Exception) {
            User()
        }
    }

    override fun toPayload(model: User): String {
        return Gson().toJson(model)
    }
}
