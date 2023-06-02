package com.quyt.mqttchat.domain.usecase.message

import com.quyt.mqttchat.domain.mapper.EventMapper
import com.quyt.mqttchat.domain.model.Event
import com.quyt.mqttchat.domain.model.EventType
import com.quyt.mqttchat.domain.repository.IMqttClient
import com.quyt.mqttchat.domain.repository.SharedPreferences

class SendMarkReadEventUseCase(
    private val sharedPreferences: SharedPreferences,
    private val mqttClient: IMqttClient,
    private val mapper: EventMapper
) {
    suspend operator fun invoke(
        conversationId: String,
        partnerId: String,
        markReadMessageIds: List<String>
    ) {
        val event = Event(
            sharedPreferences.getCurrentUser()?.id,
            EventType.MARK_READ.value,
            markReadMessageIds
        )
        mqttClient.publish(
            topic = "$partnerId/conversation/$conversationId",
            payload = mapper.toPayload(event),
            0
        )
    }
}
