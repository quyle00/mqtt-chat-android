package com.quyt.mqttchat.data.repository

import android.content.Context
import com.google.gson.Gson
import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.domain.repository.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SharedPreferencesImpl @Inject constructor(context: Context) : SharedPreferences {

    private val sharedPreferences = context.getSharedPreferences("mqtt_chat", Context.MODE_PRIVATE)

    override fun saveCurrentUser(user: User) {
        val userJson = Gson().toJson(user)
        sharedPreferences.edit().putString("current_user", userJson).apply()
    }

    override fun getCurrentUser(): User? {
        val userJson = sharedPreferences.getString("current_user", "") ?: ""
        return Gson().fromJson(userJson, User::class.java)
    }
}