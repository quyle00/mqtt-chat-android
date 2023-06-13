package com.quyt.mqttchat.presentation.adapter.message.viewHolder

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemOtherMessageBinding
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.presentation.adapter.message.GroupMessageState
import com.quyt.mqttchat.utils.DateUtils

class OtherMessageViewHolder(val binding: ItemOtherMessageBinding) : RecyclerView.ViewHolder(
    binding.root
) {
    fun bind(message: Message?, groupMessageState: GroupMessageState) {
        //
        if (message?.isTyping == false) {
            binding.tvTime2.text = DateUtils.formatTime(message.createdAt ?: "", "HH:mm")
        }
        //
        binding.tvEdited.visibility = if (message?.edited == true) {
            ViewGroup.VISIBLE
        } else {
            ViewGroup.GONE
        }
        //
        val params = binding.rlMessage.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = 40
        when (groupMessageState) {
            GroupMessageState.SINGLE -> {
                binding.rlMessage.setBackgroundResource(R.drawable.bg_other_chat)
            }

            GroupMessageState.FIRST -> {
                binding.rlMessage.setBackgroundResource(R.drawable.bg_other_chat_first)
            }

            GroupMessageState.MIDDLE -> {
                binding.rlMessage.setBackgroundResource(R.drawable.bg_other_chat_middle)
                params.topMargin = 4
            }

            GroupMessageState.LAST -> {
                binding.rlMessage.setBackgroundResource(R.drawable.bg_other_chat_last)
                params.topMargin = 4
            }
        }
        binding.rlMessage.layoutParams = params
        //
        binding.tvMessage.text = message?.content
        if (message?.isTyping == true) {
            binding.tvMessage.visibility = View.GONE
            binding.lavTyping.visibility = View.VISIBLE
        } else {
            binding.tvMessage.visibility = View.VISIBLE
            binding.lavTyping.visibility = View.GONE
        }
    }
}
