package com.quyt.mqttchat.presentation.adapter.base

import androidx.recyclerview.widget.DiffUtil

class BaseDiffUtilsCallback<T>(
    private val oldList: List<T>,
    private val newList: List<T>,
    private val itemSameChecker: (oldItem: T, newItem: T) -> Boolean,
    private val contentSameChecker: (oldItem: T, newItem: T) -> Boolean
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return itemSameChecker(oldList[oldItemPosition], newList[newItemPosition])
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return contentSameChecker(oldList[oldItemPosition], newList[newItemPosition])
    }
}