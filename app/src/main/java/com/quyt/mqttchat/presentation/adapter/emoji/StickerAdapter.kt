package com.quyt.mqttchat.presentation.adapter.emoji

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemEmojiHeaderBinding
import com.quyt.mqttchat.databinding.ItemStickerBinding
import com.quyt.mqttchat.domain.model.Sticker

class StickerAdapter(private var stickers: ArrayList<Sticker>, private val onClickSticker: (Sticker) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == StickerType.HEADER.value) {
            val binding =
                DataBindingUtil.inflate<ItemEmojiHeaderBinding>(LayoutInflater.from(parent.context), R.layout.item_emoji_header, parent, false)
            StickerHeaderViewHolder(binding)
        } else {
            val binding = DataBindingUtil.inflate<ItemStickerBinding>(LayoutInflater.from(parent.context), R.layout.item_sticker, parent, false)
            StickerViewHolder(binding)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (stickers[position].url.isEmpty()) {
            StickerType.HEADER.value
        } else {
            StickerType.STICKER.value
        }
    }

    override fun getItemCount(): Int {
        return stickers.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is StickerViewHolder) {
            holder.bind(stickers[position])
        } else if (holder is StickerHeaderViewHolder) {
            holder.bind(stickers[position])
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setData(newStickers: ArrayList<Sticker>) {
        stickers.clear()
        stickers.addAll(newStickers)
        notifyDataSetChanged()
    }

    inner class StickerViewHolder(private var binding: ItemStickerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(sticker : Sticker) {
            Glide.with(binding.root.context)
                .load(sticker.url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivSticker)
            binding.root.setOnClickListener {
                onClickSticker(sticker)
            }
        }
    }

    inner class StickerHeaderViewHolder(private var binding: ItemEmojiHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(sticker: Sticker) {
            binding.tvCategoryName.text = sticker.category
        }
    }

    enum class StickerType(val value: Int) {
        HEADER(0), STICKER(1)
    }

}