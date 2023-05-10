package com.quyt.mqttchat.domain.usecase

import com.quyt.mqttchat.domain.mapper.EventMapper
import com.quyt.mqttchat.domain.mapper.MessageMapper
import com.quyt.mqttchat.domain.model.Event
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.repository.IMqttClient

class ListenConversationEventUseCase(private val mqttClient: IMqttClient, private val mapper: EventMapper) {
    suspend operator fun invoke(conversationId: Int, callback: (Event) -> Unit) {
        mqttClient.subscribe("conversation/$conversationId", 0){
            callback(mapper.toModel(it))
        }
    }
}