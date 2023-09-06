package com.quyt.mqttchat.domain.model

class StickerPack (
    var name : String,
    var stickers : ArrayList<Sticker>,
    var selected : Boolean = false
)