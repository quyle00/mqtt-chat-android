package com.quyt.mqttchat.presentation.ui

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ActivityMainBinding
import com.quyt.mqttchat.domain.repository.SharedPreferences
import com.quyt.mqttchat.presentation.base.BaseBindingActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseBindingActivity<ActivityMainBinding>() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences
    override fun getLayoutId(): Int = R.layout.activity_main

    override fun onViewReady(savedInstance: Bundle?) {
      if (sharedPreferences.getCurrentUser() != null) {
           val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
           val navController = navHostFragment.navController
          navController.navigate(R.id.action_loginFragment_to_homeFragment)
        }
    }
}