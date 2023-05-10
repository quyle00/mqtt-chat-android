package com.quyt.mqttchat.data.datasource.remote.exception

import java.io.IOException

class NoInternetException(message: String = "No internet connection") : IOException(message)