package com.quyt.mqttchat.domain.usecase.user

import com.quyt.mqttchat.domain.mapper.UserMapper
import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.domain.repository.IMqttClient
import com.quyt.mqttchat.domain.repository.SharedPreferences

class ListenUserStatusEventUseCase(
    private val mqttClient: IMqttClient,
    private val mapper: UserMapper
) {
    suspend operator fun invoke(partnerId: String? = null, callback: (User) -> Unit) {
        val partnerRegex = partnerId.takeIf { !it.isNullOrEmpty() } ?: "+"
        mqttClient.subscribe("user/$partnerRegex/status", 0) {
            callback(mapper.toModel(it))
        }
    }
}
