package com.quyt.mqttchat.presentation.feature.home.message.detail

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
import com.quyt.mqttchat.domain.usecase.message.InsertMessageUseCase
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
    data class LoadMoreSuccess(val data: List<Message>) : ConversationDetailState()
    data class Error(val error: String) : ConversationDetailState()
    data class NewMessage(val message: Message) : ConversationDetailState()
    data class Typing(val message: Message) : ConversationDetailState()
    object SeenMessage : ConversationDetailState()
    data class SendMessageSuccess(val message: Message) : ConversationDetailState()
    data class SendMessageError(val message: Message, var error: String) : ConversationDetailState()
    data class NoMoreData(val message: String) : ConversationDetailState()
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
    private val seenMessageUseCase: SeenMessageUseCase,
    private val insertMessageUseCase: InsertMessageUseCase
) : BaseViewModel<ConversationDetailState>() {

    lateinit var mCurrentConversation: Conversation
    var isTyping: Boolean = false
    var shouldCreateConversation = false
    var mPartner = MutableLiveData<User?>()
    var mCurrentPage = 1

    fun getConversationDetail(conversation: Conversation?, partner: User?) {
        viewModelScope.launch {
            val result = if (conversation != null) {
                Result.Success(conversation)
            } else {
                getConversationDetailUseCase(partnerId = partner?.id ?: "")
            }
            when (result) {
                is Result.Success -> {
                    mPartner.postValue(result.data.participants?.firstOrNull { participant ->
                        participant.id != getCurrentUser()?.id
                    })
                    mCurrentConversation = result.data
                    getListMessage(mCurrentPage)
                    subscribeConversation()
                }

                is Result.Error -> {
                    uiState.postValue(ConversationDetailState.Error(result.exception.message ?: "Error"))
                    if ((result.exception as CustomException).code == 422) {
                        shouldCreateConversation = true
                        mPartner.postValue(partner)
                    }
                }
            }
        }
    }

    fun getListMessage(page: Int) {
        viewModelScope.launch {
            uiState.postValue(ConversationDetailState.Loading)
            val result = getListMessageUseCase(mCurrentConversation.id ?: "", page,mCurrentConversation.lastMessage?.id)
            when (result) {
                is Result.Success -> {
                    val listMessage = result.data
                    if (listMessage.isEmpty()) {
                        uiState.postValue(ConversationDetailState.NoMoreData("No more data"))
                        if (mCurrentPage > 1) {
                            mCurrentPage--
                        }
                        return@launch
                    }
                    if (mCurrentPage == 1) {
                        uiState.postValue(ConversationDetailState.Success(listMessage))
                        val unSeenMessage = getUnseenMessage(listMessage)
                        if (unSeenMessage.isNotEmpty()) {
                            seenMessage(unSeenMessage.map { message -> message.id })
                        }
                    } else {
                        uiState.postValue(ConversationDetailState.LoadMoreSuccess(listMessage))
                    }
                }

                is Result.Error -> {
                    uiState.postValue(ConversationDetailState.Error(result.exception.message ?: "Error"))
                }
            }
        }
    }

    private fun getUnseenMessage(listMessage: List<Message>?): List<Message> {
        if (listMessage.isNullOrEmpty()) return listOf()
        return listMessage.filter { message ->
            message.state == MessageState.SENT.value && message.sender?.id != getCurrentUser()?.id
        }
    }

    private suspend fun seenMessage(unSeenMessageIds: List<String>) {
        val result = seenMessageUseCase(mCurrentConversation.id ?: "", unSeenMessageIds)
        when (result) {
            is Result.Success -> {
                sendConversationEventUseCase(
                    mPartner.value?.id ?: "",
                    mCurrentConversation.id ?: "", Event(
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

    private suspend fun subscribeConversation() {
        listenMessageEventUseCase(mCurrentConversation.id ?: "") {
            when (it.type) {
                EventType.NEW_MESSAGE.value -> {
                    if (it.publisherId == getCurrentUser()?.id) return@listenMessageEventUseCase
                    uiState.postValue(ConversationDetailState.NewMessage(it.message!!))
                    //
                    val unSeenMessage = arrayListOf(it.message?.id ?: "")
                    viewModelScope.launch {
                        seenMessage(unSeenMessage)
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

    private suspend fun createConversation(): Conversation? {
        return withContext(Dispatchers.IO) {
            val userIds = arrayListOf<String>().apply {
                add(getCurrentUser()?.id ?: "")
                add(mPartner.value?.id ?: "")
            }
            val result = createConversationUseCase(userIds)
            when (result) {
                is Result.Success -> {
                    result.data
                }

                is Result.Error -> {
                    null
                }
            }
        }
    }

    fun sendMessage(message: Message) {
        viewModelScope.launch {
            if (shouldCreateConversation) {
                mCurrentConversation = createConversation() ?: return@launch
                shouldCreateConversation = false
            }
            message.state = MessageState.SENT.value
            val result = createMessageUseCase(mCurrentConversation.id ?: "", message)
            when (result) {
                is Result.Success -> {
                    val messageCreated = result.data
                    uiState.postValue(ConversationDetailState.SendMessageSuccess(messageCreated))
                    // Send Mqtt event
                    sendConversationEventUseCase(
                        mPartner.value?.id ?: "",
                        mCurrentConversation.id ?: "",
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
        if (!::mCurrentConversation.isInitialized) return
        viewModelScope.launch {
            sendConversationEventUseCase(
                mPartner.value?.id ?: "",
                mCurrentConversation.id ?: "", Event(
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