package com.quyt.mqttchat.utils.network

interface NetworkChecker {
    fun isNetworkConnected(): Boolean
}