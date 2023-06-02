package com.quyt.mqttchat

import android.app.Application
import android.util.Log
import com.quyt.mqttchat.domain.repository.IMqttClient
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@HiltAndroidApp
class MyApp : Application() {

    @Inject
    lateinit var mqttClient: IMqttClient

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        GlobalScope.launch {
            if (!mqttClient.connect()) {
                Log.d("MQTT", "Failed to connect")
            }
        }
    }
}
