package com.quyt.mqttchat.data.repository

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.quyt.mqttchat.domain.repository.IMqttClient
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MqttClientImpl @Inject constructor(private val client: Mqtt3AsyncClient) : IMqttClient {
    override suspend fun connect(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                client.connect().get()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            client.disconnect().get()
        }
    }

    override suspend fun subscribe(topic: String, qos: Int, callback: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            val subFuture = client.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback { publish ->
                    callback(String(publish.payloadAsBytes))
                }
                .send()

            subFuture.get()
        }
    }

    override suspend fun publish(topic: String, payload: String, qos: Int) {
        withContext(Dispatchers.IO) {
            val pubFuture = client.publishWith()
                .topic(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload(payload.toByteArray())
                .send()

            pubFuture.get()
        }
    }
}
