package com.quyt.mqttchat.presentation.ui.home.contact

import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.FragmentContactBinding
import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.presentation.adapter.ContactAdapter
import com.quyt.mqttchat.presentation.adapter.OnContactListener
import com.quyt.mqttchat.presentation.base.BaseBindingFragment
import com.quyt.mqttchat.presentation.ui.home.HomeFragmentDirections
import com.quyt.mqttchat.utils.LoadingDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactFragment : BaseBindingFragment<FragmentContactBinding, ContactViewModel>(), OnContactListener {

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
            HomeFragmentDirections.actionHomeFragmentToConversationDetailFragment(null, user.id)
        )
    }

    private fun setupRecyclerView() {
        mContactAdapter = ContactAdapter(this)
        binding.rvContact.adapter = mContactAdapter
        binding.rvContact.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeState() {
        viewModel.uiState().observe(viewLifecycleOwner) {state ->
            when (state) {
                is ContactState.Loading -> {
                    LoadingDialog.showLoading(requireContext())
                }
                is ContactState.Success -> {
                    LoadingDialog.hideLoading()
                    mContactAdapter.setItems(state.data)
                }
                is ContactState.Error -> {
                    LoadingDialog.hideLoading()
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}