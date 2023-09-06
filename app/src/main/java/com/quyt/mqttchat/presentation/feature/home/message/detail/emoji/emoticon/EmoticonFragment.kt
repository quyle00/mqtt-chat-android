package com.quyt.mqttchat.presentation.feature.home.message.detail.emoji.emoticon

import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.FragmentEmoticonBinding
import com.quyt.mqttchat.domain.model.Emoji
import com.quyt.mqttchat.presentation.adapter.emoji.EmojiAdapter
import com.quyt.mqttchat.domain.model.EmojiCategory
import com.quyt.mqttchat.presentation.adapter.emoji.EmojiCategoryAdapter
import com.quyt.mqttchat.presentation.base.BaseBindingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmoticonFragment(private val editText: EditText) : BaseBindingFragment<FragmentEmoticonBinding, EmoticonViewModel>() {

    private lateinit var emojiAdapter: EmojiAdapter
    private var listEmoji = ArrayList<Emoji>()
    private val listEmojiCategory = ArrayList<EmojiCategory>()
    private var listRecentEmoji = ArrayList<Emoji>()
    private var selectedCategoryIndex = 0
    private val sharedPreferences by lazy { requireContext().getSharedPreferences("quyt_emoji", Context.MODE_PRIVATE) }
    override fun layoutId(): Int = R.layout.fragment_emoticon

    override val viewModel: EmoticonViewModel by viewModels()

    override fun setupView() {
        observeState()
        handleBackSpace()
        viewModel.getEmoji(requireContext())
    }

    private fun observeState() {
        viewModel.uiState().observe(viewLifecycleOwner) {state ->
            when (state) {
                is EmoticonState.Success -> {
                    listEmoji = state.data
                    setupCategoryRecyclerView()
                    setupEmojiRecyclerView()
                }
            }
        }
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
}
