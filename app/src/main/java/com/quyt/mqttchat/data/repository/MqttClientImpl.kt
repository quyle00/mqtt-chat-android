package com.quyt.mqttchat.data.repository

import android.util.Log
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
                    val payload = String(publish.payloadAsBytes)
                    if (payload.isNotEmpty()){
                        callback(payload)
                        // Clear retain message
//                        if (publish.isRetain) {
//                            client.publishWith()
//                                .topic(publish.topic)
//                                .qos(MqttQos.AT_LEAST_ONCE)
//                                .retain(true)
//                                .payload("".toByteArray())
//                                .send()
//                        }
                    }
                }
                .send()

            subFuture.get()
        }
    }

    override suspend fun unsubscribe(topic: String) {
        withContext(Dispatchers.IO) {
            val unSubFuture = client.unsubscribeWith()
                .topicFilter(topic)
                .send()

            unSubFuture.get()
        }
    }

    override suspend fun publish(topic: String, payload: String, qos: Int,retain : Boolean) {
        withContext(Dispatchers.IO) {
            val pubFuture = client.publishWith()
                .topic(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .retain(retain)
                .payload(payload.toByteArray())
                .send()

            pubFuture.get()
        }
    }
}
