package com.quyt.mqttchat.domain.repository

import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.model.User

interface UserRepository {
    suspend fun getListContacts(): Result<List<User>>
}
