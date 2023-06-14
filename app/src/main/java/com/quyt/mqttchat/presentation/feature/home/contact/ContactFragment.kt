package com.quyt.mqttchat.presentation.feature.home.contact

import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.gson.Gson
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.FragmentContactBinding
import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.presentation.adapter.ContactAdapter
import com.quyt.mqttchat.presentation.adapter.OnContactListener
import com.quyt.mqttchat.presentation.base.BaseBindingFragment
import com.quyt.mqttchat.presentation.feature.home.HomeFragmentDirections
import com.quyt.mqttchat.utils.LoadingDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactFragment :
    BaseBindingFragment<FragmentContactBinding, ContactViewModel>(),
    OnContactListener {

    private lateinit var mContactAdapter: ContactAdapter
    override fun layoutId(): Int = R.layout.fragment_contact
    override val viewModel: ContactViewModel by viewModels()
    override fun setupView() {
        observeState()
        setupRecyclerView()
        viewModel.getListContact()
    }

    override fun onContactClick(user: User) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToConversationDetailFragment(
                null,
                Gson().toJson(user)
            )
        )
    }

    private fun setupRecyclerView() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getListContact()
        }
        mContactAdapter = ContactAdapter(this)
        binding.rvContact.adapter = mContactAdapter
        binding.rvContact.layoutManager = LinearLayoutManager(requireContext())
        (binding.rvContact.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    private fun observeState() {
        viewModel.uiState().observe(viewLifecycleOwner) { state ->
            when (state) {
                is ContactState.Loading -> {
                    binding.swipeRefreshLayout.isRefreshing = true
                }

                is ContactState.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    mContactAdapter.setItems(state.data)
                    viewModel.subscribeUserStatus()
                }

                is ContactState.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                }

                is ContactState.UserStatusChange -> {
                    mContactAdapter.updateOnlineStatus(state.userId, state.isOnline, state.lastSeen)
                }
            }
        }
    }
}
