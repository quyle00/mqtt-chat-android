package com.quyt.mqttchat.presentation.adapter.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.AlignSelf
import com.google.android.flexbox.FlexboxLayoutManager
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemImageBinding


class ImageAdapter(private val listImageUrl: List<String>) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = DataBindingUtil.inflate<ItemImageBinding>(LayoutInflater.from(parent.context), R.layout.item_image, parent, false)
        return ImageViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listImageUrl.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(listImageUrl[position])
    }

    class ImageViewHolder(private val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            Glide.with(binding.root.context).load(item).into(binding.ivImage)
//            val lp: ViewGroup.LayoutParams = binding.ivImage.layoutParams
//            if (lp is FlexboxLayoutManager.LayoutParams) {
//                lp.flexGrow = 1.0f
//                lp.alignSelf = AlignItems.FLEX_END
//            }
        }


    }

}