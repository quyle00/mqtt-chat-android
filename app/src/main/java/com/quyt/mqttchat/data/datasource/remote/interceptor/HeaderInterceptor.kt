package com.quyt.mqttchat.data.datasource.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Add header here
        val request = chain.request().newBuilder().build()
        return chain.proceed(request)
    }
}
