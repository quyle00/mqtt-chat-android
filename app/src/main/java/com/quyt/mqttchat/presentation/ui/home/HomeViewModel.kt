package com.quyt.mqttchat.presentation.ui.home

import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.presentation.base.BaseViewModel

sealed class HomeState {
    object Loading : HomeState()
    data class Success(val data: User) : HomeState()
    data class Error(val error: String) : HomeState()
}
class HomeViewModel : BaseViewModel<HomeState>() {
}