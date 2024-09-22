package com.savestatus.wsstatussaver.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.savestatus.wsstatussaver.R
import com.savestatus.wsstatussaver.WhatSaveViewModel
import com.savestatus.wsstatussaver.databinding.DialogUpdateInfoBinding
import com.savestatus.wsstatussaver.extensions.lastUpdateSearch
import com.savestatus.wsstatussaver.extensions.preferences
import com.savestatus.wsstatussaver.extensions.showToast
import com.savestatus.wsstatussaver.logUpdateRequest
import com.savestatus.wsstatussaver.update.GitHubRelease
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class UpdateDialog : BottomSheetDialogFragment(), View.OnClickListener {

    private var _binding: DialogUpdateInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModel<WhatSaveViewModel>()

    private lateinit var release: GitHubRelease

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        release = BundleCompat.getParcelable(requireArguments(), EXTRA_RELEASE, GitHubRelease::class.java)!!
       /* if (release.isNewer(requireContext())) {
            _binding = DialogUpdateInfoBinding.inflate(layoutInflater)
            binding.downloadAction.setOnClickListener(this)
            binding.cancelAction.setOnClickListener(this)
            fillVersionInfo()
            return BottomSheetDialog(requireContext()).also {
                it.setContentView(binding.root)
                it.setOnShowListener {
                    preferences().lastUpdateSearch = System.currentTimeMillis()
                }
            }
        }*/

        return MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.the_app_is_up_to_date)
            .setPositiveButton(android.R.string.ok, null)
            .create()
    }

    override fun onClick(view: View) {
        when (view) {
            binding.downloadAction -> {
                viewModel.downloadUpdate(requireContext(), release)
                showToast(R.string.downloading_update)
                logUpdateRequest(release.name, true)
            }

            binding.cancelAction -> {
                release.setIgnored(requireContext())
                logUpdateRequest(release.name, false)
            }
        }
        dismiss()
    }

    @SuppressLint("SetTextI18n")
    private fun fillVersionInfo() {
        binding.versionName.text = release.name
        if (release.body.isNotEmpty()) {
            binding.versionInfo.text = release.body
        } else {
            binding.versionInfo.isVisible = false
        }
        val date = release.getFormattedDate()
        val size = release.getDownloadSize()
        if (date != null && size != null) {
            binding.versionDetail.text = "$date • $size"
        } else if (date != null) {
            binding.versionDetail.text = date
        } else if (size != null) {
            binding.versionDetail.text = size
        } else {
            binding.versionDetail.isVisible = false
        }
    }

    companion object {
        private const val EXTRA_RELEASE = "extra_release"

        fun create(release: GitHubRelease) = UpdateDialog().apply {
            arguments = bundleOf(EXTRA_RELEASE to release)
        }
    }
}