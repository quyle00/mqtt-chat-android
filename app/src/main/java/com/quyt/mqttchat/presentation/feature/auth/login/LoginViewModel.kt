package com.quyt.mqttchat.presentation.feature.auth.login

import androidx.lifecycle.viewModelScope
import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.domain.repository.SharedPreferences
import com.quyt.mqttchat.domain.usecase.access.LoginUseCase
import com.quyt.mqttchat.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginState {
    object Loading : LoginState()
    data class Success(val data: User) : LoginState()
    data class Error(val error: String) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val sharedPreferences: SharedPreferences
) : BaseViewModel<LoginState>() {

    fun login(username: String, password: String) {
        uiState.postValue(LoginState.Loading)
        viewModelScope.launch {
            val res = loginUseCase(username, password)
            when (res) {
                is Result.Success -> {
                    sharedPreferences.saveCurrentUser(res.data)
                    uiState.postValue(LoginState.Success(res.data))
                }

                is Result.Error -> {
                    uiState.postValue(LoginState.Error(res.exception.message ?: "Error"))
                }
            }
        }
    }
}