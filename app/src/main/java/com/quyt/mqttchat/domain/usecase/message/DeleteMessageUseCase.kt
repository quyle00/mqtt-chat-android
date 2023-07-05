package com.quyt.mqttchat.domain.usecase.message

import com.quyt.mqttchat.data.datasource.remote.model.response.DeleteMessageResponse
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.repository.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
class DeleteMessageUseCase(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(message: Message, shouldDeleteRemote : Boolean): Result<DeleteMessageResponse> {
        return withContext(Dispatchers.IO) {
            messageRepository.deleteMessage(message,shouldDeleteRemote)
        }
    }
}