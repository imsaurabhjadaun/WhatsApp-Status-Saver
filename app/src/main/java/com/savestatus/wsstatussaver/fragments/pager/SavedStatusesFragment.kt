package com.savestatus.wsstatussaver.fragments.pager

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.activity.result.IntentSenderRequest
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.savestatus.wsstatussaver.R
import com.savestatus.wsstatussaver.adapter.StatusAdapter
import com.savestatus.wsstatussaver.extensions.hasR
import com.savestatus.wsstatussaver.extensions.isQuickDeletion
import com.savestatus.wsstatussaver.extensions.preferences
import com.savestatus.wsstatussaver.extensions.requestContext
import com.savestatus.wsstatussaver.fragments.base.AbsPagerFragment
import com.savestatus.wsstatussaver.model.Status
import com.savestatus.wsstatussaver.model.StatusQueryResult
import com.savestatus.wsstatussaver.model.StatusType

class SavedStatusesFragment : AbsPagerFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getSavedStatuses(statusType).apply {
            observe(viewLifecycleOwner) { result ->
                data(result)
            }
        }.also { liveData ->
            if (liveData.value == StatusQueryResult.Idle) {
                onLoadStatuses(statusType)
            }
        }
    }

    override fun onCreateAdapter(): StatusAdapter =
        StatusAdapter(
            requireActivity(),
            Glide.with(this),
            this,
            isSaveEnabled = false,
            isDeleteEnabled = true,
            isWhatsAppIconEnabled = false
        )

    override fun saveStatusClick(status: Status) {
        // do nothing
    }

    override fun deleteStatusClick(status: Status) {
        requestContext { context ->
            if (hasR()) {
                viewModel.createDeleteRequest(requireContext(), listOf(status)).observe(viewLifecycleOwner) {
                    deletionRequestLauncher.launch(IntentSenderRequest.Builder(it).build())
                }
            } else {
                if (!preferences().isQuickDeletion()) {
                    MaterialAlertDialogBuilder(context).setTitle(R.string.delete_saved_status_title)
                        .setMessage(R.string.this_saved_status_will_be_permanently_deleted)
                        .setPositiveButton(R.string.delete_action) { _: DialogInterface, _: Int ->
                            viewModel.deleteStatus(status).observe(viewLifecycleOwner) {
                                processDeletionResult(it)
                            }
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                } else {
                    viewModel.deleteStatus(status).observe(viewLifecycleOwner) {
                        processDeletionResult(it)
                    }
                }
            }
        }
    }

    override fun onLoadStatuses(type: StatusType) {
        viewModel.loadSavedStatuses(type)
    }

}