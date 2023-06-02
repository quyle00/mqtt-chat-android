package com.quyt.mqttchat.domain.usecase.message

import com.quyt.mqttchat.domain.mapper.EventMapper
import com.quyt.mqttchat.domain.model.Event
import com.quyt.mqttchat.domain.model.EventType
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.repository.IMqttClient
import com.quyt.mqttchat.domain.repository.SharedPreferences

class SendNewMessageEventUseCase(
    private val sharedPreferences: SharedPreferences,
    private val mqttClient: IMqttClient,
    private val mapper: EventMapper
) {
    suspend operator fun invoke(conversationId: String, partnerId: String, message: Message) {
        val event = Event(
            sharedPreferences.getCurrentUser()?.id,
            EventType.NEW_MESSAGE.value,
            message
        )
        mqttClient.publish(
            topic = "$partnerId/conversation/$conversationId",
            payload = mapper.toPayload(event),
            0
        )
    }
}
