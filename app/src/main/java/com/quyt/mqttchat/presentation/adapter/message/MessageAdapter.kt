package com.quyt.mqttchat.presentation.adapter.message

import android.view.LayoutInflater
import android.view.ViewGroup
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

class MessageAdapter(private val currentUserId: String?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mListMessage = mutableListOf<Message?>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            MessageType.MY_MESSAGE.value -> {
                val binding = DataBindingUtil.inflate<ItemMyMessageBinding>(LayoutInflater.from(parent.context), R.layout.item_my_message, parent, false)
                MyMessageViewHolder(binding)
            }

            MessageType.OTHERS_MESSAGE.value -> {
                val binding = DataBindingUtil.inflate<ItemOtherMessageBinding>(LayoutInflater.from(parent.context), R.layout.item_other_message, parent, false)
                OtherMessageViewHolder(binding)
            }

            MessageType.LOADING.value -> {
                val binding = DataBindingUtil.inflate<ItemLoadingBinding>(LayoutInflater.from(parent.context), R.layout.item_loading, parent, false)
                LoadingViewHolder(binding)
            }

            MessageType.MY_IMAGE.value -> {
                val binding = DataBindingUtil.inflate<ItemMyImageMessageBinding>(LayoutInflater.from(parent.context), R.layout.item_my_image_message, parent, false)
                MyImageMessageViewHolder(binding)
            }

            MessageType.OTHERS_IMAGE.value -> {
                val binding = DataBindingUtil.inflate<ItemOtherImageMessageBinding>(LayoutInflater.from(parent.context), R.layout.item_other_image_message, parent, false)
                OtherImageMessageViewHolder(binding)
            }

            else -> {
                val binding = DataBindingUtil.inflate<ItemLoadingBinding>(LayoutInflater.from(parent.context), R.layout.item_loading, parent, false)
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
        val previousMessage = if (position < mListMessage.size - 1) mListMessage[position + 1].takeIf { it?.sender?.id == message?.sender?.id } else null
        val nextMessage = if (position > 0) mListMessage[position - 1].takeIf { it?.sender?.id == message?.sender?.id } else null

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

    fun setListMessage(listMessage: List<Message>) {
        val diffResult = DiffUtil.calculateDiff(MessageDiffUtilsCallback(mListMessage, listMessage))
        mListMessage.clear()
        mListMessage.addAll(listMessage)
        diffResult.dispatchUpdatesTo(this)
    }

    fun addListMessage(listMessage: List<Message>) {
        val diffResult = DiffUtil.calculateDiff(MessageDiffUtilsCallback(mListMessage, listMessage))
        mListMessage.addAll(listMessage)
        notifyItemRangeInserted(mListMessage.size - listMessage.size, listMessage.size)
        notifyItemChanged(mListMessage.size - listMessage.size - 1)
//        diffResult.dispatchUpdatesTo(this)
    }

    fun loading(isLoading: Boolean) {
        if (isLoading) {
            mListMessage.add(null)
            notifyItemInserted(mListMessage.size - 1)
        } else {
            mListMessage.remove(null)
            notifyItemRemoved(mListMessage.size)
        }
    }

    fun addMessage(message: Message) {
        if (mListMessage.isNotEmpty() && mListMessage[0]?.isTyping == true) {
            mListMessage[0] = message
            notifyItemChanged(0)
        } else {
            mListMessage.add(0, message)
            notifyItemInserted(0)
            notifyItemChanged(1)
        }
    }

    fun updateMessage(message: Message) {
        mListMessage.find { it?.sendTime == message.sendTime }?.let {
            it.state = message.state
            notifyItemChanged(mListMessage.indexOf(it))
        }
    }

    fun setTyping(typing: Boolean) {
        if (typing) {
            if (mListMessage.isEmpty() || mListMessage[0]?.isTyping == false) {
                mListMessage.add(0, Message().apply {
                    isTyping = true
                })
                notifyItemInserted(0)
            }
        } else {
            if (mListMessage.isNotEmpty() && mListMessage[0]?.isTyping == true) {
                mListMessage.removeAt(0)
                notifyItemRemoved(0)
            }
        }
    }

    fun seenAllMessage() {
        val firstUnseenMessageIndex = mListMessage.indexOfFirst { it?.state == MessageState.SENT.value }
        if (firstUnseenMessageIndex != -1) {
            for (i in firstUnseenMessageIndex until mListMessage.size) {
                mListMessage[i]?.state = MessageState.SEEN.value
            }
            notifyItemRangeChanged(firstUnseenMessageIndex, mListMessage.size - firstUnseenMessageIndex)
        }
    }

}

class LoadingViewHolder(binding: ItemLoadingBinding) : RecyclerView.ViewHolder(binding.root) {
}

enum class GroupMessageState {
    SINGLE, FIRST, MIDDLE, LAST
}

enum class MessageType(val value: Int) {
    MY_MESSAGE(0), OTHERS_MESSAGE(1), LOADING(2), MY_IMAGE(3), OTHERS_IMAGE(4),
}

