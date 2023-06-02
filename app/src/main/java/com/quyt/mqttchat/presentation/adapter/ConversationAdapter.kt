package com.quyt.mqttchat.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemConversationBinding
import com.quyt.mqttchat.domain.model.Conversation
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.MessageState
import com.quyt.mqttchat.presentation.adapter.base.BaseRecyclerAdapter

class ConversationAdapter(private val listener: OnConversationListener) : BaseRecyclerAdapter<Conversation>(
    itemSameChecker = { oldItem, newItem -> oldItem.id == newItem.id },
    contentSameChecker = { oldItem, newItem -> oldItem == newItem }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = DataBindingUtil.inflate<ItemConversationBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_conversation,
            parent,
            false
        )
        return ConversationViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ConversationViewHolder) {
            holder.bind(getItem(position))
        }
    }

    fun updateLastMessage(conversationId: String?, lastMessage: Message?) {
        getItems().indexOfFirst { it.id == conversationId }.let {
            val conversation = getItem(it)
            conversation.lastMessage = lastMessage
            updateAt(it, conversation)
        }
    }
}

class ConversationViewHolder(
    private val binding: ItemConversationBinding,
    private val listener: OnConversationListener
) : RecyclerView.ViewHolder(
    binding.root
) {
    fun bind(conversation: Conversation) {
        if (conversation.lastMessage?.images?.isNotEmpty() == true) {
            binding.tvContent.text = "Sent ${conversation.lastMessage?.images?.size} images"
        } else {
            binding.tvContent.text = conversation.lastMessage?.content
        }
        binding.conversation = conversation
        if (conversation.lastMessage?.isMine == true) {
            binding.tvContent.text = "You: ${conversation.lastMessage?.content}"
            binding.ivState.visibility = View.VISIBLE
            binding.ivState.setImageResource(
                when (conversation.lastMessage?.state) {
                    MessageState.SENT.value -> R.drawable.ic_check
                    MessageState.SEEN.value -> R.drawable.ic_double_check
                    else -> -1
                }
            )
        } else {
            binding.ivState.visibility = View.GONE
            if (conversation.lastMessage?.state == MessageState.SEEN.value) {
                binding.tvContent.setTypeface(null, android.graphics.Typeface.NORMAL)
            } else {
                binding.tvContent.setTypeface(null, android.graphics.Typeface.BOLD)
            }
        }

        binding.rlRoot.setOnClickListener {
            listener.onConversationClick(conversation)
        }
    }
}

interface OnConversationListener {
    fun onConversationClick(conversation: Conversation?)
}
