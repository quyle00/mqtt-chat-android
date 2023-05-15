package com.quyt.mqttchat.presentation.adapter.base

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerAdapter<T>(
    private var items: MutableList<T> = mutableListOf(),
    private val itemSameChecker: (oldItem: T, newItem: T) -> Boolean,
    private val contentSameChecker: (oldItem: T, newItem: T) -> Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount() = items.size

    fun setItems(newItems: List<T>) {
        val result = DiffUtil.calculateDiff(
            BaseDiffUtilsCallback(
                oldList = items,
                newList = newItems,
                itemSameChecker = itemSameChecker,
                contentSameChecker = contentSameChecker
            )
        )
        items.clear()
        items.addAll(newItems)
        result.dispatchUpdatesTo(this)
    }

    protected fun getItem(position: Int): T {
        return items[position]
    }

    protected fun updateAt(position: Int, item: T) {
        items[position] = item
        notifyItemChanged(position)
    }

    protected fun getItems() = items

    abstract override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder

    abstract override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
}