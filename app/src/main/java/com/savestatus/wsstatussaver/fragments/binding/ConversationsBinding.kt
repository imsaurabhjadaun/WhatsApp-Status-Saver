package com.savestatus.wsstatussaver.fragments.binding

import com.savestatus.wsstatussaver.databinding.FragmentConversationsBinding

class ConversationsBinding(binding: FragmentConversationsBinding) {
    val toolbar = binding.toolbar
    val switchWithContainer = binding.conversationsContent.switchView
    val recyclerView = binding.conversationsContent.recyclerView
    val emptyView = binding.conversationsContent.emptyConversationsView.root
    val emptyTitle = binding.conversationsContent.emptyConversationsView.text1
    val emptyText = binding.conversationsContent.emptyConversationsView.text2
}