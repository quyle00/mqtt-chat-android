package com.quyt.mqttchat.domain.usecase.conversation

import com.quyt.mqttchat.domain.model.Conversation
import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.domain.repository.ConversationRepository
import com.quyt.mqttchat.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreateConversationUseCase(private val conversationRepository: ConversationRepository) {
    suspend operator fun invoke(participants: ArrayList<String>): Result<Conversation> {
        return withContext(Dispatchers.IO) {
            conversationRepository.createConversation(participants)
        }
    }
}