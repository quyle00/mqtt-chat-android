package com.quyt.mqttchat.utils.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class NetworkCheckerImpl(private val context: Context) : NetworkChecker {

    companion object {
        const val CONNECTION_CELLULAR = 1
        const val CONNECTION_WIFI = 2
        const val CONNECTION_VPN = 3
    }

    override fun isNetworkConnected(): Boolean {
        val connectionType = getConnectionType(context)
        return connectionType != 0
    }

    private fun getConnectionType(context: Context): Int {
        var result = 0 // Returns connection type. 0: none; 1: mobile data; 2: wifi
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        cm?.run {
            cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        result = CONNECTION_WIFI
                    }
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        result = CONNECTION_CELLULAR
                    }
                    hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                        result = CONNECTION_VPN
                    }
                }
            }
        }
        return result
    }
}
