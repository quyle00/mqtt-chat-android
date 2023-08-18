package com.quyt.mqttchat.presentation.adapter.message

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.quyt.mqttchat.R
import com.quyt.mqttchat.constant.Constant
import com.quyt.mqttchat.databinding.ItemImageBinding
import com.quyt.mqttchat.domain.model.Media

class MediaAdapter(private val listImageUrl: List<Media>,private val listener : OnMessageClickListener) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = DataBindingUtil.inflate<ItemImageBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_image,
            parent,
            false
        )
        return MediaViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listImageUrl.size
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(listImageUrl[position])
    }

    inner class MediaViewHolder(private val binding: ItemImageBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(item: Media) {
            binding.cvRoot.setOnClickListener {
                listener.onMediaClick(binding.ivImage,item)
            }
            Glide.with(binding.root.context)
                .load(item.getFullUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(
                    ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.ic_launcher_background
                    )
                )
                .into(binding.ivImage)
            if (getFileExtensionFromUrl(item.getFullUrl()) == "mp4") {
                binding.llDuration.visibility = View.VISIBLE
            }else
                binding.llDuration.visibility = View.GONE
        }

        private fun getFileExtensionFromUrl(url: String): String {
            val fileUrl = url.substringAfterLast("/")
            val fileExtension = fileUrl.substringAfterLast(".", "")
            return fileExtension.ifEmpty { "" }
        }
    }
}
