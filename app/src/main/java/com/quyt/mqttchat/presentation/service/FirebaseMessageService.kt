package com.quyt.mqttchat.presentation.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.quyt.mqttchat.R
import com.quyt.mqttchat.domain.repository.SharedPreferences
import com.quyt.mqttchat.presentation.feature.MainActivity
import com.quyt.mqttchat.utils.DateUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseMessageService : FirebaseMessagingService() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val CHANNEL_ID = "chat_notification"
        private const val CHANNEL_NAME = "Chat"
        private const val CHANNEL_DESCRIPTION = "Chat notification"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sharedPreferences.saveDeviceToken(token)
        Log.d("FirebaseMessageService", "onNewToken: ${token}")
        Log.d("FirebaseMessageService", "onNewToken: ${sharedPreferences.getDeviceToken()}")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        showNotification(message.data["title"], message.data["body"], message.data["conversationId"])
    }

    private fun showNotification(title: String?, message: String?, conversationId: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("conversationId", conversationId)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)


        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_messenger)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply { description = CHANNEL_DESCRIPTION }
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(DateUtils.currentTimestamp().toInt(), notificationBuilder.build())
    }

}