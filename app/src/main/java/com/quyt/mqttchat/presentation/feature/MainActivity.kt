package com.quyt.mqttchat.presentation.feature

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ActivityMainBinding
import com.quyt.mqttchat.domain.repository.IMqttClient
import com.quyt.mqttchat.domain.repository.SharedPreferences
import com.quyt.mqttchat.domain.usecase.user.SendUserStatusEventUseCase
import com.quyt.mqttchat.presentation.base.BaseBindingActivity
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

    override fun onViewReady(savedInstance: Bundle?) {
        if (sharedPreferences.getCurrentUser() != null) {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            navController.navigate(R.id.action_loginFragment_to_homeFragment)
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
