package com.quyt.mqttchat.presentation.adapter.message.viewHolder

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quyt.mqttchat.databinding.ItemOtherImageMessageBinding
import com.quyt.mqttchat.domain.model.Media
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.presentation.adapter.message.GroupMessageState
import com.quyt.mqttchat.presentation.adapter.message.MediaAdapter
import com.quyt.mqttchat.presentation.adapter.message.OnMessageClickListener
import com.quyt.mqttchat.utils.DateUtils

class OtherImageMessageViewHolder(
    private val binding: ItemOtherImageMessageBinding,
    private val listener : OnMessageClickListener
) : RecyclerView.ViewHolder(
    binding.root
) {
    fun bind(message: Message?, groupMessageState: GroupMessageState) {
        binding.message = message
        //
        if (binding.rvImages.adapter == null) {
            initRecyclerView(message?.medias ?: arrayListOf())
        }
        binding.tvTime2.text = DateUtils.formatTime(message?.createdAt ?: "", "HH:mm")
    }

    private fun initRecyclerView(imageUrls: List<Media>) {
        val imageAdapter = MediaAdapter(imageUrls,listener)
        binding.rvImages.adapter = imageAdapter
        binding.rvImages.layoutManager = GridLayoutManager(binding.root.context, 3)
        binding.rvImages.layoutDirection = View.LAYOUT_DIRECTION_LTR
        //
        val outLineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                val left = 0
                val top = 0
                val right = view?.width ?: 0
                val bottom = view?.height ?: 0
                outline?.setRoundRect(left, top, right, bottom, 50f)
            }
        }
        binding.rvImages.apply {
            clipToOutline = true
            outlineProvider = outLineProvider
        }
    }
}
