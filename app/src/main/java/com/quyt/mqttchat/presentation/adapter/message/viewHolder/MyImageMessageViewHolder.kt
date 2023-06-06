package com.quyt.mqttchat.presentation.adapter.message.viewHolder

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemMyImageMessageBinding
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.MessageState
import com.quyt.mqttchat.presentation.adapter.message.GroupMessageState
import com.quyt.mqttchat.presentation.adapter.message.MediaAdapter
import com.quyt.mqttchat.presentation.adapter.message.OnMessageClickListener
import com.quyt.mqttchat.utils.DateUtils

class MyImageMessageViewHolder(private val binding: ItemMyImageMessageBinding,private val listener : OnMessageClickListener) : RecyclerView.ViewHolder(
    binding.root
) {
    fun bind(message: Message?, groupMessageState: GroupMessageState) {
        binding.message = message
        //
        if (binding.rvImages.adapter == null) {
          initRecyclerView(message?.images ?: arrayListOf())
        }
        binding.tvTime2.text = DateUtils.formatTime(message?.createdAt ?: "", "HH:mm")
        binding.ivState.setImageResource(
            if (message?.state == MessageState.SEEN.value) R.drawable.ic_double_check else R.drawable.ic_check
        )
    }

    private fun initRecyclerView(imageUrls : List<String>){
        val imageAdapter = MediaAdapter(imageUrls,listener)
        binding.rvImages.adapter = imageAdapter
        binding.rvImages.layoutManager = GridLayoutManager(binding.root.context, 3)
        binding.rvImages.layoutDirection = View.LAYOUT_DIRECTION_RTL
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
