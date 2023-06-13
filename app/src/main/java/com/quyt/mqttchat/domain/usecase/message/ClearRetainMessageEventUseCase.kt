package com.quyt.mqttchat.domain.usecase.message

import com.quyt.mqttchat.domain.repository.IMqttClient

class ClearRetainMessageEventUseCase(
    private val mqttClient: IMqttClient,
) {
    suspend operator fun invoke(conversationId: String, userId: String) {
        mqttClient.publish(
            topic = "$userId/conversation/$conversationId",
            payload = "",
            0,
            true
        )
    }
}
