package com.quyt.mqttchat.domain.usecase.message.realTime

import com.quyt.mqttchat.domain.mapper.EventMapper
import com.quyt.mqttchat.domain.model.Event
import com.quyt.mqttchat.domain.model.EventType
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.repository.IMqttClient
import com.quyt.mqttchat.domain.repository.SharedPreferences

class SendDeleteMessageEventUseCase(
    private val sharedPreferences: SharedPreferences,
    private val mqttClient: IMqttClient,
    private val mapper: EventMapper
) {
    suspend operator fun invoke(conversationId: String, partnerId: String, message: Message) {
        val event = Event(
            sharedPreferences.getCurrentUser()?.id,
            EventType.DELETE.value,
            message
        )
        mqttClient.publish(
            topic = "conversation/$conversationId/$partnerId",
            payload = mapper.toPayload(event),
            0,
            retain = true
        )
    }
}
