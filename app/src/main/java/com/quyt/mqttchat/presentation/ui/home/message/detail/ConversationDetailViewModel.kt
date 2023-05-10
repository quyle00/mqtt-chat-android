package com.quyt.mqttchat.presentation.ui.home.message.detail

import androidx.lifecycle.viewModelScope
import com.quyt.mqttchat.domain.model.Event
import com.quyt.mqttchat.domain.model.EventType
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.domain.usecase.ListenConversationEventUseCase
import com.quyt.mqttchat.domain.usecase.SendConversationEventUseCase
import com.quyt.mqttchat.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ConversationDetailState {
    object Loading : ConversationDetailState()
    data class Success(val data: ArrayList<Message>) : ConversationDetailState()
    data class Error(val error: String) : ConversationDetailState()
    data class NewMessage(val message: Message) : ConversationDetailState()
    data class Typing(val message: Message) : ConversationDetailState()
}

@HiltViewModel
class ConversationDetailViewModel @Inject constructor(
    private val sendConversationEventUseCase: SendConversationEventUseCase,
    private val listenConversationEventUseCase: ListenConversationEventUseCase,
) : BaseViewModel<ConversationDetailState>() {

    var mTyping: Boolean = false

    fun subscribeConversation(conversationId: Int) {
        viewModelScope.launch {
            listenConversationEventUseCase(conversationId) {
                when (it.type) {
                    EventType.NEW_MESSAGE.value -> {
                        uiState.postValue(ConversationDetailState.NewMessage(it.message!!))
                    }

                    EventType.TYPING.value -> {
                        uiState.postValue(ConversationDetailState.Typing(it.message!!))
                    }
                }
            }
        }
    }

    fun sendMessage(conversationId: Int, message: Message) {
        viewModelScope.launch {
            sendConversationEventUseCase(conversationId, Event().apply {
                this.type = EventType.NEW_MESSAGE.value
                this.message = message
            })
        }
    }

    fun sendTyping(user: User, conversationId: Int, isTyping: Boolean) {
        viewModelScope.launch {
            sendConversationEventUseCase(conversationId, Event().apply {
                this.type = EventType.TYPING.value
                this.message = Message().apply {
                    this.sender = user
                    this.isTyping = isTyping
                }
            })
        }
    }

}