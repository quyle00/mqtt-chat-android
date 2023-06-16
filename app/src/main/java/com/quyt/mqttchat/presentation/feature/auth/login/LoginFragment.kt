package com.quyt.mqttchat.presentation.feature.auth.login

import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.FragmentLoginBinding
import com.quyt.mqttchat.presentation.base.BaseBindingFragment
import com.quyt.mqttchat.utils.LoadingDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseBindingFragment<FragmentLoginBinding, LoginViewModel>() {
    override fun layoutId(): Int = R.layout.fragment_login

    override val viewModel: LoginViewModel by viewModels()

    override fun setupView() {
        observeState()
        binding.btnLogin.setOnClickListener {
            viewModel.login(binding.etUsername.text.toString(), binding.etPassword.text.toString())
        }
        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun observeState() {
        viewModel.uiState().observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginState.Loading -> {
                    LoadingDialog.showLoading(requireContext())
                }

                is LoginState.Success -> {
                    LoadingDialog.hideLoading()
                    Toast.makeText(requireContext(), "Login success", Toast.LENGTH_SHORT).show()
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.homeFragment, true)
                        .build()
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment,null,navOptions)
                }

                is LoginState.Error -> {
                    LoadingDialog.hideLoading()
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
