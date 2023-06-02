package com.quyt.mqttchat.domain.usecase.message

import com.quyt.mqttchat.domain.mapper.EventMapper
import com.quyt.mqttchat.domain.model.Event
import com.quyt.mqttchat.domain.repository.IMqttClient

class ListenMessageEventUseCase(
    private val mqttClient: IMqttClient,
    private val mapper: EventMapper
) {
    suspend operator fun invoke(conversationId: String, callback: (Event) -> Unit) {
        mqttClient.subscribe("+/conversation/$conversationId", 0) {
            callback(mapper.toModel(it))
        }
    }
}
