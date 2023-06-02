package com.quyt.mqttchat.presentation.adapter

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemImagePickerBinding
import com.quyt.mqttchat.presentation.adapter.base.BaseRecyclerAdapter
import com.quyt.mqttchat.utils.DateUtils

enum class MediaType {
    IMAGE, VIDEO
}

data class MediaModel(val uri: String, val type: MediaType)

class ImagePickerAdapter(private val listener: OnImagePickerListener) : BaseRecyclerAdapter<MediaModel>(
    itemSameChecker = { oldItem, newItem -> oldItem == newItem },
    contentSameChecker = { oldItem, newItem -> oldItem == newItem }
) {
    private val mListImageSelected = ArrayList<MediaModel>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = DataBindingUtil.inflate<ItemImagePickerBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_image_picker,
            parent,
            false
        )
        return ImagePickerViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ImagePickerViewHolder) {
            holder.bind(getItem(position))
        }
    }

    fun getSelectedMediaUri(): ArrayList<String> {
        return mListImageSelected.map { it.uri } as ArrayList<String>
    }

    inner class ImagePickerViewHolder(
        val binding: ItemImagePickerBinding,
        private val listener: OnImagePickerListener
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(media: MediaModel) {
            Glide.with(binding.root.context).load(media.uri).into(binding.ivImage)
            if (media.type == MediaType.VIDEO) {
                binding.llDuration.visibility = View.VISIBLE
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(binding.root.context, Uri.parse(media.uri))
                val duration = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION
                )
                binding.tvDuration.text = DateUtils.formatMilliseconds(duration?.toLong())
                retriever.release()
            }
            if (mListImageSelected.contains(media)) {
                binding.cvCount.visibility = View.VISIBLE
                val positionInSelected = mListImageSelected.indexOf(media) + 1
                binding.tvCount.text = positionInSelected.toString()
            } else {
                binding.cvCount.visibility = View.INVISIBLE
            }
            binding.rlCount.setOnClickListener {
                if (mListImageSelected.contains(media)) {
                    mListImageSelected.remove(media)
                    notifyItemChanged(absoluteAdapterPosition)
                } else {
                    mListImageSelected.add(media)
                }
                mListImageSelected.forEach { media ->
                    val index = getItems().indexOf(media)
                    notifyItemChanged(index)
                }
                listener.onMediaSelected(mListImageSelected.map { it.uri } as ArrayList<String>)
            }
        }
    }
}

interface OnImagePickerListener {
    fun onMediaSelected(mediaSelected: ArrayList<String>)
}
