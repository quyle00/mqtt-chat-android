package com.quyt.mqttchat.presentation.feature.home.message

import androidx.lifecycle.viewModelScope
import com.quyt.mqttchat.domain.model.Conversation
import com.quyt.mqttchat.domain.model.EventType
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.usecase.conversation.GetListConversationUseCase
import com.quyt.mqttchat.domain.usecase.conversation.ListenConversationEventUseCase
import com.quyt.mqttchat.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ConversationListState {
    object Loading : ConversationListState()
    data class Success(val data: List<Conversation>) : ConversationListState()
    data class Error(val error: String) : ConversationListState()
    data class NewMessage(val message: Message?) : ConversationListState()
}

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val getListConversationUseCase: GetListConversationUseCase,
    private val listenConversationEventUseCase: ListenConversationEventUseCase
) : BaseViewModel<ConversationListState>() {
    private fun subscribeNewMessage() {
        viewModelScope.launch {
            listenConversationEventUseCase {
                if (it.type == EventType.NEW_MESSAGE.value) {
                    updateLastMessage(it.message)
                }
            }
        }
    }

    fun updateLastMessage(message: Message?) {
        uiState.postValue(ConversationListState.NewMessage(message))
    }

    fun getListConversation() {
        viewModelScope.launch {
            uiState.postValue(ConversationListState.Loading)
            when (val result = getListConversationUseCase()) {
                is Result.Success -> {
                    uiState.postValue(ConversationListState.Success(result.data))
                    subscribeNewMessage()
                }

                is Result.Error -> {
                    uiState.postValue(ConversationListState.Error(result.exception.message ?: "Error"))
                }
            }
        }
    }
}