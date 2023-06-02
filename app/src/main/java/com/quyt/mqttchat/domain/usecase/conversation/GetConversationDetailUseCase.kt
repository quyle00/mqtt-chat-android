package com.quyt.mqttchat.domain.usecase.conversation

import com.quyt.mqttchat.domain.model.Conversation
import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.repository.ConversationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetConversationDetailUseCase(private val conversationRepository: ConversationRepository) {
    suspend operator fun invoke(conversationId: String? = null, partnerId: String? = null): Result<Conversation> {
        return withContext(Dispatchers.IO) {
            if (!conversationId.isNullOrEmpty()) {
                conversationRepository.getConversationDetail(conversationId)
            } else {
                conversationRepository.getConversationDetailByPartnerId(partnerId ?: "")
            }
        }
    }
}
