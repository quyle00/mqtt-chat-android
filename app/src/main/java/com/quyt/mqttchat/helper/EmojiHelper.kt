package com.quyt.mqttchat.helper

import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.PopupEmojiBinding
import com.quyt.mqttchat.emoji.Emoji
import com.quyt.mqttchat.emoji.EmojiAdapter
import com.quyt.mqttchat.emoji.EmojiCategory
import com.quyt.mqttchat.emoji.EmojiCategoryAdapter

class EmojiHelper(private val container : ViewGroup,private val editText: EditText) {
    private val context = container.context
    private var binding: PopupEmojiBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.popup_emoji, null, false)
    private lateinit var emojiAdapter: EmojiAdapter
    private var listEmoji = ArrayList<Emoji>()
    private val listEmojiCategory = ArrayList<EmojiCategory>()
    private var listRecentEmoji = ArrayList<Emoji>()
    private var selectedCategoryIndex = 0
    private val sharedPreferences by lazy { context.getSharedPreferences("quyt_emoji", Context.MODE_PRIVATE) }

    init{
        getEmoji()
        getRecentEmoji()
        setupCategoryRecyclerView()
        setupEmojiRecyclerView()
        handleBackSpace()
        container.addView(binding.root)
    }

    private fun setupCategoryRecyclerView() {
        listEmojiCategory.add(EmojiCategory("Recent", R.drawable.ic_recent, true))
        listEmojiCategory.add(EmojiCategory("Smileys & Emotion", R.drawable.ic_emoji))
        listEmojiCategory.add(EmojiCategory("People & Body", R.drawable.ic_like))
        listEmojiCategory.add(EmojiCategory("Animals & Nature", R.drawable.ic_animal))
        listEmojiCategory.add(EmojiCategory("Food & Drink", R.drawable.ic_apple))
        listEmojiCategory.add(EmojiCategory("Travel & Places", R.drawable.ic_travel))
        listEmojiCategory.add(EmojiCategory("Activities", R.drawable.ic_ball))
        listEmojiCategory.add(EmojiCategory("Objects", R.drawable.ic_object))
        listEmojiCategory.add(EmojiCategory("Symbols", R.drawable.ic_symbol))
        listEmojiCategory.add(EmojiCategory("Flags", R.drawable.ic_flag))
        val emojiCategoryAdapter = EmojiCategoryAdapter(listEmojiCategory) {
            val position = listEmoji.indexOfFirst { emoji ->
                emoji.category == it.name
            }
            if (position != -1) {
                (binding.rvEmoji.layoutManager as GridLayoutManager).scrollToPositionWithOffset(position, 0)
            }
        }
        binding.rvCategory.adapter = emojiCategoryAdapter
        binding.rvCategory.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupEmojiRecyclerView() {
        emojiAdapter = EmojiAdapter(listEmoji) {
            editText.text.insert(editText.selectionStart, it.emoji)
            updateRecent(it)
        }
        emojiAdapter.setHasStableIds(true)
        val gridLayoutManager = GridLayoutManager(context, 8)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (emojiAdapter.getItemViewType(position) == EmojiAdapter.EmojiType.HEADER.value) {
                    8
                } else {
                    1
                }
            }
        }
        binding.rvEmoji.adapter = emojiAdapter
        binding.rvEmoji.layoutManager = gridLayoutManager
//        binding.rvEmoji.setItemViewCacheSize(100)
        binding.rvEmoji.isNestedScrollingEnabled = false
        binding.rvEmoji.setHasFixedSize(true)
//        (binding.rvEmoji.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        binding.rvEmoji.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    super.onScrolled(recyclerView, dx, dy)
                    val firstVisibleItemPosition = gridLayoutManager.findFirstVisibleItemPosition()
                    val emojiCategory = listEmoji[firstVisibleItemPosition].category
                    val emojiCategoryPosition = listEmojiCategory.indexOfFirst { category ->
                        category.name == emojiCategory
                    }
                    if (emojiCategoryPosition != -1 && emojiCategoryPosition != selectedCategoryIndex) {
                        selectedCategoryIndex = emojiCategoryPosition
                        val emojiCategoryAdapter = binding.rvCategory.adapter as EmojiCategoryAdapter
                        emojiCategoryAdapter.selectCategory(selectedCategoryIndex)
                    }
                }
            }
        })
    }

    private fun handleBackSpace() {
        binding.cvBackspace.visibility = if (editText.text.isNullOrEmpty()) View.GONE else View.VISIBLE
        editText.addTextChangedListener {
            binding.cvBackspace.visibility = if (editText.text.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
        binding.cvBackspace.setOnClickListener {
            val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)
            editText.dispatchKeyEvent(event)
        }
    }

    private fun getEmoji() {
        val emojis = sharedPreferences.getString("emojis", "")
        listEmoji = if (emojis.isNullOrEmpty()) {
            val data = readEmojiFromJson()
            // Add header
            data.add(0, Emoji(data[0].category))
            for (i in 1 until data.size) {
                if (listRecentEmoji.size < 16) {
                    val recentTmp = data[i * 2].copy()
                    recentTmp.category = "Recent"
                    listRecentEmoji.add(recentTmp)
                }
                if (data[i].category != data[i - 1].category) {
                    data.add(i, Emoji(data[i].category))
                }
            }
            val json = Gson().toJson(data)
            sharedPreferences.edit().putString("emojis", json).apply()
            saveRecent()
            data
        } else {
            Gson().fromJson(emojis, object : TypeToken<ArrayList<Emoji>>() {}.type)
        }
    }

    private fun getRecentEmoji() {
        val recent = sharedPreferences.getString("recent", "")
        listRecentEmoji = if (recent.isNullOrEmpty()) {
            arrayListOf()
        } else {
            Gson().fromJson(recent, object : TypeToken<ArrayList<Emoji>>() {}.type)
        }
    }

    private fun updateRecent(emoji: Emoji) {
        emoji.category = "Recent"
        listRecentEmoji.removeIf { it.emoji == emoji.emoji }
        listRecentEmoji.add(0, emoji)
        if (listRecentEmoji.size > 16) {
            listRecentEmoji.removeLast()
        }
    }

    private fun saveRecent() {
        val json = Gson().toJson(listRecentEmoji)
        sharedPreferences.edit().putString("recent", json).apply()
    }

    private fun readEmojiFromJson(): ArrayList<Emoji> {
        var json: ArrayList<Emoji>? = null
        try {
            val inputStream = context.assets.open("emoji.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val jsonStr = String(buffer, Charsets.UTF_8)
            json = Gson().fromJson(jsonStr, object : TypeToken<ArrayList<Emoji>>() {}.type)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return json ?: arrayListOf()
    }
}