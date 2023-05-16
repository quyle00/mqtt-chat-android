package com.quyt.mqttchat.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemMyMessageBinding
import com.quyt.mqttchat.databinding.ItemOtherMessageBinding
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.MessageState
import com.quyt.mqttchat.utils.DateUtils

class MessageAdapter2(private val currentUserId: String?) : PagingDataAdapter<Message,RecyclerView.ViewHolder>(callback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == MessageType.MY_MESSAGE.value) {
            val binding = DataBindingUtil.inflate<ItemMyMessageBinding>(LayoutInflater.from(parent.context), R.layout.item_my_message, parent, false)
            MyMessageViewHolder2(binding)
        } else {
            val binding = DataBindingUtil.inflate<ItemOtherMessageBinding>(LayoutInflater.from(parent.context), R.layout.item_other_message, parent, false)
            OtherMessageViewHolder2(binding)
        }
    }

     object callback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(
            oldItem: Message,
            newItem: Message
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Message,
            newItem: Message
        ): Boolean = oldItem.sendTime == newItem.sendTime
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)?.sender?.id == currentUserId) {
            MessageType.MY_MESSAGE.value
        } else {
            MessageType.OTHERS_MESSAGE.value
        }
    }

//    override fun getItemCount(): Int {
//        return mListMessage.size
//    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        // Because list reverse, so previous message is next message and next message is previous message
        val previousMessage = if (position < itemCount - 1) getItem(position+1) else null
        val nextMessage = if (position > 0) getItem(position - 1) else null

        when (holder) {
            is MyMessageViewHolder2 -> {
                val myPreviousMessageTime = previousMessage?.sender?.id?.takeIf { it == currentUserId }?.let { previousMessage.createdAt }
                val myNextMessageTime = nextMessage?.sender?.id?.takeIf { it == currentUserId }?.let { nextMessage.createdAt }
                holder.bind(message, myPreviousMessageTime, myNextMessageTime)
            }

            is OtherMessageViewHolder2 -> {
                val otherPreviousMessageTime = previousMessage?.sender?.id?.takeIf { it != currentUserId }?.let { previousMessage.createdAt }
                val otherNextMessageTime = nextMessage?.sender?.id?.takeIf { it != currentUserId }?.let { nextMessage.createdAt }
                holder.bind(message, otherPreviousMessageTime, otherNextMessageTime)
            }
        }
    }

//    fun setListMessage(listMessage: List<Message>) {
//        val diffResult = DiffUtil.calculateDiff(MessageDiffUtilsCallback(mListMessage, listMessage))
//        mListMessage.clear()
//        mListMessage.addAll(listMessage)
//        addMessage()
//        diffResult.dispatchUpdatesTo(this)
//    }

//    fun addMessage(message: Message) {
//        if (mListMessage.isNotEmpty() && mListMessage[0].isTyping) {
//            mListMessage[0] = message
//            notifyItemChanged(0)
//        } else {
//            mListMessage.add(0, message)
//            notifyItemInserted(0)
//            notifyItemChanged(1)
//        }
//    }
//
//    fun updateMessage(message: Message) {
//        mListMessage.find { it.sendTime == message.sendTime }?.let {
//            it.state = message.state
//            notifyItemChanged(mListMessage.indexOf(it))
//        }
//    }
//
//    fun setTyping(typing: Boolean) {
//        if (typing) {
//            if (mListMessage.isEmpty() || !mListMessage[0].isTyping) {
//                mListMessage.add(0, Message().apply {
//                    isTyping = true
//                })
//                notifyItemInserted(0)
//            }
//        } else {
//            if (mListMessage.isNotEmpty() && mListMessage[0].isTyping) {
//                mListMessage.removeAt(0)
//                notifyItemRemoved(0)
//            }
//        }
//    }
//
//    fun seenMessage() {
//        val firstUnseenMessageIndex = mListMessage.indexOfFirst { it.state == MessageState.SENT.value }
//        if (firstUnseenMessageIndex != -1) {
//            for (i in firstUnseenMessageIndex until mListMessage.size) {
//                mListMessage[i].state = MessageState.SEEN.value
//            }
//            notifyItemRangeChanged(firstUnseenMessageIndex, mListMessage.size - firstUnseenMessageIndex)
//        }
//    }

}

class MyMessageViewHolder2(private val binding: ItemMyMessageBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(message: Message?, myPreviousMessageTime: String?, myNextMessageTime: String?) {
        binding.message = message
        if (message?.isTyping == false) {
            binding.tvTime.text = DateUtils.formatTime(message.createdAt ?: "")
        }
        //
        val params = binding.llRoot.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = 40
        when (handleGroupMessage(myPreviousMessageTime, message?.createdAt, myNextMessageTime)) {
            GroupMessageState.SINGLE -> {
                binding.rlMessage.setBackgroundResource(R.drawable.bg_my_chat)
            }
            GroupMessageState.FIRST -> {
                binding.rlMessage.setBackgroundResource(R.drawable.bg_my_chat_first)
//                if (DateUtils.compareInMinutes(myPreviousMessageTime, message?.createdAt) > 1) {
//                    binding.tvTime.visibility = View.VISIBLE
//                } else {
//                    binding.tvTime.visibility = View.GONE
//                }
            }
            GroupMessageState.MIDDLE -> {
                binding.rlMessage.setBackgroundResource(R.drawable.bg_my_chat_middle)
                params.topMargin = 4
            }

            GroupMessageState.LAST -> {
                binding.rlMessage.setBackgroundResource(R.drawable.bg_my_chat_last)
                params.topMargin = 4
            }
        }

        binding.rlMessage.setOnClickListener {
            if (binding.tvTime.visibility == View.VISIBLE) {
                binding.tvTime.visibility = View.GONE
            } else {
                binding.tvTime.visibility = View.VISIBLE
            }
        }
    }


}

class OtherMessageViewHolder2(private val binding: ItemOtherMessageBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(message: Message?, myPreviousMessageTime: String?, myNextMessageTime: String?) {
        //
        val params = binding.llRoot.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = 40
        when (handleGroupMessage(myPreviousMessageTime, message?.createdAt, myNextMessageTime)) {
            GroupMessageState.SINGLE -> {
                binding.rlMessage.setBackgroundResource(R.drawable.bg_other_chat)
            }

            GroupMessageState.FIRST -> {
                binding.rlMessage.setBackgroundResource(R.drawable.bg_other_chat_first)
            }

            GroupMessageState.MIDDLE -> {
                binding.rlMessage.setBackgroundResource(R.drawable.bg_other_chat_middle)
                params.topMargin = 4
            }

            GroupMessageState.LAST -> {
                binding.rlMessage.setBackgroundResource(R.drawable.bg_other_chat_last)
                params.topMargin = 4
            }
        }
        binding.llRoot.layoutParams = params
        //
        if (message?.isTyping == false) {
            binding.tvTime.text = DateUtils.formatTime(message?.createdAt ?: "")
        }
        binding.tvMessage.text = message?.content
        if (message?.isTyping == true) {
            binding.tvMessage.visibility = View.GONE
            binding.lavTyping.visibility = View.VISIBLE
        } else {
            binding.tvMessage.visibility = View.VISIBLE
            binding.lavTyping.visibility = View.GONE
        }
        binding.rlMessage.setOnClickListener {
            if (binding.tvTime.visibility == View.VISIBLE) {
                binding.tvTime.visibility = View.GONE
            } else {
                binding.tvTime.visibility = View.VISIBLE
            }
        }
    }
}
