package com.quyt.mqttchat.domain.usecase.message

import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.MessageState
import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.repository.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateMessageUseCase(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(message: Message,shouldUpdateRemote : Boolean): Result<Message> {
        return withContext(Dispatchers.IO) {
            messageRepository.updateMessage(message,shouldUpdateRemote)
        }
    }
}