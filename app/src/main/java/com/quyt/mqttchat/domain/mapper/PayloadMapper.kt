package com.quyt.mqttchat.domain.mapper

interface PayloadMapper<T> {
    fun toModel(payload: String): T
    fun toPayload(model: T): String
}
