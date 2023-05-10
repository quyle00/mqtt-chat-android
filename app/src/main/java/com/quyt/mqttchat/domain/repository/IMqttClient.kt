package com.quyt.mqttchat.domain.repository

interface IMqttClient {
    suspend fun connect() : Boolean
    suspend fun disconnect()
    suspend fun subscribe(topic: String, qos: Int, callback: (String) -> Unit)
    suspend fun publish(topic: String, message: String, qos: Int)

}