package com.quyt.mqttchat.presentation.feature.home.message

import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.gson.Gson
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.FragmentConversationListBinding
import com.quyt.mqttchat.domain.model.Conversation
import com.quyt.mqttchat.presentation.adapter.ConversationAdapter
import com.quyt.mqttchat.presentation.adapter.OnConversationListener
import com.quyt.mqttchat.presentation.base.BaseBindingFragment
import com.quyt.mqttchat.presentation.feature.home.HomeFragmentDirections
import com.quyt.mqttchat.utils.LoadingDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConversationListFragment :
    BaseBindingFragment<FragmentConversationListBinding, ConversationListViewModel>(),
    OnConversationListener {

    override fun layoutId(): Int = R.layout.fragment_conversation_list

    override val viewModel: ConversationListViewModel by activityViewModels()

    private lateinit var mConversationAdapter: ConversationAdapter

    override fun setupView() {
        setupRecyclerView()
        observeState()
        viewModel.getListConversation()
    }

    override fun onConversationClick(conversation: Conversation?) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToConversationDetailFragment(
                conversation?.id,
                null
            )
        )
    }

    private fun setupRecyclerView() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getListConversation()
        }
        //
        mConversationAdapter = ConversationAdapter(viewModel.currentUser?.id?:"",this)
        binding.rvConversation.adapter = mConversationAdapter
        binding.rvConversation.layoutManager = LinearLayoutManager(requireContext())
        (binding.rvConversation.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    private fun observeState() {
        viewModel.uiState().observe(viewLifecycleOwner) { state ->
            when (state) {
                is ConversationListState.Loading -> {
                    binding.swipeRefreshLayout.isRefreshing = true
                }

                is ConversationListState.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    mConversationAdapter.setItems(state.data)
                    viewModel.subscribeUserStatus()
                }

                is ConversationListState.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                }

                is ConversationListState.NewMessage -> {
                    val conversationId = state.message?.conversation
                    mConversationAdapter.updateLastMessage(conversationId, state.message)
                }
                is ConversationListState.UserStatusChange -> {
                    mConversationAdapter.updateUserStatus(state.userId, state.isOnline)
                }
                is ConversationListState.MarkReadLastMessage -> {
                    mConversationAdapter.markReadLastMessage(state.conversationId)
                }
            }
        }
    }
}
