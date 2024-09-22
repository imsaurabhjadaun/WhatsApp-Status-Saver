package com.savestatus.wsstatussaver.fragments.messageview

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFadeThrough
import com.savestatus.wsstatussaver.R
import com.savestatus.wsstatussaver.WhatSaveViewModel
import com.savestatus.wsstatussaver.adapter.MessageAdapter
import com.savestatus.wsstatussaver.database.Conversation
import com.savestatus.wsstatussaver.database.MessageEntity
import com.savestatus.wsstatussaver.databinding.FragmentMessagesBinding
import com.savestatus.wsstatussaver.extensions.startActivitySafe
import com.savestatus.wsstatussaver.extensions.toChooser
import com.savestatus.wsstatussaver.fragments.base.BaseFragment
import com.savestatus.wsstatussaver.interfaces.IMessageCallback
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class ConversationDetailFragment : BaseFragment(R.layout.fragment_messages), IMessageCallback {

    private val arguments by navArgs<ConversationDetailFragmentArgs>()
    private val viewModel: WhatSaveViewModel by activityViewModel()

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    private var adapter: MessageAdapter? = null

    private val conversation: Conversation
        get() = arguments.extraConversation

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMessagesBinding.bind(view)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        enterTransition = MaterialFadeThrough().addTarget(view)
        reenterTransition = MaterialFadeThrough().addTarget(view)

        setupToolbar()
        setupRecyclerView()

        viewModel.receivedMessages(conversation).observe(viewLifecycleOwner) {
            data(it)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.messages_from_x, conversation.name)
        statusesActivity.setSupportActionBar(binding.toolbar)
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter(requireContext(), arrayListOf(), this)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun data(messages: List<MessageEntity>) {
        adapter?.data(messages)
        if (messages.isEmpty()) {
            findNavController().popBackStack()
        }
    }

    override fun messageClick(message: MessageEntity) {
        startActivitySafe(
            Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, message.content)
                .toChooser(getString(R.string.share_with))
        )
    }

    override fun messageLongClick(message: MessageEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_message_title)
            .setMessage(R.string.delete_message_confirmation)
            .setPositiveButton(R.string.yes_action) { _: DialogInterface, _: Int ->
                viewModel.deleteMessage(message)
            }
            .setNegativeButton(R.string.no_action, null)
            .show()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateMenu(menu, menuInflater)
        menu.removeItem(R.id.action_settings)
    }
}