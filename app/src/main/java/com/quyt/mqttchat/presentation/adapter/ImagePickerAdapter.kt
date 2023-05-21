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
    private val mListImageSelected = ArrayList<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = DataBindingUtil.inflate<ItemImagePickerBinding>(LayoutInflater.from(parent.context), R.layout.item_image_picker, parent, false)
        return ImagePickerViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ImagePickerViewHolder) {
            holder.bind(getItem(position))
        }
    }

    fun getSelectedImages() : ArrayList<String> {
        return mListImageSelected
    }

    inner class ImagePickerViewHolder(val binding: ItemImagePickerBinding, private val listener: OnImagePickerListener) : RecyclerView.ViewHolder(binding.root) {
        fun bind(image: String) {
            Glide.with(binding.root).load(image).fitCenter().into(binding.ivImage)
            if (mListImageSelected.contains(image)) {
                binding.cvCount.visibility = android.view.View.VISIBLE
                val positionInSelected = mListImageSelected.indexOf(image) + 1
                binding.tvCount.text = positionInSelected.toString()
            } else {
                binding.cvCount.visibility = android.view.View.INVISIBLE
            }
            binding.rlCount.setOnClickListener {
                if (mListImageSelected.contains(image)) {
                    mListImageSelected.remove(image)
                    notifyItemChanged(absoluteAdapterPosition)
                } else {
                    mListImageSelected.add(image)
                }
                mListImageSelected.forEach { image ->
                    val index = getItems().indexOf(image)
                    notifyItemChanged(index)
                }
                listener.onImageSelect(mListImageSelected)
            }
        }
    }
}

interface OnImagePickerListener {
    fun onImageSelect(imageSelected : ArrayList<String>)
}