package com.savestatus.wsstatussaver.fragments.binding

import com.savestatus.wsstatussaver.databinding.FragmentStatusesPageBinding

class StatusesPageBinding(private val binding: FragmentStatusesPageBinding) {
    val swipeRefreshLayout = binding.swipeRefreshLayout
    val recyclerView = binding.recyclerView
    val emptyView = binding.emptyView.root
    val emptyTitle = binding.emptyView.emptyTitle
    val emptyText = binding.emptyView.emptyText
    val emptyButton = binding.emptyView.emptyButton
}