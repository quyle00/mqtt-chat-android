package com.quyt.mqttchat.data.repository

import com.google.gson.Gson
import com.quyt.mqttchat.data.datasource.local.MessageLocalDataSource
import com.quyt.mqttchat.data.datasource.remote.extension.getError
import com.quyt.mqttchat.data.datasource.remote.service.MessageService
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.repository.MessageRepository
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class MessageRepositoryImpl(
    private val messageService: MessageService,
    private val messageLocalDatasource: MessageLocalDataSource
) : MessageRepository {
    override suspend fun getListMessage(
        conversationId: String,
        page: Int,
        lastMessageId: String?
    ): Result<List<Message>> {
        return try {
            if (page == 1) {
//                if (lastMessageId != null) {
                    val localLatestMessage = messageLocalDatasource.getLatestMessage(conversationId)
                    if (localLatestMessage != null && localLatestMessage.id != lastMessageId) {
                        messageLocalDatasource.clearMessage(conversationId)
                    }
//                }
            }

            val localMessage = messageLocalDatasource.getMessageByPage(conversationId, page)
            if (localMessage.isNotEmpty()) {
                Result.Success(localMessage)
            } else {
                val res = messageService.getListMessage(conversationId, page)
                if (res.isSuccessful) {
                    val remoteMessages = res.body()?.data?.data ?: listOf()
                    if (remoteMessages.isNotEmpty()) {
                        messageLocalDatasource.insertMessage(remoteMessages)
                        Result.Success(res.body()?.data?.data ?: listOf())
                    } else {
                        Result.Success(listOf())
                    }
                } else {
                    Result.Error(res.getError())
                }
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun createMessage(conversationId: String, message: Message): Result<Message> {
        return try {
            val res = if (message.images?.isNotEmpty() == true) {
                val files = mutableListOf<MultipartBody.Part>()
                message.images?.forEach { image ->
                    val file = File(image)
                    val requestFile = RequestBody.create(MediaType.parse("image/*"), file)
                    val part = MultipartBody.Part.createFormData("images", file.name, requestFile)
                    files.add(part)
                }
                val messageRequestBody = RequestBody.create(
                    MediaType.parse("text/json"),
                    Gson().toJson(message)
                )
                messageService.createMessage2(conversationId, messageRequestBody, files)
            } else {
                messageService.createMessage(conversationId, message)
            }
            if (res.isSuccessful) {
                Result.Success(res.body()?.data!!)
            } else {
                Result.Error(res.getError())
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun insertMessage(messageList: List<Message>) {
        messageLocalDatasource.insertMessage(messageList)
    }

    override suspend fun updateSeenMessage(
        conversationId: String,
        messageIds: List<String>
    ): Result<String> {
        return try {
            val res = messageService.updateSeenMessage(conversationId, messageIds)
            if (res.isSuccessful) {
                Result.Success(res.body()?.data!!)
            } else {
                Result.Error(res.getError())
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateLocalMessageState(messageIds: List<String>, newState: Int) {
        messageLocalDatasource.updateMessageState(messageIds, newState)
    }

    override suspend fun updateMessage(message: Message, shouldUpdateRemote: Boolean): Result<Message> {
        return try {
            if (shouldUpdateRemote) {
                val res = messageService.updateMessage(message.conversation ?: "", message)
                if (res.isSuccessful) {
                    messageLocalDatasource.updateMessage(res.body()?.data!!)
                    Result.Success(res.body()?.data!!)
                } else {
                    Result.Error(res.getError())
                }
            } else {
                messageLocalDatasource.updateMessage(message)
                Result.Success(message)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteMessage(message: Message, shouldDeleteRemote: Boolean): Result<String> {
        return try {
            if (shouldDeleteRemote) {
                val res = messageService.deleteMessage(message.conversation ?: "", message.id ?: "")
                if (res.isSuccessful) {
                    messageLocalDatasource.deleteMessage(message)
                    Result.Success("Success")
                } else {
                    Result.Error(res.getError())
                }
            } else {
                messageLocalDatasource.deleteMessage(message)
                Result.Success("Success")
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
