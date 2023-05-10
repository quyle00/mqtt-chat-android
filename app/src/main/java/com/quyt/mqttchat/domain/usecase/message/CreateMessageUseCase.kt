package com.quyt.mqttchat.domain.usecase.message

import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.repository.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreateMessageUseCase(private val messageRepository: MessageRepository) {
    suspend operator fun invoke(conversationId: String, message: Message): Result<Message> {
        return withContext(Dispatchers.IO) {
            messageRepository.createMessage(conversationId, message)
        }
    }
}