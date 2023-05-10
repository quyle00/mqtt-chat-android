package com.quyt.mqttchat.presentation.ui.home.message.detail

import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.FragmentConversionDetailBinding
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.MessageState
import com.quyt.mqttchat.presentation.adapter.MessageAdapter
import com.quyt.mqttchat.presentation.base.BaseBindingFragment
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class ConversationDetailFragment : BaseBindingFragment<FragmentConversionDetailBinding, ConversationDetailViewModel>() {

    private val args: ConversationDetailFragmentArgs by navArgs()
    override fun layoutId(): Int = R.layout.fragment_conversion_detail
    override val viewModel: ConversationDetailViewModel by viewModels()

    private lateinit var mMessageAdapter: MessageAdapter
    private var mListMessage = ArrayList<Message>()
    private lateinit var mManager: LinearLayoutManager


    override fun setupView() {
        initConversationList()
        observeState()
        handleTyping()
        viewModel.getListMessage(args.conversationId)
        viewModel.subscribeConversation(args.conversationId)
        binding.ivSend.setOnClickListener {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val messageContent = binding.etMessage.text.toString()
            if (messageContent.isNotEmpty()) {
                // Create message model
                val newMessage = Message().apply {
                    this.sender = viewModel.getCurrentUser()
                    this.content = messageContent
                    this.state = MessageState.SENDING.value
                    this.createdAt = sdf.format(Date())
                }
                // Add message to view
                mMessageAdapter.addMessage(newMessage)
                binding.rvMessage.scrollToPosition(0)
//                binding.rvMessage.layoutManager?.smoothScrollToPosition(binding.rvMessage, null, 0)
//                binding.rvMessage.scrollToPosition(0)
                //
                viewModel.sendMessage(args.conversationId, newMessage)
                binding.etMessage.setText("")
            }
        }
    }

    private fun observeState() {
        viewModel.uiState().observe(viewLifecycleOwner) { state ->
            when (state) {
                is ConversationDetailState.Loading -> {

                }

                is ConversationDetailState.Success -> {
                    mMessageAdapter.setListMessage(state.data)
                    binding.rvMessage.scrollToPosition(0)
                }

                is ConversationDetailState.Error -> {

                }

                is ConversationDetailState.NewMessage -> {
                    mMessageAdapter.addMessage(state.message)
                    binding.rvMessage.scrollToPosition(0)
                }

                is ConversationDetailState.Typing -> {
                    if (state.message.sender?.id != viewModel.getCurrentUser()?.id) {
                        mMessageAdapter.setTyping(state.message.isTyping)
                        binding.rvMessage.scrollToPosition(0)
                    }
                }

                is ConversationDetailState.SendMessageSuccess -> {
                    mMessageAdapter.setMessageSent(state.message)
                }

                is ConversationDetailState.SendMessageError -> {
                    mMessageAdapter.setMessageFailed(state.message)
                }
            }
        }
    }

    private fun handleTyping() {
        binding.etMessage.addTextChangedListener {
            if (it.toString().isNotEmpty() && !viewModel.mTyping) {
                viewModel.mTyping = true
                viewModel.sendTyping(viewModel.getCurrentUser(), args.conversationId, true)
            }
            if (it.toString().isEmpty() && viewModel.mTyping) {
                viewModel.mTyping = false
                viewModel.sendTyping(viewModel.getCurrentUser(), args.conversationId, false)
            }
        }
    }

    private fun initConversationList() {
        mMessageAdapter = MessageAdapter(viewModel.getCurrentUser()?.id)
        binding.rvMessage.adapter = mMessageAdapter
        binding.rvMessage.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        (binding.rvMessage.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        mMessageAdapter.setListMessage(mListMessage)
    }

}