package com.quyt.mqttchat.domain.usecase.user

import com.google.gson.JsonObject
import com.quyt.mqttchat.domain.mapper.UserMapper
import com.quyt.mqttchat.domain.repository.IMqttClient
import com.quyt.mqttchat.domain.repository.SharedPreferences
import com.quyt.mqttchat.utils.DateUtils

class SendUserStatusEventUseCase(
    private val sharedPreferences: SharedPreferences,
    private val mqttClient: IMqttClient,
) {
    suspend operator fun invoke(isOnline: Boolean) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("_id", sharedPreferences.getCurrentUser()?.id)
        jsonObject.addProperty("isOnline", isOnline)
        jsonObject.addProperty("lastSeen", DateUtils.currentTimestamp())
        mqttClient.publish(
            topic = "user/${sharedPreferences.getCurrentUser()?.id}/status",
            payload = jsonObject.toString(),
            0,
            retain = true
        )
    }
}
