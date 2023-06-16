package com.quyt.mqttchat.presentation.feature.auth.register

import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.presentation.base.BaseViewModel

sealed class RegisterState {
    object Loading : RegisterState()
    data class Success(val data: User) : RegisterState()
    data class Error(val error: String) : RegisterState()
}

class RegisterViewModel : BaseViewModel<RegisterState>() {
}