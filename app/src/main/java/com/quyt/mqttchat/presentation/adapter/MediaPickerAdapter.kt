package com.quyt.mqttchat.presentation.adapter

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemCameraBinding
import com.quyt.mqttchat.databinding.ItemImagePickerBinding
import com.quyt.mqttchat.domain.model.Media
import com.quyt.mqttchat.domain.model.MediaType
import com.quyt.mqttchat.presentation.adapter.base.BaseRecyclerAdapter
import com.quyt.mqttchat.utils.DateUtils



class ImagePickerAdapter(private val listener: OnImagePickerListener) : BaseRecyclerAdapter<Media?>(
    itemSameChecker = { oldItem, newItem -> oldItem == newItem },
    contentSameChecker = { oldItem, newItem -> oldItem == newItem }
) {
    private val mListImageSelected = ArrayList<Media>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 1){
            val binding = DataBindingUtil.inflate<ItemCameraBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_camera,
                parent,
                false
            )
            return CameraViewHolder(binding)
        }else{
            val binding = DataBindingUtil.inflate<ItemImagePickerBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_image_picker,
                parent,
                false
            )
            return ImagePickerViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ImagePickerViewHolder) {
            holder.bind(getItem(position)!!)
        }
        if (holder is CameraViewHolder){
            holder.bind()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) == null) {
            1
        } else {
            0
        }
    }

    fun getSelectedMediaUri(): ArrayList<Media> {
        return mListImageSelected
    }

    inner class ImagePickerViewHolder(
        val binding: ItemImagePickerBinding,
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(media: Media) {
            Glide.with(binding.root.context).load(media.localUri).into(binding.ivImage)
            if (media.type == MediaType.VIDEO.value) {
                binding.llDuration.visibility = View.VISIBLE
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(binding.root.context, Uri.parse(media.localUri))
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
                listener.onMediaSelected(mListImageSelected)
            }
        }
    }

    inner class CameraViewHolder(val binding : ItemCameraBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(){
        }
    }
}

interface OnImagePickerListener {
    fun onMediaSelected(mediaSelected: ArrayList<Media>)
}
