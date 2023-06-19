package com.quyt.mqttchat.data.repository

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.quyt.mqttchat.domain.repository.IMqttClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MqttClientImpl @Inject constructor(private val client: Mqtt3AsyncClient) : IMqttClient {
    override suspend fun connect(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                client.connectWith().cleanSession(false).send()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            client.disconnect()
        }
    }

    override suspend fun subscribe(topic: String, qos: Int, callback: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            client.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback { publish ->
                    val payload = String(publish.payloadAsBytes)
                    if (payload.isNotEmpty()) {
                        callback(payload)
                    }
                }
                .send()
        }
    }

    override suspend fun unsubscribe(topic: String) {
        withContext(Dispatchers.IO) {
            client.unsubscribeWith()
                .topicFilter(topic)
                .send()
        }
    }

    override suspend fun publish(topic: String, payload: String, qos: Int, retain: Boolean) {
        withContext(Dispatchers.IO) {
            client.publishWith()
                .topic(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .retain(retain)
                .payload(payload.toByteArray())
                .send()

        }
    }
}
