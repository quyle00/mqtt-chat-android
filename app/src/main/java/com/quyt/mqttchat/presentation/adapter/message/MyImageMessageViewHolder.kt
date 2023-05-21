package com.quyt.mqttchat.presentation.adapter.message

import android.graphics.Outline
import android.graphics.Rect
import android.view.View
import android.view.ViewOutlineProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemMyImageMessageBinding
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.MessageState
import com.quyt.mqttchat.utils.DateUtils

class MyImageMessageViewHolder(private val binding: ItemMyImageMessageBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(message: Message?, groupMessageState: GroupMessageState) {
        binding.message = message
        //
        val imageAdapter = ImageAdapter(message?.images?: arrayListOf())
        binding.rvImages.adapter = imageAdapter
        binding.rvImages.layoutManager = GridLayoutManager(binding.root.context, 3)
        binding.rvImages.layoutDirection = View.LAYOUT_DIRECTION_RTL
        //
//        binding.rvImages.addItemDecoration(SpacingImageDecoration(3, 20))
//        (binding.rvImages.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        //
        val outLineProvider = object : ViewOutlineProvider(){
            override fun getOutline(view: View?, outline: Outline?) {
                val left = 0
                val top = 0
                val right = view?.width?:0
                val bottom = view?.height?:0
                outline?.setRoundRect(left, top, right, bottom, 50f)
            }

        }
        binding.rvImages.apply {
            clipToOutline = true
            outlineProvider = outLineProvider
        }
        binding.tvTime2.text = DateUtils.formatTime(message?.createdAt ?:"", "HH:mm")
        binding.ivState.setImageResource(if (message?.state == MessageState.SEEN.value) R.drawable.ic_double_check else R.drawable.ic_check)
        //
//        val params = binding.rlMessage.layoutParams as ViewGroup.MarginLayoutParams
//        params.topMargin = 40
//        when (groupMessageState) {
//            GroupMessageState.SINGLE -> {
//                binding.rlMessage.setBackgroundResource(R.drawable.bg_my_chat)
//            }
//
//            GroupMessageState.FIRST -> {
//                binding.rlMessage.setBackgroundResource(R.drawable.bg_my_chat_first)
//            }
//
//            GroupMessageState.MIDDLE -> {
//                binding.rlMessage.setBackgroundResource(R.drawable.bg_my_chat_middle)
//                params.topMargin = 4
//            }
//
//            GroupMessageState.LAST -> {
//                binding.rlMessage.setBackgroundResource(R.drawable.bg_my_chat_last)
//                params.topMargin = 4
//            }
//        }
//        binding.rlMessage.layoutParams = params
    }


}

class SpacingImageDecoration(
    private val spanCount: Int,
    private val spacing: Int,
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        outRect.left = spacing / 2
        outRect.right = spacing / 2

        // Nếu item không nằm ở hàng đầu tiên, set margin bên trên
        if (position >= spanCount) {
            outRect.top = spacing
        }

        // Nếu item không nằm ở cột cuối cùng, set margin bên phải
        if (column < spanCount - 1) {
            outRect.right = spacing
        }
        if (position >= spanCount) {
            outRect.top = spacing
        }
    }
}