package com.quyt.mqttchat.emoji

class Emoji(
    var emoji: String,
    var description: String,
    var category: String,
    var aliases: ArrayList<String>,
    var tags: ArrayList<String>,
    var unicode_version: String,
    var ios_version: String,
) {
    constructor(category : String) : this(
        "",
        "",
        category,
        ArrayList(),
        ArrayList(),
        "",
        ""
    )

    fun copy() : Emoji {
        return Emoji(
            this.emoji,
            this.description,
            this.category,
            this.aliases,
            this.tags,
            this.unicode_version,
            this.ios_version
        )
    }
}