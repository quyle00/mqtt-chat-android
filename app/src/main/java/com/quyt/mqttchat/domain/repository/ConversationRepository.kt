package com.quyt.mqttchat.domain.repository

import com.quyt.mqttchat.domain.model.Conversation
import com.quyt.mqttchat.domain.model.Result

interface ConversationRepository {
    suspend fun createConversation(participants: ArrayList<String>): Result<Conversation>
    suspend fun getListConversation(): Result<List<Conversation>>
    suspend fun getConversationDetail(id: String): Result<Conversation>
}