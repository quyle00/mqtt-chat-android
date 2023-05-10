package com.quyt.mqttchat.presentation.ui.home.message.detail

import androidx.lifecycle.viewModelScope
import com.quyt.mqttchat.domain.model.Event
import com.quyt.mqttchat.domain.model.EventType
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.domain.repository.SharedPreferences
import com.quyt.mqttchat.domain.usecase.ListenConversationEventUseCase
import com.quyt.mqttchat.domain.usecase.SendConversationEventUseCase
import com.quyt.mqttchat.domain.usecase.message.CreateMessageUseCase
import com.quyt.mqttchat.domain.usecase.message.GetListMessageUseCase
import com.quyt.mqttchat.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ConversationDetailState {
    object Loading : ConversationDetailState()
    data class Success(val data: List<Message>) : ConversationDetailState()
    data class Error(val error: String) : ConversationDetailState()
    data class NewMessage(val message: Message) : ConversationDetailState()
    data class Typing(val message: Message) : ConversationDetailState()
    data class SendMessageSuccess(val message: Message) : ConversationDetailState()
    data class SendMessageError(val message: Message,var error: String) : ConversationDetailState()
}

@HiltViewModel
class ConversationDetailViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val sendConversationEventUseCase: SendConversationEventUseCase,
    private val listenConversationEventUseCase: ListenConversationEventUseCase,
    private val getListMessageUseCase: GetListMessageUseCase,
    private val createMessageUseCase: CreateMessageUseCase
) : BaseViewModel<ConversationDetailState>() {

    var mTyping: Boolean = false

    fun getListMessage(conversationId: String) {
        viewModelScope.launch {
            uiState.postValue(ConversationDetailState.Loading)
            getListMessageUseCase(conversationId).let {
                when (it) {
                    is Result.Success -> {
                        uiState.postValue(ConversationDetailState.Success(it.data))
                    }

                    is Result.Error -> {
                        uiState.postValue(ConversationDetailState.Error(it.exception.message ?: "Error"))
                    }
                }
            }
        }
    }

    fun subscribeConversation(conversationId: String) {
        viewModelScope.launch {
            listenConversationEventUseCase(conversationId) {
                when (it.type) {
                    EventType.NEW_MESSAGE.value -> {
                        if (it.publisherId == getCurrentUser()?.id) return@listenConversationEventUseCase
                        uiState.postValue(ConversationDetailState.NewMessage(it.message!!))
                    }

                    EventType.TYPING.value -> {
                        if (it.publisherId == getCurrentUser()?.id) return@listenConversationEventUseCase
                        uiState.postValue(ConversationDetailState.Typing(it.message!!))
                    }
                }
            }
        }
    }

    fun sendMessage(conversationId: String, message: Message) {
        val maxRetryCount = 3
        var retryCount = 0
        viewModelScope.launch {
            retrySendMessage(conversationId, message, maxRetryCount)
        }
    }

    private suspend fun retrySendMessage(conversationId: String, message: Message, maxRetryCount: Int) {
        var retryCount = 0

        while (retryCount < maxRetryCount) {
            val result = createMessageUseCase(conversationId, message)
            delay(3000)
            when (result) {
                is Result.Success -> {
                    uiState.postValue(ConversationDetailState.SendMessageSuccess(result.data))
                    // Send Mqtt event
                    sendConversationEventUseCase(conversationId, Event(getCurrentUser()?.id, EventType.NEW_MESSAGE.value, message))
                    return // Kết thúc coroutine khi thành công
                }
                is Result.Error -> {
                    retryCount++
                    if (retryCount >= maxRetryCount) {
                        uiState.postValue(ConversationDetailState.SendMessageError(message, result.exception.message ?: "Error"))
                        return // Kết thúc coroutine khi gặp lỗi và không thể retry nữa
                    }
                }
            }
        }
    }


    fun sendTyping(user: User?, conversationId: String, isTyping: Boolean) {
        viewModelScope.launch {
            sendConversationEventUseCase(conversationId, Event(
                getCurrentUser()?.id,
                EventType.TYPING.value,
                Message().apply {
                    this.sender = user
                    this.isTyping = isTyping
                }
            ))
        }
    }

    fun createMessage(conversationId: String, message: Message) {
        viewModelScope.launch {
            createMessageUseCase(conversationId, message)
        }
    }

    fun getCurrentUser() = sharedPreferences.getCurrentUser()

}