package com.quyt.mqttchat.presentation.feature

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    private var hasNotificationPermissionGranted = false
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            hasNotificationPermissionGranted = isGranted
            if (!isGranted) {
                if (Build.VERSION.SDK_INT >= 33) {
                    if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                        showNotificationPermissionRationale()
                    } else {
                        showSettingDialog()
                    }
                }
            } else {
                Toast.makeText(applicationContext, "notification permission granted", Toast.LENGTH_SHORT)
                    .show()
            }
        }

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
        if (Build.VERSION.SDK_INT >= 33) {
            hasNotificationPermissionGranted = checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == 0
            if (!hasNotificationPermissionGranted) {
                requestNotificationPermission()
            }
        } else {
            hasNotificationPermissionGranted = true
        }

    }

    private fun requestNotificationPermission(){
        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            hasNotificationPermissionGranted = true
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

    private fun showSettingDialog() {
        MaterialAlertDialogBuilder(this, com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setTitle("Notification Permission")
            .setMessage("Notification permission is required, Please allow notification permission from setting")
            .setPositiveButton("Ok") { _, _ ->
                val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showNotificationPermissionRationale() {

        MaterialAlertDialogBuilder(this, com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setTitle("Alert")
            .setMessage("Notification permission is required, to show notification")
            .setPositiveButton("Ok") { _, _ ->
                if (Build.VERSION.SDK_INT >= 33) {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}
