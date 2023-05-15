package com.quyt.mqttchat.domain.usecase.message

import com.quyt.mqttchat.domain.mapper.EventMapper
import com.quyt.mqttchat.domain.model.Event
import com.quyt.mqttchat.domain.repository.IMqttClient
import com.quyt.mqttchat.domain.repository.SharedPreferences

class SendMessageEventUseCase(
    private val mqttClient: IMqttClient,
    private val mapper: EventMapper) {
    suspend operator fun invoke(partnerId : String,conversationId: String, message: Event) {
        mqttClient.publish("$partnerId/conversation/$conversationId", mapper.toPayload(message), 0)
    }
}