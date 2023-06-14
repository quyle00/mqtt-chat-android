package com.quyt.mqttchat.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemConversationBinding
import com.quyt.mqttchat.domain.model.Conversation
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.MessageState
import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.extensions.setAvatar
import com.quyt.mqttchat.extensions.setShortTime
import com.quyt.mqttchat.presentation.adapter.base.BaseRecyclerAdapter


interface OnConversationListener {
    fun onConversationClick(conversation: Conversation?)
}

class ConversationAdapter(private val currentUserId : String,
                          private val listener: OnConversationListener) : BaseRecyclerAdapter<Conversation>(
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
        return ConversationViewHolder(binding)
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

    fun updateUserStatus(userId: String?, isOnline: Boolean) {
        val foundIndex = getItems().indexOfFirst {
            val foundParticipant = it.participants?.firstOrNull { user ->
                user.id == userId
            }
            foundParticipant != null
        }
        if (foundIndex != -1) {
            val conversation = getItem(foundIndex)
            conversation.participants?.firstOrNull { user ->
                user.id == userId
            }?.isOnline = isOnline
            updateAt(foundIndex, conversation)
        }
    }

    fun markReadLastMessage(conversationId: String?) {
        getItems().indexOfFirst { it.id == conversationId }.let {
            val conversation = getItem(it)
            conversation.lastMessage?.state = MessageState.SEEN.value
            updateAt(it, conversation)
        }
    }


    inner class ConversationViewHolder(
        private val binding: ItemConversationBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(conversation: Conversation) {
            val partner = conversation.participants?.getPartner()
            binding.tvName.text = partner?.fullname
            binding.ivAvatar.setAvatar(partner?.avatar)
            binding.tvTime.setShortTime(conversation.lastMessage?.createdAt?:"")
            binding.cvOnlineStatus.visibility = if (partner?.isOnline == true) View.VISIBLE else View.GONE
            // Show last message
            if (conversation.lastMessage?.images?.isNotEmpty() == true) {
                binding.tvContent.text = "Sent ${conversation.lastMessage?.images?.size} images"
            } else {
                binding.tvContent.text = conversation.lastMessage?.content
            }
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
                    binding.tvContent.setTextColor(ContextCompat.getColor(binding.root.context, R.color.normal_text))
                } else {
                    binding.tvContent.setTypeface(null, android.graphics.Typeface.BOLD)
                    binding.tvContent.setTextColor(ContextCompat.getColor(binding.root.context, R.color.title_text))
                }
            }

            binding.rlRoot.setOnClickListener {
                listener.onConversationClick(conversation)
            }
        }
        private fun List<User>?.getPartner(): User? {
            return this?.firstOrNull { user -> user.id != currentUserId }
        }
    }
}

