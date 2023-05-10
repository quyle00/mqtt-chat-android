package com.quyt.mqttchat.domain.usecase

import com.quyt.mqttchat.domain.mapper.EventMapper
import com.quyt.mqttchat.domain.mapper.MessageMapper
import com.quyt.mqttchat.domain.model.Event
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.repository.IMqttClient

class SendConversationEventUseCase(private val mqttClient: IMqttClient, private val mapper: EventMapper) {
    suspend operator fun invoke(conversationId: String, message: Event) {
        mqttClient.publish("conversation/$conversationId", mapper.toPayload(message), 0)
    }
}