package com.quyt.mqttchat.presentation.feature.home.message.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quyt.mqttchat.data.datasource.remote.extension.CustomException
import com.quyt.mqttchat.domain.model.Conversation
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
import com.quyt.mqttchat.domain.usecase.message.SendMarkReadEventUseCase
import com.quyt.mqttchat.domain.usecase.message.SendNewMessageEventUseCase
import com.quyt.mqttchat.domain.usecase.message.SendTypingEventUseCase
import com.quyt.mqttchat.domain.usecase.message.UpdateLocalMessageStateUseCase
import com.quyt.mqttchat.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ConversationDetailState {
    object Loading : ConversationDetailState()
    data class Success(val data: List<Message>) : ConversationDetailState()
    data class LoadMoreSuccess(val data: List<Message>) : ConversationDetailState()
    data class Error(val error: String) : ConversationDetailState()
    data class NewMessage(val message: Message) : ConversationDetailState()
    data class Typing(val message: Message) : ConversationDetailState()
    data class MarkReadMessage(val messageIds: List<String>?) : ConversationDetailState()
    data class SendMessageSuccess(val message: Message) : ConversationDetailState()
    data class SendMessageError(val message: Message, var error: String) : ConversationDetailState()
    data class NoMoreData(val message: String) : ConversationDetailState()
}

@HiltViewModel
class ConversationDetailViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val sendNewMessageEventUseCase: SendNewMessageEventUseCase,
    private val sendMarkReadEventUseCase: SendMarkReadEventUseCase,
    private val sendTypingEventUseCase: SendTypingEventUseCase,
    private val listenMessageEventUseCase: ListenMessageEventUseCase,
    private val getListMessageUseCase: GetListMessageUseCase,
    private val createMessageUseCase: CreateMessageUseCase,
    private val createConversationUseCase: CreateConversationUseCase,
    private val getConversationDetailUseCase: GetConversationDetailUseCase,
    private val seenMessageUseCase: SeenMessageUseCase,
    private val updateLocalMessageStateUseCase: UpdateLocalMessageStateUseCase
) : BaseViewModel<ConversationDetailState>() {

    val currentUser: User? by lazy { sharedPreferences.getCurrentUser() }
    private lateinit var mCurrentConversation: Conversation
    var isTyping: Boolean = false
    var mPartner = MutableLiveData<User?>()
    var mCurrentPage = 1
    var messageToReply = MutableLiveData<Message?>()

    fun getConversationDetail(conversation: Conversation?, partner: User?) {
        viewModelScope.launch {
            val result = if (conversation != null) {
                Result.Success(conversation)
            } else {
                getConversationDetailUseCase(partnerId = partner?.id ?: "")
            }
            when (result) {
                is Result.Success -> {
                    mPartner.postValue(result.data.participants.getPartner())
                    mCurrentConversation = result.data
                    getListMessage(mCurrentPage)
                    subscribeConversation()
                }

                is Result.Error -> {
                    uiState.postValue(
                        ConversationDetailState.Error(result.exception.message ?: "Error")
                    )
                    if ((result.exception as CustomException).code == 422) {
                        mPartner.postValue(partner)
                    }
                }
            }
        }
    }

    fun getListMessage(page: Int) {
        viewModelScope.launch {
            uiState.postValue(ConversationDetailState.Loading)
            val result = getListMessageUseCase(
                mCurrentConversation.id ?: "",
                page,
                mCurrentConversation.lastMessage?.id
            )
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
                        val unSeenMessage = getUnseenMessageIds(listMessage)
                        if (unSeenMessage.isNotEmpty()) {
                            markReadMessage(unSeenMessage)
                        }
                    } else {
                        uiState.postValue(ConversationDetailState.LoadMoreSuccess(listMessage))
                    }
                }

                is Result.Error -> {
                    uiState.postValue(
                        ConversationDetailState.Error(result.exception.message ?: "Error")
                    )
                }
            }
        }
    }

    private suspend fun markReadMessage(unSeenMessageIds: List<String>) {
        val result = seenMessageUseCase(mCurrentConversation.id ?: "", unSeenMessageIds)
        when (result) {
            is Result.Success -> {
                sendMarkReadEventUseCase(
                    mCurrentConversation.id ?: "",
                    mPartner.value?.id ?: "",
                    unSeenMessageIds
                )
            }

            is Result.Error -> {
                uiState.postValue(
                    ConversationDetailState.Error(result.exception.message ?: "Error")
                )
            }
        }
    }

    private suspend fun subscribeConversation() {
        listenMessageEventUseCase(mCurrentConversation.id ?: "") {
            if (it.publisherId == currentUser?.id) return@listenMessageEventUseCase
            when (it.type) {
                EventType.NEW_MESSAGE.value -> {
                    uiState.postValue(ConversationDetailState.NewMessage(it.message!!))
                    //
                    val unSeenMessage = arrayListOf((it.message?.id ?: ""))
                    viewModelScope.launch {
                        markReadMessage(unSeenMessage)
                    }
                }

                EventType.TYPING.value -> {
                    uiState.postValue(ConversationDetailState.Typing(it.message!!))
                }

                EventType.MARK_READ.value -> {
                    if (!it.messageIds.isNullOrEmpty()) {
                        viewModelScope.launch {
                            updateLocalMessageStateUseCase(it.messageIds!!, MessageState.SEEN.value)
                        }
                        uiState.postValue(ConversationDetailState.MarkReadMessage(it.messageIds))
                    }
                }
            }
        }
    }

    private suspend fun createConversation(): Conversation? {
        return withContext(Dispatchers.IO) {
            val userIds = arrayListOf<String>().apply {
                add(currentUser?.id ?: "")
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
            if (mCurrentConversation.id.isNullOrEmpty()) {
                mCurrentConversation = createConversation() ?: return@launch
            }
            message.reply = messageToReply.value
            messageToReply.postValue(null)
            val result = createMessageUseCase(mCurrentConversation.id ?: "", message)
            when (result) {
                is Result.Success -> {
                    val messageCreated = result.data
                    uiState.postValue(ConversationDetailState.SendMessageSuccess(messageCreated))
                    // Send Mqtt event
                    sendNewMessageEventUseCase(
                        mCurrentConversation.id ?: "",
                        mPartner.value?.id ?: "",
                        result.data
                    )
                }

                is Result.Error -> {
                    uiState.postValue(
                        ConversationDetailState.SendMessageError(
                            message,
                            result.exception.message ?: "Error"
                        )
                    )
                }
            }
        }
    }

    fun sendTyping(isTyping: Boolean) {
        if (!::mCurrentConversation.isInitialized) return
        viewModelScope.launch {
            sendTypingEventUseCase(
                mCurrentConversation.id ?: "",
                mPartner.value?.id ?: "",
                isTyping
            )
        }
    }

    fun setReplyMessage(message: Message? = null) {
        message?.reply = null
        messageToReply.postValue(message)
    }

    fun onCloseReplyMessage() {
        messageToReply.postValue(null)
    }

    private fun getUnseenMessageIds(listMessage: List<Message>?): List<String> {
        if (listMessage.isNullOrEmpty()) return listOf()
        return listMessage.filter { message ->
            message.state == MessageState.SENT.value && message.sender?.id != currentUser?.id
        }.map { message -> message.id }
    }

    private fun List<User>?.getPartner(): User? {
        return this?.firstOrNull { user -> user.id != currentUser?.id }
    }
}
