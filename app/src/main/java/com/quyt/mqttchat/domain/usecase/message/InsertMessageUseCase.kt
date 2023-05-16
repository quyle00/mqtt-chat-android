package com.quyt.mqttchat.domain.usecase.message

import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.repository.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InsertMessageUseCase(private val messageRepository: MessageRepository) {
    suspend operator fun invoke(messageList: List<Message>) {
        return withContext(Dispatchers.IO) {
            messageRepository.insertMessage(messageList)
        }
    }
}