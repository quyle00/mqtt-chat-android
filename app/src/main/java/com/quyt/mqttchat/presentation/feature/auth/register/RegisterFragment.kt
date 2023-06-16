package com.quyt.mqttchat.presentation.feature.auth.register

import androidx.fragment.app.viewModels
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.FragmentRegisterBinding
import com.quyt.mqttchat.presentation.base.BaseBindingFragment

class RegisterFragment : BaseBindingFragment<FragmentRegisterBinding, RegisterViewModel>() {
    override fun layoutId(): Int = R.layout.fragment_register

    override val viewModel: RegisterViewModel by viewModels()

    override fun setupView() {
    }
}