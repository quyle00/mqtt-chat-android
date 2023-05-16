package com.quyt.mqttchat.presentation.adapter

import androidx.recyclerview.widget.DiffUtil
import com.quyt.mqttchat.domain.model.Message

class MessageDiffUtilsCallback(private val oldPosts : List<Message?>, private val newPosts : List<Message?>) : DiffUtil.Callback(){
    override fun getOldListSize(): Int {
        return oldPosts.size
    }

    override fun getNewListSize(): Int {
        return newPosts.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldPosts[oldItemPosition]?.id == newPosts[newItemPosition]?.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldPosts[oldItemPosition] == newPosts[newItemPosition]
    }
}