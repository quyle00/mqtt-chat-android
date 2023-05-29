package com.quyt.mqttchat.presentation.adapter.message

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemImageBinding
import com.quyt.mqttchat.utils.DateUtils


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
            if (getFileExtensionFromUrl(item) == "mp4") {
                binding.ivImage.visibility = View.GONE
                binding.rlVideo.visibility = View.VISIBLE
                binding.vvVideo.setVideoPath(item)
                binding.vvVideo.requestFocus()
                binding.tvDuration.text = DateUtils.formatMilliseconds(binding.vvVideo.duration.toLong())
                binding.vvVideo.setOnClickListener {
                    if (binding.vvVideo.isPlaying) {
                        binding.vvVideo.pause()
                    } else {
                        binding.vvVideo.start()
                    }
                }
            } else {
                binding.ivImage.visibility = View.VISIBLE
                binding.rlVideo.visibility = View.GONE
                Glide.with(binding.root.context)
                    .load(item)
                    .error(ContextCompat.getDrawable(binding.root.context, R.drawable.ic_launcher_background))
                    .into(binding.ivImage)
            }
        }

        private fun getFileExtensionFromUrl(url: String): String {
            val fileUrl = url.substringAfterLast("/")
            val fileExtension = fileUrl.substringAfterLast(".", "")
            return if (fileExtension.isNotEmpty()) fileExtension else ""
        }


    }

}