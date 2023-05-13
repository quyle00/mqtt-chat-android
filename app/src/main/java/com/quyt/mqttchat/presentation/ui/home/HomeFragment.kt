package com.quyt.mqttchat.presentation.ui.home

import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.FragmentHomeBinding
import com.quyt.mqttchat.presentation.adapter.HomeViewPagerAdapter
import com.quyt.mqttchat.presentation.base.BaseBindingFragment
import com.quyt.mqttchat.presentation.ui.home.contact.ContactFragment
import com.quyt.mqttchat.presentation.ui.home.message.ConversationListFragment

class HomeFragment() : BaseBindingFragment<FragmentHomeBinding, HomeViewModel>() {
    override fun layoutId(): Int = R.layout.fragment_home
    override val viewModel: HomeViewModel by viewModels()

    private lateinit var mHomeViewPagerAdapter: HomeViewPagerAdapter
    override fun setupView() {
        setupViewPager()
        setupBottomNavigation()
    }

    private fun setupViewPager() {
        mHomeViewPagerAdapter = HomeViewPagerAdapter(childFragmentManager)
        mHomeViewPagerAdapter.addFragment(ConversationListFragment())
        mHomeViewPagerAdapter.addFragment(ContactFragment())
        binding.vpHome.adapter = mHomeViewPagerAdapter
//        binding.vpHome.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
//            override fun onPageScrollStateChanged(state: Int) {}
//
//            override fun onPageScrolled(
//                position: Int,
//                positionOffset: Float,
//                positionOffsetPixels: Int
//            ) {}
//
//            override fun onPageSelected(position: Int) {
//                binding.bnvHome.menu.getItem(position).isChecked = true
//            }
//        })
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