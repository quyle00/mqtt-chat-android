package com.quyt.mqttchat.presentation.adapter.message.viewHolder

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemMyMessageBinding
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.MessageState
import com.quyt.mqttchat.presentation.adapter.message.GroupMessageState
import com.quyt.mqttchat.utils.DateUtils

class MyMessageViewHolder(private val binding: ItemMyMessageBinding) : RecyclerView.ViewHolder(
    binding.root
) {
    fun bind(message: Message?, groupMessageState: GroupMessageState) {
        //
        if (message?.reply != null) {
            binding.layoutReply.root.visibility = ViewGroup.VISIBLE
            binding.layoutReply.tvMessageOwner.text = message.reply?.sender?.fullname
            binding.layoutReply.tvMessageContent.text = message.reply?.content
        }
        //
        binding.message = message
        if (message?.isTyping == false) {
            binding.tvTime2.text = DateUtils.formatTime(message.createdAt ?: "", "HH:mm")
        }
        binding.ivState.setImageResource(
            if (message?.state == MessageState.SEEN.value) {
                R.drawable.ic_double_check
            } else {
                R.drawable.ic_check
            }
        )
        //
        val params = binding.rlMessage.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = 40
        when (groupMessageState) {
            GroupMessageState.SINGLE -> {
                binding.rlMessage.setBackgroundResource(R.drawable.bg_my_chat)
            }

            GroupMessageState.FIRST -> {
                binding.rlMessage.setBackgroundResource(R.drawable.bg_my_chat_first)
            }

            GroupMessageState.MIDDLE -> {
                binding.rlMessage.setBackgroundResource(R.drawable.bg_my_chat_middle)
                params.topMargin = 4
            }

            GroupMessageState.LAST -> {
                binding.rlMessage.setBackgroundResource(R.drawable.bg_my_chat_last)
                params.topMargin = 4
            }
        }
        binding.rlMessage.layoutParams = params
    }
}
