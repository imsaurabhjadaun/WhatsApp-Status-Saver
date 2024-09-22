package com.savestatus.wsstatussaver.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.savestatus.wsstatussaver.R
import com.savestatus.wsstatussaver.extensions.blacklistedSenders
import com.savestatus.wsstatussaver.extensions.formattedAsHtml
import com.savestatus.wsstatussaver.extensions.preferences
import com.savestatus.wsstatussaver.extensions.whitelistMessageSender
import com.savestatus.wsstatussaver.getApp

class BlacklistedSenderDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val blacklisted = preferences().blacklistedSenders()?.toTypedArray()
        if (blacklisted.isNullOrEmpty()) {
            return MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.blacklisted_senders)
                .setMessage(R.string.no_blacklisted_senders)
                .setPositiveButton(android.R.string.ok, null)
                .create()
        }
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.blacklisted_senders)
            .setItems(blacklisted) { _: DialogInterface, which: Int ->
                removeItem(blacklisted[which])
            }
            .setPositiveButton(R.string.close_action, null)
            .create()
    }

    private fun removeItem(name: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.remove_x_from_the_blacklist, name).formattedAsHtml())
            .setPositiveButton(R.string.yes_action) { _: DialogInterface, _: Int ->
                getApp().preferences().whitelistMessageSender(name)
            }
            .setNegativeButton(R.string.no_action, null)
            .show()
    }
}