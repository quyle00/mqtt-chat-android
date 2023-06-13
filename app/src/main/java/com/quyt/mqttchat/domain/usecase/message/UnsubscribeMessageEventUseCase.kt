package com.quyt.mqttchat.domain.usecase.message

import com.quyt.mqttchat.domain.mapper.EventMapper
import com.quyt.mqttchat.domain.model.Event
import com.quyt.mqttchat.domain.repository.IMqttClient

class UnsubscribeMessageEventUseCase(
    private val mqttClient: IMqttClient,
) {
    suspend operator fun invoke(conversationId: String, ) {
        mqttClient.unsubscribe("+/conversation/$conversationId")
    }
}
