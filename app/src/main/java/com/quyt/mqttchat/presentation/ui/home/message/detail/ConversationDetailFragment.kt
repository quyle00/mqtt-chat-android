package com.quyt.mqttchat.presentation.ui.home.message.detail

import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.FragmentConversionDetailBinding
import com.quyt.mqttchat.domain.model.Message
import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.presentation.adapter.MessageAdapter
import com.quyt.mqttchat.presentation.base.BaseBindingFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.Random

@AndroidEntryPoint
class ConversationDetailFragment : BaseBindingFragment<FragmentConversionDetailBinding, ConversationDetailViewModel>() {

    //    private val args : FragmentConversionBinding by navArgs()
    override fun layoutId(): Int = R.layout.fragment_conversion_detail
    override val viewModel: ConversationDetailViewModel by viewModels()

    private lateinit var mMessageAdapter: MessageAdapter
    private var mListMessage = ArrayList<Message>()

    private var user = User().apply {
        id = Random().nextInt(1000)
        fullname = "Quyt"
    }

    override fun setupView() {
        initConversationList()
        observeState()
        handleTyping()
        viewModel.subscribeConversation(0)
        binding.ivSend.setOnClickListener {
            val text = binding.etMessage.text.toString()
            if (text.isNotEmpty()) {
                sendMessage(text)
            }
        }
    }

    private fun observeState() {
        viewModel.uiState().observe(viewLifecycleOwner) { state ->
            when (state) {
                is ConversationDetailState.Loading -> {

                }

                is ConversationDetailState.Success -> {

                }

                is ConversationDetailState.Error -> {

                }

                is ConversationDetailState.NewMessage -> {
                    mMessageAdapter.addMessage(state.message)
                    binding.rvMessage.scrollToPosition(0)
                }

                is ConversationDetailState.Typing -> {
                    if (state.message.sender?.id != user.id) {
                        mMessageAdapter.setTyping(state.message.isTyping)
                        binding.rvMessage.scrollToPosition(0)
                    }
                }
            }
        }
    }

    private fun handleTyping() {
        binding.etMessage.addTextChangedListener {
            if (it.toString().isNotEmpty() && !viewModel.mTyping) {
                viewModel.mTyping = true
                viewModel.sendTyping(user, 0, true)
            }
            if (it.toString().isEmpty() && viewModel.mTyping) {
                viewModel.mTyping = false
                viewModel.sendTyping(user, 0, false)
            }
        }
    }

    private fun initConversationList() {
        mMessageAdapter = MessageAdapter(user.id)
        binding.rvMessage.adapter = mMessageAdapter
        binding.rvMessage.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        mMessageAdapter.setListMessage(mListMessage)
    }

    private fun sendMessage(messageText: String) {
        val message = Message().apply {
            content = messageText
            sender = user
        }
        viewModel.sendMessage(0, message)
        binding.etMessage.setText("")
    }

}