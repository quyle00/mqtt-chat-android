package com.quyt.mqttchat.presentation.adapter.emoji

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemEmojiBinding
import com.quyt.mqttchat.databinding.ItemEmojiHeaderBinding
import com.quyt.mqttchat.domain.model.Emoji

class EmojiAdapter(private var emojis: ArrayList<Emoji>, private val onClickEmoji: (Emoji) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == EmojiType.HEADER.value) {
            val binding =
                DataBindingUtil.inflate<ItemEmojiHeaderBinding>(LayoutInflater.from(parent.context), R.layout.item_emoji_header, parent, false)
            EmojiHeaderViewHolder(binding)
        } else {
            val binding = DataBindingUtil.inflate<ItemEmojiBinding>(LayoutInflater.from(parent.context), R.layout.item_emoji, parent, false)
            EmojiViewHolder(binding)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (emojis[position].emoji.isEmpty()) {
            EmojiType.HEADER.value
        } else {
            EmojiType.EMOJI.value
        }
    }

    override fun getItemCount(): Int {
        return emojis.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is EmojiViewHolder) {
            holder.bind(emojis[position])
        } else if (holder is EmojiHeaderViewHolder) {
            holder.bind(emojis[position])
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun updateRecentEmoji(newRecentEmoji: ArrayList<Emoji>) {
        if (emojis[0].category == "Recent") {
            emojis.subList(0, 16).clear()
            emojis.addAll(0, newRecentEmoji)
//            notifyItemRangeChanged(0, 16)
        } else {
            emojis.addAll(0, newRecentEmoji)
//            notifyItemRangeInserted(0, 16)
        }
        notifyDataSetChanged()
    }

    inner class EmojiViewHolder(private var binding: ItemEmojiBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(emoji: Emoji) {
            binding.tvEmoji.text = emoji.emoji
            binding.root.setOnClickListener {
                onClickEmoji(emoji)
            }
        }
    }

    inner class EmojiHeaderViewHolder(private var binding: ItemEmojiHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(emoji: Emoji) {
            binding.tvCategoryName.text = emoji.category
        }
    }

    enum class EmojiType(val value: Int) {
        HEADER(0), EMOJI(1)
    }

}