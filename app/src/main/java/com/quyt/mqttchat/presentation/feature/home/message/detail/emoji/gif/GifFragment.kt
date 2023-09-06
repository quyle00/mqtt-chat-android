package com.quyt.mqttchat.presentation.feature.home.message.detail.emoji.gif

import androidx.fragment.app.viewModels
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.FragmentLoginBinding
import com.quyt.mqttchat.presentation.base.BaseBindingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GifFragment : BaseBindingFragment<FragmentLoginBinding, GifViewModel>() {
    override fun layoutId(): Int = R.layout.fragment_gif

    override val viewModel: GifViewModel by viewModels()

    override fun setupView() {
        observeState()
    }

    private fun observeState() {
        viewModel.uiState().observe(viewLifecycleOwner) {
        }
    }
}
