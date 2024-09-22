package com.savestatus.wsstatussaver.extensions

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.savestatus.wsstatussaver.R
import com.savestatus.wsstatussaver.databinding.DialogProgressBinding
import com.savestatus.wsstatussaver.databinding.DialogStatusOptionsBinding
import com.savestatus.wsstatussaver.interfaces.IStatusCallback
import com.savestatus.wsstatussaver.model.Status

private typealias StatusBinding = DialogStatusOptionsBinding
private typealias ViewCallback = (View) -> Unit

fun Context.createProgressDialog(): Dialog {
    val builder = MaterialAlertDialogBuilder(this)
    val binding = DialogProgressBinding.inflate(LayoutInflater.from(builder.context))
    return builder.setView(binding.root).setCancelable(false).create()
}

fun Context.showStatusOptions(
    status: Status,
    isSaveEnabled: Boolean,
    isDeleteEnabled: Boolean,
    callback: IStatusCallback
): Dialog {
    val binding = StatusBinding.inflate(LayoutInflater.from(this))
    val bottomSheetDialog = BottomSheetDialog(this)
    bottomSheetDialog.setContentView(binding.root)
    bottomSheetDialog.setOnShowListener {
        binding.setupPreview(status)
        binding.setupSave(isSaveEnabled) {
            bottomSheetDialog.dismiss()
            callback.saveStatusClick(status)
        }
        binding.setupDelete(isDeleteEnabled) {
            bottomSheetDialog.dismiss()
            callback.deleteStatusClick(status)
        }
        binding.setupListeners {
            bottomSheetDialog.dismiss()
            when (it) {
                binding.shareAction -> callback.shareStatusClick(status)
                binding.image -> callback.previewStatusClick(status)
            }
        }

        Glide.with(this)
            .asBitmap()
            .load(status.fileUri)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(binding.image)
    }
    return bottomSheetDialog.also {
        it.show()
    }
}

private fun StatusBinding.setupPreview(status: Status) {
    if (status.isVideo) {
        previewAction.setCompoundDrawablesRelativeWithIntrinsicBounds(
            R.drawable.ic_round_play_arrow_24dp, 0, 0, 0
        )
        previewAction.setText(R.string.play_video_action)
    } else {
        previewAction.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_image_24dp, 0, 0, 0)
        previewAction.setText(R.string.view_image_action)
    }
}

private fun StatusBinding.setupSave(isSaveEnabled: Boolean, callback: ViewCallback) {
    if (isSaveEnabled)
        saveAction.setOnClickListener(callback)
    else saveAction.isVisible = false
}

private fun StatusBinding.setupDelete(isDeleteEnabled: Boolean, callback: ViewCallback) {
    if (isDeleteEnabled)
        deleteAction.setOnClickListener(callback)
    else deleteAction.isVisible = false
}

private fun StatusBinding.setupListeners(callback: ViewCallback) {
    shareAction.setOnClickListener(callback)
    image.setOnClickListener(callback)
}