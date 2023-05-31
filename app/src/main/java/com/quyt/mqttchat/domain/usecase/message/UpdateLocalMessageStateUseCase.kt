package com.quyt.mqttchat.domain.usecase.message

import com.quyt.mqttchat.domain.repository.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateLocalMessageStateUseCase(private val messageRepository: MessageRepository) {
    suspend operator fun invoke(messageIds: List<String>, newState: Int) {
        return withContext(Dispatchers.IO) {
            messageRepository.updateLocalMessageState(messageIds, newState)
        }
    }
}