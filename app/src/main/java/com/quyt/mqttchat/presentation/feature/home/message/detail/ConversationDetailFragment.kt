package com.quyt.mqttchat.presentation.feature.home.message.detail

import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.gson.Gson
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.FragmentConversionDetailBinding
import com.quyt.mqttchat.domain.model.Conversation
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.MessageContentType
import com.quyt.mqttchat.domain.model.MessageState
import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.presentation.adapter.message.MessageAdapter
import com.quyt.mqttchat.presentation.adapter.message.MessageSwipeController
import com.quyt.mqttchat.presentation.base.BaseBindingFragment
import com.quyt.mqttchat.presentation.feature.home.message.ConversationListViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ConversationDetailFragment : BaseBindingFragment<FragmentConversionDetailBinding, ConversationDetailViewModel>(), BottomSheetListener {

    private val args: ConversationDetailFragmentArgs by navArgs()
    private lateinit var messageAdapter: MessageAdapter
    private var isLoading = false
    private var noMoreData = false
    override fun layoutId(): Int = R.layout.fragment_conversion_detail
    override val viewModel: ConversationDetailViewModel by viewModels()
    private val conversationListViewModel: ConversationListViewModel by activityViewModels()

    override fun setupView() {
        binding.viewModel = viewModel
        viewModel.getConversationDetail(Gson().fromJson(args.conversation, Conversation::class.java), Gson().fromJson(args.partner, User::class.java))
        initConversationList()
        observeState()
        handleAction()
    }

    private fun observeState() {
        viewModel.uiState().observe(viewLifecycleOwner) { state ->
            when (state) {
                is ConversationDetailState.Loading -> {

                }

                is ConversationDetailState.Success -> {
                    messageAdapter.setFirstPageMessage(state.data)
                    scrollToBottom()
                    noMoreData = state.data.size < 20
                }

                is ConversationDetailState.LoadMoreSuccess -> {
                    isLoading = false
                    messageAdapter.loadMoreLoading(false)
                    messageAdapter.addOlderListMessage(state.data)
                    noMoreData = state.data.size < 20
                }

                is ConversationDetailState.Error -> {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                }

                is ConversationDetailState.NewMessage -> {
                    messageAdapter.addNewMessage(state.message)
                    scrollToBottom()
                }

                is ConversationDetailState.Typing -> {
                    if (state.message.sender?.id != viewModel.currentUser?.id) {
                        messageAdapter.setTyping(state.message.isTyping)
                        scrollToBottom()
                    }
                }

                is ConversationDetailState.MarkReadMessage -> {
                    messageAdapter.seenAllMessage()
                }

                is ConversationDetailState.SendMessageSuccess -> {
                    messageAdapter.updateMessage(state.message)
                    state.message.isMine = state.message.sender?.id == viewModel.currentUser?.id
                    conversationListViewModel.updateLastMessage(state.message)
                }

                is ConversationDetailState.SendMessageError -> {
                    val failedMessage = state.message
                    failedMessage.state = MessageState.FAILED.value
                    messageAdapter.updateMessage(failedMessage)
                }

                is ConversationDetailState.NoMoreData -> {
                    noMoreData = true
                }
            }
        }
    }

    private fun handleAction() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.etMessage.addTextChangedListener {
            if (it.toString().isNotEmpty() && !viewModel.isTyping) {
                viewModel.isTyping = true
                viewModel.sendTyping(true)
            }
            if (it.toString().isEmpty() && viewModel.isTyping) {
                viewModel.isTyping = false
                viewModel.sendTyping(false)
            }
        }
        binding.ivSend.setOnClickListener {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val messageContent = binding.etMessage.text.toString().trim()
            if (messageContent.isNotEmpty()) {
                // Create message model
                val newMessage = Message().apply {
                    this.sender = viewModel.currentUser
                    this.content = messageContent
                    this.state = MessageState.SENDING.value
                    this.createdAt = sdf.format(Date())
                    this.sendTime = Date().time
                }
                messageAdapter.addNewMessage(newMessage)
                binding.rvMessage.scrollToPosition(messageAdapter.itemCount - 1)
                viewModel.sendMessage(newMessage)
                binding.etMessage.setText("")
            }
        }
        binding.ivAttach.setOnClickListener {
            val bottomSheetAttach = BottomSheetAttach(this)
            bottomSheetAttach.show(childFragmentManager, bottomSheetAttach.tag)
        }
    }

    private fun initConversationList() {
        messageAdapter = MessageAdapter(viewModel.currentUser?.id)
        binding.rvMessage.adapter = messageAdapter
        binding.rvMessage.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        (binding.rvMessage.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        messageAdapter.setFirstPageMessage(ArrayList())
        val messageSwipeController = MessageSwipeController(requireContext()) {
            messageAdapter.getMessage(it)?.let { message ->
                viewModel.setReplyMessage(message)
            }
        }
        val itemTouchHelper = ItemTouchHelper(messageSwipeController)
        itemTouchHelper.attachToRecyclerView(binding.rvMessage)
        initScrollListener()
    }

    private fun initScrollListener() {
        binding.rvMessage.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    if (!isLoading && !noMoreData) {
                        isLoading = true
                        messageAdapter.loadMoreLoading(true)
                        viewModel.mCurrentPage++
                        viewModel.getListMessage(viewModel.mCurrentPage)
                    }
                }
            }
        })
    }

    override fun onDataSelected(data: ArrayList<String>) {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        // Create message model
        val newMessage = Message().apply {
            this.sender = viewModel.currentUser
            this.state = MessageState.SENDING.value
            this.createdAt = sdf.format(Date())
            this.sendTime = Date().time
            this.images = data
            this.type = MessageContentType.IMAGE.value
        }
        messageAdapter.addNewMessage(newMessage)
        scrollToBottom()
        viewModel.sendMessage(newMessage)
    }

    private fun scrollToBottom() {
        binding.rvMessage.scrollToPosition(messageAdapter.itemCount - 1)
    }
}