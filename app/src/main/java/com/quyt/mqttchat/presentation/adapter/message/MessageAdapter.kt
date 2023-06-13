package com.quyt.mqttchat.presentation.adapter.message

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemLoadingBinding
import com.quyt.mqttchat.databinding.ItemMyImageMessageBinding
import com.quyt.mqttchat.databinding.ItemMyMessageBinding
import com.quyt.mqttchat.databinding.ItemOtherImageMessageBinding
import com.quyt.mqttchat.databinding.ItemOtherMessageBinding
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.MessageState
import com.quyt.mqttchat.presentation.adapter.message.viewHolder.MyImageMessageViewHolder
import com.quyt.mqttchat.presentation.adapter.message.viewHolder.MyMessageViewHolder
import com.quyt.mqttchat.presentation.adapter.message.viewHolder.OtherImageMessageViewHolder
import com.quyt.mqttchat.presentation.adapter.message.viewHolder.OtherMessageViewHolder


interface OnMessageClickListener {
    fun onMediaClick(imageView : ImageView, url: String?)
    fun onMessageLongClick(message: Message?, position: Int)
}

class MessageAdapter(private val currentUserId: String?,private val listener : OnMessageClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mListMessage = mutableListOf<Message?>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            MessageType.MY_MESSAGE.value -> {
                val binding = DataBindingUtil.inflate<ItemMyMessageBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.item_my_message,
                    parent,
                    false
                )
                MyMessageViewHolder(binding,listener)
            }

            MessageType.OTHERS_MESSAGE.value -> {
                val binding = DataBindingUtil.inflate<ItemOtherMessageBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.item_other_message,
                    parent,
                    false
                )
                OtherMessageViewHolder(binding)
            }

            MessageType.LOADING.value -> {
                val binding = DataBindingUtil.inflate<ItemLoadingBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.item_loading,
                    parent,
                    false
                )
                LoadingViewHolder(binding)
            }

            MessageType.MY_IMAGE.value -> {
                val binding = DataBindingUtil.inflate<ItemMyImageMessageBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.item_my_image_message,
                    parent,
                    false
                )
                MyImageMessageViewHolder(binding,listener)
            }

            MessageType.OTHERS_IMAGE.value -> {
                val binding = DataBindingUtil.inflate<ItemOtherImageMessageBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.item_other_image_message,
                    parent,
                    false
                )
                OtherImageMessageViewHolder(binding,listener)
            }

            else -> {
                val binding = DataBindingUtil.inflate<ItemLoadingBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.item_loading,
                    parent,
                    false
                )
                LoadingViewHolder(binding)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mListMessage[position] == null) {
            MessageType.LOADING.value
        } else {
            if (mListMessage[position]?.sender?.id == currentUserId) {
                if (!mListMessage[position]?.images.isNullOrEmpty()) {
                    MessageType.MY_IMAGE.value
                } else {
                    MessageType.MY_MESSAGE.value
                }
            } else {
                if (!mListMessage[position]?.images.isNullOrEmpty()) {
                    MessageType.OTHERS_IMAGE.value
                } else {
                    MessageType.OTHERS_MESSAGE.value
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return mListMessage.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = mListMessage[position]
        val previousMessage = if (position > 0) mListMessage[position - 1].takeIf { it?.sender?.id == message?.sender?.id } else null
        val nextMessage = if (position < mListMessage.size - 1) {
            mListMessage[position + 1].takeIf { it?.sender?.id == message?.sender?.id }
        } else {
            null
        }

        val messageGroupState = when {
            nextMessage != null && previousMessage != null -> GroupMessageState.MIDDLE
            nextMessage != null && previousMessage == null -> GroupMessageState.FIRST
            nextMessage == null && previousMessage != null -> GroupMessageState.LAST
            else -> GroupMessageState.SINGLE
        }
        when (holder) {
            is MyMessageViewHolder -> {
                holder.bind(message, messageGroupState)
            }

            is OtherMessageViewHolder -> {
                holder.bind(message, messageGroupState)
            }

            is MyImageMessageViewHolder -> {
                holder.bind(message, messageGroupState)
            }

            is OtherImageMessageViewHolder -> {
                holder.bind(message, messageGroupState)
            }
        }
    }

    fun setFirstPageMessage(listMessage: List<Message>) {
        val diffResult = DiffUtil.calculateDiff(MessageDiffUtilsCallback(mListMessage, listMessage))
        mListMessage.clear()
        mListMessage.addAll(listMessage.reversed())
        diffResult.dispatchUpdatesTo(this)
    }

    fun addOlderListMessage(listMessage: List<Message>) {
        mListMessage.removeIf { it == null }
        mListMessage.addAll(0, listMessage.reversed())
        notifyItemRangeInserted(0, listMessage.size - 1)
        notifyItemChanged(listMessage.size)
    }

    fun loadMoreLoading() {
        mListMessage.add(0, null)
        notifyItemInserted(0)
    }

    fun removeLoading() {
        mListMessage.removeIf { it == null }
        notifyItemRemoved(0)
    }

    fun addNewMessage(message: Message) {
        if (mListMessage.isNotEmpty() && mListMessage.last()?.isTyping == true) {
            mListMessage[mListMessage.size - 1] = message
            notifyItemChanged(mListMessage.size - 1)
        } else {
            mListMessage.add(message)
            notifyItemInserted(mListMessage.size - 1)
            notifyItemChanged(mListMessage.size - 2)
        }
    }

    fun updateMessage(message: Message) {
        mListMessage.find { it?.id == message.id }?.let {
            it.edited = message.edited
            it.content = message.content
            notifyItemChanged(mListMessage.indexOf(it))
        }
    }

    fun updateMessageState(message: Message) {
        mListMessage.find { it?.sendTime == message.sendTime }?.let {
            it.id = message.id
            it.conversation = message.conversation
            it.state = message.state
            notifyItemChanged(mListMessage.indexOf(it))
        }
    }

    fun deleteMessage(messageId: String){
        mListMessage.find { it?.id == messageId }?.let {
            val index = mListMessage.indexOf(it)
            mListMessage.remove(it)
            notifyItemRemoved(index)
        }
    }

    fun setTyping(typing: Boolean) {
        if (typing) {
            if (mListMessage.isEmpty() || mListMessage.last()?.isTyping == false) {
                mListMessage.add(
                    Message().apply {
                        isTyping = true
                    }
                )
                notifyItemInserted(mListMessage.size - 1)
            }
        } else {
            if (mListMessage.isNotEmpty() && mListMessage.last()?.isTyping == true) {
                mListMessage.removeLast()
                notifyItemRemoved(mListMessage.size)
            }
        }
    }

    fun seenAllMessage() {
        val firstUnseenMessageIndex = mListMessage.indexOfFirst {
            it?.state == MessageState.SENT.value
        }
        if (firstUnseenMessageIndex != -1) {
            for (i in firstUnseenMessageIndex until mListMessage.size) {
                mListMessage[i]?.state = MessageState.SEEN.value
            }
            notifyItemRangeChanged(
                firstUnseenMessageIndex,
                mListMessage.size - firstUnseenMessageIndex
            )
        }
    }

    fun getMessage(position: Int): Message? {
        return mListMessage[position]
    }
}

class LoadingViewHolder(binding: ItemLoadingBinding) : RecyclerView.ViewHolder(binding.root)

enum class GroupMessageState {
    SINGLE, FIRST, MIDDLE, LAST
}

enum class MessageType(val value: Int) {
    MY_MESSAGE(0), OTHERS_MESSAGE(1), LOADING(2), MY_IMAGE(3), OTHERS_IMAGE(4),
}
