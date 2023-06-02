package com.quyt.mqttchat.domain.usecase.message

import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.repository.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SeenMessageUseCase(private val messageRepository: MessageRepository) {
    suspend operator fun invoke(conversationId: String, messageIds: List<String>): Result<String> {
        return withContext(Dispatchers.IO) {
            messageRepository.updateSeenMessage(conversationId, messageIds)
        }
    }
}
