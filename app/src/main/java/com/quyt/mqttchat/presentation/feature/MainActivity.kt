package com.quyt.mqttchat.presentation.feature

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ActivityMainBinding
import com.quyt.mqttchat.domain.model.Conversation
import com.quyt.mqttchat.domain.repository.IMqttClient
import com.quyt.mqttchat.domain.repository.SharedPreferences
import com.quyt.mqttchat.domain.usecase.user.SendUserStatusEventUseCase
import com.quyt.mqttchat.presentation.base.BaseBindingActivity
import com.quyt.mqttchat.presentation.feature.home.HomeFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseBindingActivity<ActivityMainBinding>() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences
    @Inject
    lateinit var mqttClient: IMqttClient
    @Inject
    lateinit var sendUserStatusEventUseCase: SendUserStatusEventUseCase
    override fun getLayoutId(): Int = R.layout.activity_main

    override fun onViewReady(savedInstance: Bundle?)  {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        if (sharedPreferences.getCurrentUser()?.token.isNullOrEmpty()) {
            navGraph.setStartDestination(R.id.loginFragment)
        }else{
            navGraph.setStartDestination(R.id.homeFragment)
        }
        navController.graph = navGraph

        val extras = intent.extras
        if (extras != null) {
            val conversationId = extras.getString("conversationId")
            navController.navigate(
                HomeFragmentDirections.actionHomeFragmentToConversationDetailFragment(
                    conversationId,
                    null
                )
            )
        }

    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            if (!mqttClient.connect()) {
                Log.d("MQTT", "Failed to connect")
            }
            sendUserStatusEventUseCase(true)
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            sendUserStatusEventUseCase(false)
        }
    }

}
