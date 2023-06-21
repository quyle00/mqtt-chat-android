package com.quyt.mqttchat.emoji

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemEmojiCategoryBinding

class EmojiCategoryAdapter(
    private val listCategory: ArrayList<EmojiCategory>,
    private val onCategoryClick: (EmojiCategory) -> Unit
) : RecyclerView.Adapter<EmojiCategoryAdapter.EmoticonCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmoticonCategoryViewHolder {
        val binding = ItemEmojiCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EmoticonCategoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listCategory.size
    }

    override fun onBindViewHolder(holder: EmoticonCategoryViewHolder, position: Int) {
        holder.bind(listCategory[position])
    }

    fun selectCategory(position: Int) {
        for (i in listCategory.indices) {
            listCategory[i].selected = i == position
        }
        notifyDataSetChanged()
    }


    inner class EmoticonCategoryViewHolder(
        private val binding: ItemEmojiCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: EmojiCategory) {
            binding.root.setOnClickListener {
                onCategoryClick(category)
                selectCategory(adapterPosition)
            }
            binding.ivEmojiCategory.setImageResource(category.icon)
            if (category.selected) {
                binding.ivEmojiCategory.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.colorPrimary))
            } else {
                binding.ivEmojiCategory.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.normal_text))
            }
        }
    }
}