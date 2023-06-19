package com.quyt.mqttchat.presentation.feature.home

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.FragmentHomeBinding
import com.quyt.mqttchat.presentation.adapter.HomeViewPagerAdapter
import com.quyt.mqttchat.presentation.base.BaseBindingFragment
import com.quyt.mqttchat.presentation.feature.home.contact.ContactFragment
import com.quyt.mqttchat.presentation.feature.home.message.ConversationListFragment

class HomeFragment() : BaseBindingFragment<FragmentHomeBinding, HomeViewModel>() {

    private lateinit var mHomeViewPagerAdapter: HomeViewPagerAdapter
    private var hasNotificationPermissionGranted = false
    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasNotificationPermissionGranted = isGranted
        if (!isGranted) {
            if (Build.VERSION.SDK_INT >= 33) {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                    showNotificationPermissionRationale()
                } else {
                    showSettingDialog()
                }
            }
        }
    }

    override fun layoutId(): Int = R.layout.fragment_home
    override val viewModel: HomeViewModel by viewModels()

    override fun setupView() {
        setupViewPager()
        setupBottomNavigation()
        checkNotificationPermission()
    }

    private fun setupViewPager() {
        mHomeViewPagerAdapter = HomeViewPagerAdapter(requireActivity())
        mHomeViewPagerAdapter.addFragment(ConversationListFragment())
        mHomeViewPagerAdapter.addFragment(ContactFragment())
        binding.vpHome.adapter = mHomeViewPagerAdapter
        binding.vpHome.offscreenPageLimit = 2
        binding.vpHome.isUserInputEnabled = false
    }

    private fun setupBottomNavigation() {
        binding.bnvHome.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.chat -> {
                    binding.vpHome.currentItem = 0
                    true
                }

                R.id.contact -> {
                    binding.vpHome.currentItem = 1
                    true
                }

                else -> false
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            hasNotificationPermissionGranted = requireActivity().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!hasNotificationPermissionGranted) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            hasNotificationPermissionGranted = true
        }
    }

    private fun showSettingDialog() {
        MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.MaterialAlertDialog_Material3).setTitle("Notification Permission")
            .setMessage("Notification permission is required to show notifications for messages, Please allow notification permission from setting. if you deny the notification permission, you will not receive notifications when there are new messages")
            .setPositiveButton("Ok") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:${requireActivity().packageName}")
                startActivity(intent)
            }.setNegativeButton("Cancel", null).show()
    }

    private fun showNotificationPermissionRationale() {

        MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.MaterialAlertDialog_Material3).setTitle("Alert")
            .setMessage("Notification permission is required to show notifications for messages")
            .setPositiveButton("Ok") { _, _ ->
                if (Build.VERSION.SDK_INT >= 33) {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }.setNegativeButton("Cancel", null).show()
    }
}
