package com.savestatus.wsstatussaver.fragments

import android.app.Activity
import android.app.KeyguardManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.getSystemService
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialFadeThrough
import com.savestatus.wsstatussaver.R
import com.savestatus.wsstatussaver.WhatSaveViewModel
import com.savestatus.wsstatussaver.databinding.FragmentToolBinding
import com.savestatus.wsstatussaver.extensions.isMessageViewEnabled
import com.savestatus.wsstatussaver.extensions.isNotificationListener
import com.savestatus.wsstatussaver.extensions.preferences
import com.savestatus.wsstatussaver.fragments.base.BaseFragment
import com.savestatus.wsstatussaver.logToolView
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class ToolFragment : BaseFragment(R.layout.fragment_tool) {

    private val viewModel: WhatSaveViewModel by activityViewModel()
    private val keyguardManager: KeyguardManager by lazy { requireContext().getSystemService()!! }
    private lateinit var credentialsRequestLauncher: ActivityResultLauncher<Intent>

    private var _binding: FragmentToolBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentToolBinding.bind(view)
        binding.msgANumber.setOnClickListener {
            logToolView("MessageFragment", "Message a number")
            findNavController().navigate(R.id.messageFragment)
        }
        binding.messageView.setOnClickListener {
            logToolView("ConversationListFragment", "Message view")
            if (requireContext().isNotificationListener()) {
                openMessageView()
            } else {
                findNavController().navigate(R.id.messageViewTermsFragment)
            }
        }

        statusesActivity.setSupportActionBar(binding.toolbar)
        credentialsRequestLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                viewModel.unlockMessageView()
            } else {
                viewModel.getMessageViewLockObservable().removeObserver(credentialObserver)
            }
        }

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        enterTransition = MaterialFadeThrough().addTarget(view)
        reenterTransition = MaterialFadeThrough().addTarget(view)
    }

    private fun openMessageView() {
        viewModel.getMessageViewLockObservable().observe(viewLifecycleOwner, credentialObserver)
    }

    @Suppress("DEPRECATION")
    private val credentialObserver = Observer<Boolean> { isUnlocked ->
        if (isUnlocked || !preferences().isMessageViewEnabled) {
            findNavController().navigate(R.id.conversationsFragment)
        } else {
            val credentialsRequestIntel = keyguardManager.createConfirmDeviceCredentialIntent(
                getString(R.string.message_view),
                getString(R.string.confirm_device_credentials)
            )
            if (credentialsRequestIntel != null) {
                credentialsRequestLauncher.launch(credentialsRequestIntel)
            } else {
                viewModel.unlockMessageView()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}