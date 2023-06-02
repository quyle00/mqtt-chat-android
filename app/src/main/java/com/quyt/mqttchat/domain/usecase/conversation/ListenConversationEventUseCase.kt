package com.quyt.mqttchat.domain.usecase.conversation

import com.quyt.mqttchat.domain.mapper.EventMapper
import com.quyt.mqttchat.domain.model.Event
import com.quyt.mqttchat.domain.repository.IMqttClient
import com.quyt.mqttchat.domain.repository.SharedPreferences

class ListenConversationEventUseCase(
    private val sharedPreferences: SharedPreferences,
    private val mqttClient: IMqttClient,
    private val mapper: EventMapper
) {
    suspend operator fun invoke(callback: (Event) -> Unit) {
        mqttClient.subscribe("${sharedPreferences.getCurrentUser()?.id}/conversation/#", 0) {
            callback(mapper.toModel(it))
        }
    }
}
