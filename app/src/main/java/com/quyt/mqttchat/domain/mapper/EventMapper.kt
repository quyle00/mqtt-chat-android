package com.quyt.mqttchat.domain.mapper

import com.google.gson.Gson
import com.quyt.mqttchat.domain.model.Event
import com.quyt.mqttchat.domain.model.Message

class EventMapper : PayloadMapper<Event> {
    override fun toModel(payload: String): Event {
        return try {
            Gson().fromJson(payload, Event::class.java)
        } catch (e: Exception) {
            Event()
        }
    }
    override fun toPayload(model: Event): String {
        return Gson().toJson(model)
    }
}