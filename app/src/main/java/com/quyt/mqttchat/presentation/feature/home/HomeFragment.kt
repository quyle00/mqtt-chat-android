package com.quyt.mqttchat.presentation.feature.home

import androidx.fragment.app.viewModels
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.FragmentHomeBinding
import com.quyt.mqttchat.presentation.adapter.HomeViewPagerAdapter
import com.quyt.mqttchat.presentation.base.BaseBindingFragment
import com.quyt.mqttchat.presentation.feature.home.contact.ContactFragment
import com.quyt.mqttchat.presentation.feature.home.message.ConversationListFragment

class HomeFragment() : BaseBindingFragment<FragmentHomeBinding, HomeViewModel>() {
    override fun layoutId(): Int = R.layout.fragment_home
    override val viewModel: HomeViewModel by viewModels()

    private lateinit var mHomeViewPagerAdapter: HomeViewPagerAdapter
    override fun setupView() {
        setupViewPager()
        setupBottomNavigation()
    }

    private fun setupViewPager() {
        mHomeViewPagerAdapter = HomeViewPagerAdapter(requireActivity())
        mHomeViewPagerAdapter.addFragment(ConversationListFragment())
        mHomeViewPagerAdapter.addFragment(ContactFragment())
        binding.vpHome.adapter = mHomeViewPagerAdapter
        binding.vpHome.offscreenPageLimit = 2
        binding.vpHome.isUserInputEnabled = false

    }

    private fun setupBottomNavigation(){
        binding.bnvHome.setOnNavigationItemSelectedListener {
            when(it.itemId){
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

}