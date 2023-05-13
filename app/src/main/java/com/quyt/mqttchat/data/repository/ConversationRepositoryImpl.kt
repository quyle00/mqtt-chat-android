package com.quyt.mqttchat.data.repository

import com.quyt.mqttchat.data.datasource.remote.extension.getError
import com.quyt.mqttchat.data.datasource.remote.service.ConversationService
import com.quyt.mqttchat.domain.model.Conversation
import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.repository.ConversationRepository
import javax.inject.Inject

class ConversationRepositoryImpl @Inject constructor(
    private val service: ConversationService
) : ConversationRepository {
    override suspend fun createConversation(participants: ArrayList<String>): Result<Conversation> {
        return try {
            val res = service.createConversation(participants)
            if (res.isSuccessful) {
                Result.Success(res.body()?.data!!)
            } else {
                Result.Error(res.getError())
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getListConversation(): Result<List<Conversation>> {
        return try {
            val res = service.getListConversation()
            if (res.isSuccessful) {
                Result.Success(res.body()?.data!!)
            } else {
                Result.Error(res.getError())
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getConversationDetail(id: String): Result<Conversation> {
        return try {
            val res = service.getConversationDetail(id)
            if (res.isSuccessful) {
                Result.Success(res.body()?.data!!)
            } else {
                Result.Error(res.getError())
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getConversationDetailByPartnerId(partnerId: String): Result<Conversation> {
        return try {
            val res = service.getConversationDetailByPartnerId(partnerId)
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