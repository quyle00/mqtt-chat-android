package com.quyt.mqttchat.data.repository

import com.quyt.mqttchat.data.datasource.remote.extension.getError
import com.quyt.mqttchat.data.datasource.remote.service.AccessService
import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.domain.repository.AccessRepository
import com.quyt.mqttchat.domain.repository.SharedPreferences
import javax.inject.Inject

class AccessRepositoryImpl @Inject constructor(
    private val service: AccessService,
    private val sharedPreferences: SharedPreferences
) : AccessRepository {
    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val res = service.login(username, password,sharedPreferences.getDeviceToken()?:"")
            if (res.isSuccessful) {
                Result.Success(res.body()?.data!!)
            } else {
                Result.Error(res.getError())
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
