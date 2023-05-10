package com.quyt.mqttchat.data.datasource.remote.interceptor

import com.quyt.mqttchat.domain.repository.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class TokenInterceptor(private val sharedPreferences: SharedPreferences) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request().signedRequest()
        return chain.proceed(newRequest)
    }

    private fun Request.signedRequest(): Request {
        return newBuilder()
            .header("x-access-token", sharedPreferences.getCurrentUser()?.token.toString())
            .build()
    }
}
