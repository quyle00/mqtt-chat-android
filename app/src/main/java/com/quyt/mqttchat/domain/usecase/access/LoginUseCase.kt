package com.quyt.mqttchat.domain.usecase.access

import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.domain.repository.AccessRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginUseCase(private val accessRepository: AccessRepository) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            accessRepository.login(username, password)
        }
    }
}
