package com.quyt.mqttchat.presentation.feature.home.message.detail

import android.util.Log
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
import com.quyt.mqttchat.domain.usecase.message.realTime.ClearRetainMessageEventUseCase
import com.quyt.mqttchat.domain.usecase.message.CreateMessageUseCase
import com.quyt.mqttchat.domain.usecase.message.DeleteMessageUseCase
import com.quyt.mqttchat.domain.usecase.message.GetListMessageUseCase
import com.quyt.mqttchat.domain.usecase.message.realTime.ListenMessageEventUseCase
import com.quyt.mqttchat.domain.usecase.message.SeenMessageUseCase
import com.quyt.mqttchat.domain.usecase.message.realTime.SendDeleteMessageEventUseCase
import com.quyt.mqttchat.domain.usecase.message.realTime.SendEditMessageEventUseCase
import com.quyt.mqttchat.domain.usecase.message.realTime.SendMarkReadEventUseCase
import com.quyt.mqttchat.domain.usecase.message.realTime.SendNewMessageEventUseCase
import com.quyt.mqttchat.domain.usecase.message.realTime.SendTypingEventUseCase
import com.quyt.mqttchat.domain.usecase.message.realTime.UnsubscribeMessageEventUseCase
import com.quyt.mqttchat.domain.usecase.message.UpdateLocalMessageStateUseCase
import com.quyt.mqttchat.domain.usecase.message.UpdateMessageUseCase
import com.quyt.mqttchat.domain.usecase.user.ListenUserStatusEventUseCase
import com.quyt.mqttchat.presentation.base.BaseViewModel
import com.quyt.mqttchat.utils.DateUtils
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
    data class PartnerMarkReadMessage(val messageIds: List<String>?) : ConversationDetailState()
    data class SendMarkReadMessageSuccess(val conversationId: String) : ConversationDetailState()
    data class SendMessageSuccess(val message: Message) : ConversationDetailState()
    data class SendMessageError(val message: Message, var error: String) : ConversationDetailState()
    data class NoMoreData(val message: String) : ConversationDetailState()
    data class EditMessageSuccess(val message: Message) : ConversationDetailState()
    data class DeleteMessageSuccess(val message: Message) : ConversationDetailState()
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
    private val updateLocalMessageStateUseCase: UpdateLocalMessageStateUseCase,
    private val updateMessageUseCase: UpdateMessageUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val sendEditMessageEventUseCase: SendEditMessageEventUseCase,
    private val sendDeleteMessageEventUseCase: SendDeleteMessageEventUseCase,
    private val unsubscribeMessageEventUseCase: UnsubscribeMessageEventUseCase,
    private val clearRetainMessageEventUseCase: ClearRetainMessageEventUseCase,
    private val listenUserStatusEventUseCase: ListenUserStatusEventUseCase
) : BaseViewModel<ConversationDetailState>() {

    val currentUser: User? by lazy { sharedPreferences.getCurrentUser() }
    private var mCurrentConversation: Conversation? = null
    var isTyping: Boolean = false
    var mPartner = MutableLiveData<User?>()
    var mCurrentPage = 1
    var messageToReply: Message? = null
    var messageToEdit: Message? = null
    var messageEditorTitle = MutableLiveData<String?>()
    var messageEditorContent = MutableLiveData<String?>()
    var messageInputValue = MutableLiveData<String?>()
    var isEditing = MutableLiveData<Boolean>()

    fun getConversationDetail(conversationId: String?, partner: User?) {
        viewModelScope.launch {
            val result = if (conversationId != null) {
                getConversationDetailUseCase(conversationId = conversationId)
            } else {
                getConversationDetailUseCase(partnerId = partner?.id ?: "")
            }
            when (result) {
                is Result.Success -> {
                    mPartner.postValue(result.data.participants.getPartner())
                    mCurrentConversation = result.data
                    getListMessage(mCurrentPage)
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
                mCurrentConversation?.id ?: "",
                page,
                mCurrentConversation?.lastMessage?.id
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
                    subscribeConversation()
                    subscribeUserStatus()
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
        val result = seenMessageUseCase(mCurrentConversation?.id ?: "", unSeenMessageIds)
        when (result) {
            is Result.Success -> {
                sendMarkReadEventUseCase(
                    mCurrentConversation?.id ?: "",
                    mPartner.value?.id ?: "",
                    unSeenMessageIds
                )
                updateLocalMessageStateUseCase(unSeenMessageIds, MessageState.SEEN.value)
                uiState.postValue(
                    ConversationDetailState.SendMarkReadMessageSuccess(mCurrentConversation?.id ?: "")
                )
            }

            is Result.Error -> {
                uiState.postValue(
                    ConversationDetailState.Error(result.exception.message ?: "Error")
                )
            }
        }
    }

    private suspend fun subscribeUserStatus() {
        listenUserStatusEventUseCase(mPartner.value?.id ?: "") {
            val user = mPartner.value
            user?.isOnline = it.isOnline
            user?.lastSeen = it.lastSeen
            mPartner.postValue(user)
        }
    }

    private suspend fun subscribeConversation() {
        listenMessageEventUseCase(mCurrentConversation?.id ?: "") {
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
                        uiState.postValue(ConversationDetailState.PartnerMarkReadMessage(it.messageIds))
                    }
                }

                EventType.EDIT.value -> {
                    viewModelScope.launch {
                        val result = updateMessageUseCase(it.message!!, false)
                        if (result is Result.Success) {
                            uiState.postValue(ConversationDetailState.EditMessageSuccess(it.message!!))
                            clearRetainMessageEventUseCase(
                                mCurrentConversation?.id ?: "",
                                currentUser?.id ?: ""
                            )
                        }
                    }
                }

                EventType.DELETE.value -> {
                    viewModelScope.launch {
                        val result = deleteMessageUseCase(it.message!!, false)
                        if (result is Result.Success) {
                            uiState.postValue(ConversationDetailState.DeleteMessageSuccess(it.message!!))
                            clearRetainMessageEventUseCase(
                                mCurrentConversation?.id ?: "",
                                currentUser?.id ?: ""
                            )
                        }
                    }
                }
            }
        }
    }

    suspend fun unsubscribeConversation() {
        unsubscribeMessageEventUseCase(mCurrentConversation?.id ?: "")
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
            if (mCurrentConversation?.id.isNullOrEmpty()) {
                mCurrentConversation = createConversation() ?: return@launch
            }
            message.reply = messageToReply
            onCloseEditorMessage()
            val result = createMessageUseCase(mCurrentConversation?.id ?: "", message)
            when (result) {
                is Result.Success -> {
                    val messageCreated = result.data
                    uiState.postValue(ConversationDetailState.SendMessageSuccess(messageCreated))
                    // Send Mqtt event
                    sendNewMessageEventUseCase(
                        mCurrentConversation?.id ?: "",
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

    fun editMessage() {
        if (messageToEdit == null) return
        messageToEdit?.content = messageInputValue.value ?: ""
        viewModelScope.launch {
            val result = updateMessageUseCase(messageToEdit!!, true)
            onCloseEditorMessage()
            when (result) {
                is Result.Success -> {
                    uiState.postValue(ConversationDetailState.EditMessageSuccess(result.data))
                    //Send Mqtt event
                    sendEditMessageEventUseCase(
                        mCurrentConversation?.id ?: "",
                        mPartner.value?.id ?: "",
                        result.data
                    )
                }

                is Result.Error -> {

                }
            }
        }
    }

    fun deleteMessage(message: Message) {
        viewModelScope.launch {
            val result = deleteMessageUseCase(message, true)
            when (result) {
                is Result.Success -> {
                    uiState.postValue(ConversationDetailState.DeleteMessageSuccess(message))
                    //Send Mqtt event
                    sendDeleteMessageEventUseCase(
                        mCurrentConversation?.id ?: "",
                        mPartner.value?.id ?: "",
                        message
                    )
                }

                is Result.Error -> {

                }
            }
        }
    }

    fun sendTyping(isTyping: Boolean) {
        if (mCurrentConversation?.id.isNullOrEmpty()) return
        viewModelScope.launch {
            sendTypingEventUseCase(
                mCurrentConversation?.id ?: "",
                mPartner.value?.id ?: "",
                isTyping
            )
        }
    }

    fun setReplyMessage(message: Message? = null) {
        isEditing.postValue(false)
        message?.reply = null
        messageToReply = message
        messageEditorTitle.postValue(message?.sender?.fullname)
        messageEditorContent.postValue(message?.content)
    }

    fun onCloseEditorMessage() {
        messageToReply = null
        messageEditorTitle.postValue(null)
        messageEditorContent.postValue(null)
        if (isEditing.value == true) {
            isEditing.postValue(false)
            messageInputValue.postValue("")
        }
    }

    fun setEditMessage(message: Message?) {
        isEditing.postValue(true)
        messageToEdit = message
        messageEditorTitle.postValue("Edit message")
        messageEditorContent.postValue(message?.content)
        messageInputValue.postValue(message?.content)
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
