package com.quyt.mqttchat.presentation.adapter.message.viewHolder

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemMyStickerBinding
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.MessageState
import com.quyt.mqttchat.presentation.adapter.message.OnMessageClickListener
import com.quyt.mqttchat.utils.DateUtils

class MyStickerViewHolder(private val binding: ItemMyStickerBinding, private val listener: OnMessageClickListener) : RecyclerView.ViewHolder(
    binding.root
) {
    fun bind(message: Message?) {
        Glide.with(binding.root)
            .load(message?.medias?.firstOrNull()?.url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.ivSticker)
        //
        binding.ivSticker.setOnClickListener {
            listener.onMessageLongClick(message, absoluteAdapterPosition)
        }
        binding.tvTime2.text = DateUtils.formatTime(message?.createdAt ?: "", "HH:mm")
        binding.ivState.setImageResource(
            if (message?.state == MessageState.SEEN.value) R.drawable.ic_double_check else R.drawable.ic_check
        )
    }
}
