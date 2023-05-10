package com.quyt.mqttchat.data.datasource.remote.interceptor

import android.net.ConnectivityManager
import com.quyt.mqttchat.data.datasource.remote.exception.NoInternetException
import com.quyt.mqttchat.utils.network.NetworkChecker
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class ConnectivityInterceptor (
    private val networkChecker: NetworkChecker
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!networkChecker.isNetworkConnected()) {
            throw NoInternetException()
        }
        return chain.proceed(chain.request())
    }
}