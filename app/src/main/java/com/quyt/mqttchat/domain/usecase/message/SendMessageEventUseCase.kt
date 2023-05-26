package com.quyt.mqttchat.domain.usecase.message

import com.quyt.mqttchat.domain.mapper.EventMapper
import com.quyt.mqttchat.domain.model.Event
import com.quyt.mqttchat.domain.repository.IMqttClient

class SendMessageEventUseCase(
    private val mqttClient: IMqttClient,
    private val mapper: EventMapper
) {
    suspend operator fun invoke(userId: String, partnerId: String, conversationId: String, event: Event) {
        mqttClient.publish(
            topic = "$partnerId/conversation/$conversationId",
            payload = mapper.toPayload(event), 0
        )
        mqttClient.publish(
            topic = "$userId/conversation/$conversationId",
            payload = mapper.toPayload(event.apply {
                this.message.apply {
                    this?.isMine = true
                }
            }), 0
        )
    }
}