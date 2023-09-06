package com.quyt.mqttchat.presentation.adapter.emoji

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemEmojiCategoryBinding
import com.quyt.mqttchat.databinding.ItemStickerBinding
import com.quyt.mqttchat.databinding.ItemStickerCategoryBinding
import com.quyt.mqttchat.domain.model.StickerPack

class StickerCategoryAdapter(
    private val listCategory: ArrayList<StickerPack>,
    private val onCategoryClick: (StickerPack) -> Unit
) : RecyclerView.Adapter<StickerCategoryAdapter.StickerCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerCategoryViewHolder {
        val binding = ItemStickerCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StickerCategoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listCategory.size
    }

    override fun onBindViewHolder(holder: StickerCategoryViewHolder, position: Int) {
        holder.bind(listCategory[position])
    }

    fun selectCategory(position: Int) {
        for (i in listCategory.indices) {
            listCategory[i].selected = i == position
        }
        notifyDataSetChanged()
    }

    fun setData(newStickers: ArrayList<StickerPack>) {
        listCategory.clear()
        listCategory.addAll(newStickers)
        notifyDataSetChanged()
    }

    inner class StickerCategoryViewHolder(
        private val binding: ItemStickerCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(stickerPack: StickerPack) {
            binding.root.setOnClickListener {
                onCategoryClick(stickerPack)
                selectCategory(adapterPosition)
            }
            Glide.with(binding.root.context)
                .load(stickerPack.stickers[0].url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivStickerCategory)
//            if (stickerPack.selected) {
//                binding.ivEmojiCategory.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.colorPrimary))
//            } else {
//                binding.ivEmojiCategory.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.normal_text))
//            }
        }
    }
}