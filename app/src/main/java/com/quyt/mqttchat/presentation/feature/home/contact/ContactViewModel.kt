package com.quyt.mqttchat.presentation.feature.home.contact

import androidx.lifecycle.viewModelScope
import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.domain.usecase.contact.GetListContactUseCase
import com.quyt.mqttchat.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

sealed class ContactState {
    object Loading : ContactState()
    data class Success(val data: List<User>) : ContactState()
    data class Error(val error: String) : ContactState()
}

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val getListContactUseCase: GetListContactUseCase
) : BaseViewModel<ContactState>() {

    fun getListContact() {
        uiState.postValue(ContactState.Loading)
        viewModelScope.launch {
            when (val res = getListContactUseCase()) {
                is Result.Success -> {
                    uiState.postValue(ContactState.Success(res.data))
                }

                is Result.Error -> {
                    uiState.postValue(ContactState.Error(res.exception.message!!))
                }
            }
        }
    }
}
