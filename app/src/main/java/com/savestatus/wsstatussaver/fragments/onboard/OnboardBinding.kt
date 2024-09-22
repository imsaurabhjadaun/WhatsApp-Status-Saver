package com.savestatus.wsstatussaver.fragments.onboard

import com.savestatus.wsstatussaver.databinding.FragmentOnboardBinding

class OnboardBinding(binding: FragmentOnboardBinding) {
    val subtitle = binding.subtitle
    val agreementText = binding.agreementText
    val storagePermissionView = binding.storagePermissionView.root
    val clientPermissionView = binding.clientPermissionView.root
    val recyclerView = binding.clientPermissionView.recyclerView
    val grantStorageButton = binding.storagePermissionView.grantButton
    val continueButton = binding.continueButton
    val privacyPolicyButton = binding.privacyPolicyButton
}