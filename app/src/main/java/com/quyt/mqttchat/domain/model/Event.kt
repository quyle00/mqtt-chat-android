package com.quyt.mqttchat.domain.model

class Event(
    var publisherId: String?,
    var type: Int?,
    var message: Message?,
    var messageIds: List<String>?
) {
    constructor() : this(
        publisherId = "",
        type = 0,
        message = Message(),
        messageIds = listOf()
    )

    constructor(
        publisherId: String?,
        type: Int?,
        message: Message?
    ) : this(
        publisherId = publisherId,
        type = type,
        message = message,
        messageIds = listOf()
    )

    constructor(
        publisherId: String?,
        type: Int?,
        messageIds: List<String>?
    ) : this(
        publisherId = publisherId,
        type = type,
        message = Message(),
        messageIds = messageIds
    )
}

enum class EventType(val value: Int) {
    NEW_MESSAGE(0),
    TYPING(1),
    MARK_READ(2),
    EDIT(3),
    DELETE(4),
}
