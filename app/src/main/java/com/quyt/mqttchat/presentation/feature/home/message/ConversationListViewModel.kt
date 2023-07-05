package com.quyt.mqttchat.presentation.feature.home.message

import androidx.lifecycle.viewModelScope
import com.quyt.mqttchat.domain.model.Conversation
import com.quyt.mqttchat.domain.model.EventType
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.domain.repository.SharedPreferences
import com.quyt.mqttchat.domain.usecase.conversation.GetListConversationUseCase
import com.quyt.mqttchat.domain.usecase.conversation.ListenConversationEventUseCase
import com.quyt.mqttchat.domain.usecase.user.ListenUserStatusEventUseCase
import com.quyt.mqttchat.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

sealed class ConversationListState {
    object Loading : ConversationListState()
    data class Success(val data: List<Conversation>) : ConversationListState()
    data class Error(val error: String) : ConversationListState()
    data class NewMessage(val message: Message?) : ConversationListState()
    data class UserStatusChange(val userId: String, val isOnline: Boolean) : ConversationListState()
    data class MarkReadLastMessage(val conversationId: String) : ConversationListState()
}

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val getListConversationUseCase: GetListConversationUseCase,
    private val listenConversationEventUseCase: ListenConversationEventUseCase,
    private val listenUserStatusEventUseCase: ListenUserStatusEventUseCase,
    private val sharedPreferences: SharedPreferences
) : BaseViewModel<ConversationListState>() {
    val currentUser: User? by lazy { sharedPreferences.getCurrentUser() }
    private fun subscribeNewMessage() {
        viewModelScope.launch {
            listenConversationEventUseCase {
                if (it.type == EventType.NEW_MESSAGE.value) {
                    updateLastMessage(it.message)
                }
                if (it.type == EventType.MARK_READ.value) {
                    uiState.postValue(
                        ConversationListState.MarkReadLastMessage(it.conversationId?:"")
                    )
                }
            }
        }
    }

    fun subscribeUserStatus() {
        viewModelScope.launch {
            listenUserStatusEventUseCase{
                uiState.postValue(ConversationListState.UserStatusChange(it.id?:"", it.isOnline))
            }
        }
    }

    fun updateLastMessage(message: Message?) {
        message?.isMine = message?.sender?.id == currentUser?.id
        uiState.postValue(ConversationListState.NewMessage(message))
    }

    fun updateMarkReadMyLastMessage(conversationId: String){
        uiState.postValue(ConversationListState.MarkReadLastMessage(conversationId))
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
                    uiState.postValue(
                        ConversationListState.Error(result.exception.message ?: "Error")
                    )
                }
            }
        }
    }
}
