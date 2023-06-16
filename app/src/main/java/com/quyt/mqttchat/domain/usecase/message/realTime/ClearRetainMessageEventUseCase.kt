package com.quyt.mqttchat.domain.usecase.message.realTime

import com.quyt.mqttchat.domain.repository.IMqttClient

class ClearRetainMessageEventUseCase(
    private val mqttClient: IMqttClient,
) {
    suspend operator fun invoke(conversationId: String, userId: String) {
        mqttClient.publish(
            topic = "conversation/$conversationId/$userId",
            payload = "",
            0,
            true
        )
    }
}
