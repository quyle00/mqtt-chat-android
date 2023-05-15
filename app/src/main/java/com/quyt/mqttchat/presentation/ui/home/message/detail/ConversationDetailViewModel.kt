package com.quyt.mqttchat.presentation.ui.home.message.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quyt.mqttchat.data.datasource.remote.extension.CustomException
import com.quyt.mqttchat.domain.model.Conversation
import com.quyt.mqttchat.domain.model.Event
import com.quyt.mqttchat.domain.model.EventType
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.MessageState
import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.domain.repository.SharedPreferences
import com.quyt.mqttchat.domain.usecase.conversation.CreateConversationUseCase
import com.quyt.mqttchat.domain.usecase.conversation.GetConversationDetailUseCase
import com.quyt.mqttchat.domain.usecase.message.CreateMessageUseCase
import com.quyt.mqttchat.domain.usecase.message.GetListMessageUseCase
import com.quyt.mqttchat.domain.usecase.message.ListenMessageEventUseCase
import com.quyt.mqttchat.domain.usecase.message.SeenMessageUseCase
import com.quyt.mqttchat.domain.usecase.message.SendMessageEventUseCase
import com.quyt.mqttchat.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class ConversationDetailState {
    object Loading : ConversationDetailState()
    data class Success(val data: List<Message>) : ConversationDetailState()
    data class Error(val error: String) : ConversationDetailState()
    data class NewMessage(val message: Message) : ConversationDetailState()
    data class Typing(val message: Message) : ConversationDetailState()
    object SeenMessage : ConversationDetailState()
    data class SendMessageSuccess(val message: Message) : ConversationDetailState()
    data class SendMessageError(val message: Message, var error: String) : ConversationDetailState()
}

@HiltViewModel
class ConversationDetailViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val sendConversationEventUseCase: SendMessageEventUseCase,
    private val listenMessageEventUseCase: ListenMessageEventUseCase,
    private val getListMessageUseCase: GetListMessageUseCase,
    private val createMessageUseCase: CreateMessageUseCase,
    private val createConversationUseCase: CreateConversationUseCase,
    private val getConversationDetailUseCase: GetConversationDetailUseCase,
    private val seenMessageUseCase: SeenMessageUseCase
) : BaseViewModel<ConversationDetailState>() {

    var isTyping: Boolean = false
    private var currentConversation: Conversation? = null
    var shouldCreateConversation = false
    var partner = MutableLiveData<User>()
    var mPartnerID = ""
    fun getConversationDetail(conversationId: String?, partnerId: String?) {
        viewModelScope.launch {
            getConversationDetailUseCase(conversationId, partnerId).let {
                when (it) {
                    is Result.Success -> {
                        partner.postValue(it.data.participants?.firstOrNull { participant ->
                            participant.id != getCurrentUser()?.id
                        })
                        currentConversation = it.data
                        getListMessage(currentConversation?.id ?: "")
                        subscribeConversation(currentConversation?.id ?: "")
                    }

                    is Result.Error -> {
                        uiState.postValue(ConversationDetailState.Error(it.exception.message ?: "Error"))
                        if ((it.exception as CustomException).code == 422) {
                            shouldCreateConversation = true
                            mPartnerID = partnerId ?: ""
                        }
                    }
                }
            }
        }
    }

    private fun getListMessage(conversationId: String) {
        viewModelScope.launch {
            uiState.postValue(ConversationDetailState.Loading)
            getListMessageUseCase(conversationId).let {
                when (it) {
                    is Result.Success -> {
                        uiState.postValue(ConversationDetailState.Success(it.data))
                        val unSeenMessage = it.data.filter { message ->
                            message.state == MessageState.SENT.value
                                    && message.sender?.id != getCurrentUser()?.id
                        }
                        if (unSeenMessage.isNotEmpty()) {
                            seenMessage(conversationId, unSeenMessage.map { message -> message.id ?: "" })
                        }
                    }

                    is Result.Error -> {
                        uiState.postValue(ConversationDetailState.Error(it.exception.message ?: "Error"))
                    }
                }
            }
        }
    }

    private suspend fun seenMessage(conversationId: String, messageIds: List<String>) {
        when (val result = seenMessageUseCase(conversationId, messageIds)) {
            is Result.Success -> {
                sendConversationEventUseCase(
                    partner.value?.id ?: "",
                    currentConversation?.id ?: "", Event(
                        getCurrentUser()?.id,
                        EventType.SEEN.value,
                        null,
                    )
                )
            }

            is Result.Error -> {
                uiState.postValue(ConversationDetailState.Error(result.exception.message ?: "Error"))
            }
        }

    }

    private fun subscribeConversation(conversationId: String) {
        viewModelScope.launch {
            listenMessageEventUseCase(conversationId) {
                when (it.type) {
                    EventType.NEW_MESSAGE.value -> {
                        if (it.publisherId == getCurrentUser()?.id) return@listenMessageEventUseCase
                        uiState.postValue(ConversationDetailState.NewMessage(it.message!!))
                        //
                        val unSeenMessage = arrayListOf(it.message?.id ?: "")
                        viewModelScope.launch {
                            seenMessage(conversationId, unSeenMessage)
                        }
                    }

                    EventType.TYPING.value -> {
                        if (it.publisherId == getCurrentUser()?.id) return@listenMessageEventUseCase
                        uiState.postValue(ConversationDetailState.Typing(it.message!!))
                    }

                    EventType.SEEN.value -> {
                        if (it.publisherId == getCurrentUser()?.id) return@listenMessageEventUseCase
                        uiState.postValue(ConversationDetailState.SeenMessage)
                    }
                }
            }
        }
    }

    private suspend fun createConversation(): Conversation? {
        return withContext(Dispatchers.IO) {
            val userIds = arrayListOf<String>().apply {
                add(getCurrentUser()?.id ?: "")
                add(mPartnerID)
            }
            createConversationUseCase(userIds).let {
                when (it) {
                    is Result.Success -> {
                        it.data
                    }

                    is Result.Error -> {
                        null
                    }
                }

            }
        }
    }

    fun sendMessage(message: Message) {
        viewModelScope.launch {
            if (shouldCreateConversation) {
                currentConversation = createConversation()
                shouldCreateConversation = false
            }
            message.state = MessageState.SENT.value
            val result = createMessageUseCase(currentConversation?.id ?: "", message)
            when (result) {
                is Result.Success -> {
                    val messageCreated = result.data
                    uiState.postValue(ConversationDetailState.SendMessageSuccess(messageCreated))
                    // Send Mqtt event
                    sendConversationEventUseCase(
                        partner.value?.id ?: "",
                        currentConversation?.id ?: "",
                        Event(getCurrentUser()?.id, EventType.NEW_MESSAGE.value, result.data)
                    )
                }

                is Result.Error -> {
                    uiState.postValue(ConversationDetailState.SendMessageError(message, result.exception.message ?: "Error"))
                }
            }
        }
    }

    fun sendTyping(isTyping: Boolean) {
        if (currentConversation == null) return
        viewModelScope.launch {
            sendConversationEventUseCase(
                partner.value?.id ?: "",
                currentConversation?.id ?: "", Event(
                    getCurrentUser()?.id,
                    EventType.TYPING.value,
                    Message().apply {
                        this.sender = getCurrentUser()
                        this.isTyping = isTyping
                    }
                ))
        }
    }

    fun getCurrentUser() = sharedPreferences.getCurrentUser()


}