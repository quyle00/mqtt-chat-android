package com.quyt.mqttchat.data.repository

import com.google.gson.Gson
import com.quyt.mqttchat.data.datasource.remote.model.response.BaseResponse
import com.quyt.mqttchat.data.datasource.remote.service.ContactService
import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val service: ContactService
) : UserRepository {
    override suspend fun getListContacts(): Result<List<User>> {
        return try {
            val res = service.getListContact()
            if (res.isSuccessful) {
                Result.Success(res.body()?.data!!)
            } else {
                val error = Gson().fromJson(res.errorBody()?.string(), BaseResponse::class.java)
                Result.Error(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
