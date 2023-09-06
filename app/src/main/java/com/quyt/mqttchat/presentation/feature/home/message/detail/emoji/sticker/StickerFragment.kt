package com.quyt.mqttchat.presentation.feature.home.message.detail.emoji.sticker

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.FragmentStickerBinding
import com.quyt.mqttchat.domain.model.Sticker
import com.quyt.mqttchat.presentation.adapter.emoji.StickerAdapter
import com.quyt.mqttchat.presentation.adapter.emoji.StickerCategoryAdapter
import com.quyt.mqttchat.presentation.base.BaseBindingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StickerFragment(private val onStickerSelected : (Sticker) -> Unit) : BaseBindingFragment<FragmentStickerBinding, StickerViewModel>() {

    private lateinit var stickerAdapter: StickerAdapter
    private lateinit var stickerCategoryAdapter: StickerCategoryAdapter
    private var listSticker = ArrayList<Sticker>()
    override fun layoutId(): Int = R.layout.fragment_sticker

    override val viewModel: StickerViewModel by viewModels()

    override fun setupView() {
        observeState()
        setupStickerRecyclerView()
        setupCategoryRecyclerView()
    }

    private fun observeState() {
        viewModel.uiState().observe(viewLifecycleOwner) { state ->
            when (state) {
                is StickerState.Success -> {
                    listSticker.clear()
                    listSticker.addAll(state.data)
                    stickerAdapter.setData(state.data)
                    stickerCategoryAdapter.setData(state.category)
                }
            }
        }
    }

    private fun setupCategoryRecyclerView() {
        stickerCategoryAdapter = StickerCategoryAdapter(ArrayList()) {
            val position = listSticker.indexOfFirst { sticker ->
                sticker.category == it.name
            }
            if (position != -1) {
                (binding.rvSticker.layoutManager as GridLayoutManager).scrollToPositionWithOffset(position, 0)
            }
        }
        binding.rvCategory.adapter = stickerCategoryAdapter
        binding.rvCategory.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupStickerRecyclerView() {
        stickerAdapter = StickerAdapter(ArrayList()) {
            onStickerSelected(it)
        }
        val gridLayoutManager = GridLayoutManager(context, 5)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (stickerAdapter.getItemViewType(position) == StickerAdapter.StickerType.HEADER.value) {
                    5
                } else {
                    1
                }
            }
        }
        binding.rvSticker.adapter = stickerAdapter
        binding.rvSticker.layoutManager = gridLayoutManager
//        (binding.rvEmoji.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
//        binding.rvEmoji.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_DRAGGING) {
//                    super.onScrolled(recyclerView, dx, dy)
//                    val firstVisibleItemPosition = gridLayoutManager.findFirstVisibleItemPosition()
//                    val emojiCategory = listEmoji[firstVisibleItemPosition].category
//                    val emojiCategoryPosition = listEmojiCategory.indexOfFirst { category ->
//                        category.name == emojiCategory
//                    }
//                    if (emojiCategoryPosition != -1 && emojiCategoryPosition != selectedCategoryIndex) {
//                        selectedCategoryIndex = emojiCategoryPosition
//                        val emojiCategoryAdapter = binding.rvCategory.adapter as EmojiCategoryAdapter
//                        emojiCategoryAdapter.selectCategory(selectedCategoryIndex)
//                    }
//                }
//            }
//        })
    }
}
