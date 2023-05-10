package com.quyt.mqttchat.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemConversationBinding
import com.quyt.mqttchat.domain.model.Conversation
import com.quyt.mqttchat.presentation.adapter.base.BaseRecyclerAdapter

class ConversationAdapter(private val listener: OnConversationListener) : BaseRecyclerAdapter<Conversation>(
    itemSameChecker = { oldItem, newItem -> oldItem.id == newItem.id },
    contentSameChecker = { oldItem, newItem -> oldItem == newItem }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = DataBindingUtil.inflate<ItemConversationBinding>(LayoutInflater.from(parent.context), R.layout.item_conversation, parent, false)
        return ConversationViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ConversationViewHolder) {
            holder.bind(getItem(position))
        }
    }
}

class ConversationViewHolder(private val binding: ItemConversationBinding, private val listener: OnConversationListener) : RecyclerView.ViewHolder(binding.root) {
    fun bind(conversation: Conversation) {
        binding.tvName.text = conversation.name
        binding.rlRoot.setOnClickListener {
            listener.onConversationClick(conversation.id)
        }
    }
}

interface OnConversationListener {
    fun onConversationClick(conversationId: Int)
}
