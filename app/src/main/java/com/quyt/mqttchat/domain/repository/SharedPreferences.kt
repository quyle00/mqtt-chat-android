package com.quyt.mqttchat.domain.repository

import com.quyt.mqttchat.domain.model.User

interface SharedPreferences {
    fun saveCurrentUser(user: User)
    fun getCurrentUser(): User?
}
