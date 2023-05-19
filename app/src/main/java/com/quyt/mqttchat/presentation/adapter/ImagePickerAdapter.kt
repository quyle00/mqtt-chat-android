package com.quyt.mqttchat.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemImagePickerBinding
import com.quyt.mqttchat.presentation.adapter.base.BaseRecyclerAdapter

class ImagePickerAdapter(private val listener: OnImagePickerListener) : BaseRecyclerAdapter<String>(
    itemSameChecker = { oldItem, newItem -> oldItem == newItem },
    contentSameChecker = { oldItem, newItem -> oldItem == newItem }
) {

    var imageCount = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = DataBindingUtil.inflate<ItemImagePickerBinding>(LayoutInflater.from(parent.context), R.layout.item_image_picker, parent, false)
        return ImagePickerViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ImagePickerViewHolder) {
            holder.bind(getItem(position))
        }
    }

    inner class ImagePickerViewHolder(private val binding: ItemImagePickerBinding, private val listener: OnImagePickerListener) : RecyclerView.ViewHolder(binding.root) {
        fun bind(image: String) {
            Glide.with(binding.root).load(image).fitCenter().into(binding.ivImage)
            binding.rlCount.setOnClickListener {
                if (binding.cvCount.visibility == android.view.View.VISIBLE) {
                    imageCount--
                    binding.cvCount.visibility = android.view.View.INVISIBLE
                    listener.onImageSelect(false, image)
                } else {
                    imageCount++
                    binding.cvCount.visibility = android.view.View.VISIBLE
                    listener.onImageSelect(true, image)
                    binding.tvCount.text = imageCount.toString()
                }
            }
        }
    }
}

interface OnImagePickerListener {
    fun onImageSelect(isSelect: Boolean, imageUri: String)
}