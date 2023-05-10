package com.quyt.mqttchat.data.datasource.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("app-id", "642a7f19674c92985d0410ca").build()
        return chain.proceed(request)
    }
}
