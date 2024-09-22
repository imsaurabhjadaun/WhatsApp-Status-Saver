package com.savestatus.wsstatussaver.fragments.message

import com.savestatus.wsstatussaver.databinding.FragmentMessageANumberBinding

class MessageBinding(binding: FragmentMessageANumberBinding) {
    val appBar = binding.appBar
    val toolbar = binding.toolbar
    val collapsingToolbarLayout = binding.collapsingToolbarLayout
    val phoneInputLayout = binding.messageANumberContent.phoneNumberInputLayout
    val messageInputLayout = binding.messageANumberContent.messageInputLayout
    val phoneNumber = binding.messageANumberContent.phoneNumber
    val message = binding.messageANumberContent.message
    val sendButton = binding.messageANumberContent.sendButton
}