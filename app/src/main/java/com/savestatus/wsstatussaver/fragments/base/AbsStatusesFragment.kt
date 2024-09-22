package com.savestatus.wsstatussaver.fragments.base

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.doOnPreDraw
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialFadeThrough
import com.savestatus.wsstatussaver.R
import com.savestatus.wsstatussaver.activities.setupWhatsAppMenuItem
import com.savestatus.wsstatussaver.adapter.PagerAdapter
import com.savestatus.wsstatussaver.databinding.FragmentStatusesBinding
import com.savestatus.wsstatussaver.extensions.PREFERENCE_DEFAULT_CLIENT
import com.savestatus.wsstatussaver.extensions.doOnPageSelected
import com.savestatus.wsstatussaver.extensions.findCurrentFragment
import com.savestatus.wsstatussaver.extensions.preferences
import com.savestatus.wsstatussaver.interfaces.IScrollable
import com.savestatus.wsstatussaver.model.StatusType


abstract class AbsStatusesFragment : BaseFragment(R.layout.fragment_statuses),
    SharedPreferences.OnSharedPreferenceChangeListener, IScrollable {

    private var _binding: FragmentStatusesBinding? = null
    private val binding get() = _binding!!

    private lateinit var tabLayoutMediator: TabLayoutMediator
    protected var pagerAdapter: PagerAdapter? = null
    protected var currentType: StatusType
        get() = StatusType.entries.first { type -> type.ordinal == binding.viewPager.currentItem }
        set(type) {
            binding.viewPager.currentItem = type.ordinal
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStatusesBinding.bind(view).apply {
            appBar.statusBarForeground = MaterialShapeDrawable.createWithElevationOverlay(requireContext())
            viewPager.doOnPageSelected(viewLifecycleOwner) {
                onBackPressedCallback.isEnabled = currentType != StatusType.IMAGE
            }
            viewPager.adapter = onCreatePagerAdapter().also { newPagerAdapter ->
                pagerAdapter = newPagerAdapter
            }
            viewPager.offscreenPageLimit = pagerAdapter!!.itemCount - 1
        }.also { viewBinding ->
            tabLayoutMediator =
                TabLayoutMediator(viewBinding.tabLayout, viewBinding.viewPager) { tab: TabLayout.Tab, position: Int ->
                    tab.text = pagerAdapter?.getPageTitle(position)
                }.also { mediator ->
                    mediator.attach()
                }
        }

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        enterTransition = MaterialFadeThrough().addTarget(view)
        reenterTransition = MaterialFadeThrough().addTarget(view)

        statusesActivity.setSupportActionBar(binding.toolbar)
        statusesActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        preferences().registerOnSharedPreferenceChangeListener(this)
    }

    protected abstract fun onCreatePagerAdapter(): PagerAdapter

    override fun onSharedPreferenceChanged(preferences: SharedPreferences?, key: String?) {
        if (PREFERENCE_DEFAULT_CLIENT == key) {
            _binding?.apply { toolbar.menu?.setupWhatsAppMenuItem(requireActivity()) }
        }
    }

    override fun onDestroyView() {
        preferences().unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroyView()
        tabLayoutMediator.detach()
        _binding = null
    }

    override fun scrollToTop() {
        val currentFragment = binding.viewPager.findCurrentFragment(childFragmentManager)
        if (currentFragment is AbsPagerFragment) {
            currentFragment.scrollToTop()
        }
    }

    internal fun getViewPager() = binding.viewPager

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            currentType = StatusType.IMAGE
        }
    }
}