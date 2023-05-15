package com.quyt.mqttchat.presentation.ui.home.message

import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.FragmentConversationListBinding
import com.quyt.mqttchat.presentation.adapter.ConversationAdapter
import com.quyt.mqttchat.presentation.adapter.OnConversationListener
import com.quyt.mqttchat.presentation.base.BaseBindingFragment
import com.quyt.mqttchat.presentation.ui.home.HomeFragmentDirections
import com.quyt.mqttchat.utils.LoadingDialog
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ConversationListFragment : BaseBindingFragment<FragmentConversationListBinding, ConversationListViewModel>(), OnConversationListener {

    override fun layoutId(): Int = R.layout.fragment_conversation_list

    override val viewModel: ConversationListViewModel by viewModels()

    private lateinit var mConversationAdapter: ConversationAdapter

    override fun setupView() {
        setupRecyclerView()
        observeState()
        viewModel.getListConversation()
    }

    override fun onConversationClick(conversationId: String?) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToConversationDetailFragment(conversationId, null)
        )
    }

    private fun setupRecyclerView() {
        mConversationAdapter = ConversationAdapter(this)
        binding.rvConversation.adapter = mConversationAdapter
        binding.rvConversation.layoutManager = LinearLayoutManager(requireContext())
        (binding.rvConversation.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    private fun observeState() {
        viewModel.uiState().observe(viewLifecycleOwner) { state ->
            when (state) {
                is ConversationListState.Loading -> {
                    LoadingDialog.showLoading(requireContext())
                }

                is ConversationListState.Success -> {
                    LoadingDialog.hideLoading()
                    mConversationAdapter.setItems(state.data)
                }

                is ConversationListState.Error -> {
                    LoadingDialog.hideLoading()
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                }

                is ConversationListState.NewMessage -> {
                    val conversationId = state.message?.conversation
                    mConversationAdapter.updateLastMessage(conversationId, state.message)
                }
            }
        }
    }

}