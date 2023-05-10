package com.quyt.mqttchat.domain.repository

import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.model.User

interface AccessRepository {
    suspend fun login(username: String, password: String) : Result<User>
}