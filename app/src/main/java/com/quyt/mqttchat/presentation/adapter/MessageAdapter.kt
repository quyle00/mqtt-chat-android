package com.quyt.mqttchat.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemMyMessageBinding
import com.quyt.mqttchat.databinding.ItemOtherMessageBinding
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.MessageState
import com.quyt.mqttchat.utils.DateUtils

class MessageAdapter(private val currentUserId: String?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mListMessage = mutableListOf<Message>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == MessageType.MY_MESSAGE.value) {
            val binding = DataBindingUtil.inflate<ItemMyMessageBinding>(LayoutInflater.from(parent.context), R.layout.item_my_message, parent, false)
            MyMessageViewHolder(binding)
        } else {
            val binding = DataBindingUtil.inflate<ItemOtherMessageBinding>(LayoutInflater.from(parent.context), R.layout.item_other_message, parent, false)
            OtherMessageViewHolder(binding)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mListMessage[position].sender?.id == currentUserId) {
            MessageType.MY_MESSAGE.value
        } else {
            MessageType.OTHERS_MESSAGE.value
        }
    }

    override fun getItemCount(): Int {
        return mListMessage.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = mListMessage[position]
        val previousMessageTime = if (position < mListMessage.size - 1) mListMessage[position + 1].createdAt else null

        when (holder) {
            is MyMessageViewHolder -> {
                holder.bind(message, previousMessageTime)
            }

            is OtherMessageViewHolder -> {
                holder.bind(message, previousMessageTime)
            }
        }
    }

    fun setListMessage(listMessage: List<Message>) {
        val diffResult = DiffUtil.calculateDiff(MessageDiffUtilsCallback(mListMessage, listMessage))
        mListMessage.clear()
        mListMessage.addAll(listMessage)
        diffResult.dispatchUpdatesTo(this)
    }

    fun addMessage(message: Message) {
        if (mListMessage.isEmpty()) {
            mListMessage.add(0, message)
            notifyItemInserted(0)
        } else if (!mListMessage[0].isTyping) {
            mListMessage.add(0, message)
            notifyItemInserted(0)
        } else {
            mListMessage[0] = message
            notifyItemChanged(0)
        }
    }

    fun setMessageSent(message: Message) {
//        val index = mListMessage.indexOfFirst { it.id == message.id }
//        if (index != -1) {
//            message.state = MessageState.SENT.value
//            mListMessage[index] = message
//            notifyItemChanged(index)
//        }
        mListMessage.find { it.id == message.id }?.let {
            it.state = MessageState.SENT.value
            notifyItemChanged(mListMessage.indexOf(it))
        }
    }

    fun setMessageFailed(message: Message) {
        val index = mListMessage.indexOfFirst { it.id == message.id }
        if (index != -1) {
            message.state = MessageState.FAILED.value
            mListMessage[index] = message
            notifyItemChanged(index)
        }
    }

    fun setTyping(typing: Boolean) {
        if (typing) {
            if (mListMessage.isEmpty() || !mListMessage[0].isTyping) {
                mListMessage.add(0, Message().apply {
                    isTyping = true
                })
                notifyItemInserted(0)
            }
        } else {
            if (mListMessage.isNotEmpty() && mListMessage[0].isTyping) {
                mListMessage.removeAt(0)
                notifyItemRemoved(0)
            }
        }
    }

}

class MyMessageViewHolder(private val binding: ItemMyMessageBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(message: Message, previousMessageTime: String?) {
        if (DateUtils.compareInMinutes(previousMessageTime, message.createdAt) > 5) {
            binding.tvTime.visibility = View.VISIBLE
            binding.tvTime.text = DateUtils.formatTime(message.createdAt ?: "")
        } else {
            binding.tvTime.visibility = View.GONE
        }
        binding.tvMessage.text = message.content
        when (message.state) {
            MessageState.SENDING.value -> {
                binding.loading.visibility = View.VISIBLE
                binding.ivRefresh.visibility = View.GONE
            }

            MessageState.SENT.value -> {
                binding.loading.visibility = View.GONE
                binding.ivRefresh.visibility = View.GONE
            }

            MessageState.FAILED.value -> {
                binding.loading.visibility = View.GONE
                binding.ivRefresh.visibility = View.VISIBLE
            }
        }
    }
}

class OtherMessageViewHolder(private val binding: ItemOtherMessageBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(message: Message, previousMessageTime: String?) {
        if (DateUtils.compareInMinutes(previousMessageTime, message.createdAt) > 5) {
            binding.tvTime.visibility = View.VISIBLE
            binding.tvTime.text = DateUtils.formatTime(message.createdAt ?: "")
        } else {
            binding.tvTime.visibility = View.GONE
        }
        binding.tvMessage.text = message.content
        if (message.isTyping) {
            binding.tvMessage.visibility = View.GONE
            binding.lavTyping.visibility = View.VISIBLE
        } else {
            binding.tvMessage.visibility = View.VISIBLE
            binding.lavTyping.visibility = View.GONE
        }
    }

}

enum class MessageType(val value: Int) {
    MY_MESSAGE(0),
    OTHERS_MESSAGE(1)
}

