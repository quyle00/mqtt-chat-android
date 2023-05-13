package com.quyt.mqttchat.presentation.ui.home.message

import android.graphics.Paint
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvConversation)
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
            }
        }
    }

    val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        val p = Paint()
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            if (direction == ItemTouchHelper.LEFT) {
                mConversationAdapter.notifyItemChanged(position)
                Toast.makeText(requireContext(), "Swipe left", Toast.LENGTH_SHORT).show()
            } else if (direction == ItemTouchHelper.RIGHT) {
                mConversationAdapter.notifyItemChanged(position)
                Toast.makeText(requireContext(), "Swipe right", Toast.LENGTH_SHORT).show()
            }
        }
    }

}