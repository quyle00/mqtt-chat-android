package com.quyt.mqttchat.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemContactBinding
import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.presentation.adapter.base.BaseRecyclerAdapter

class ContactAdapter(private val listener: OnContactListener) : BaseRecyclerAdapter<User>(
    itemSameChecker = { oldItem, newItem -> oldItem.id == newItem.id },
    contentSameChecker = { oldItem, newItem -> oldItem == newItem }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = DataBindingUtil.inflate<ItemContactBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_contact,
            parent,
            false
        )
        return ContactViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ContactViewHolder) {
            holder.bind(getItem(position))
        }
    }

    fun updateOnlineStatus(userId: String?, isOnline: Boolean,lastSeen : Long) {
        val foundIndex = getItems().indexOfFirst {
            it.id == userId
        }
        if (foundIndex != -1) {
            val user = getItem(foundIndex)
            user.isOnline = isOnline
            user.lastSeen = lastSeen
            updateAt(foundIndex, user)
        }
    }
}

class ContactViewHolder(
    private val binding: ItemContactBinding,
    private val listener: OnContactListener
) : RecyclerView.ViewHolder(
    binding.root
) {
    fun bind(item: User) {
        binding.contact = item
        binding.llRoot.setOnClickListener {
            listener.onContactClick(item)
        }
    }
}

interface OnContactListener {
    fun onContactClick(user: User)
}
