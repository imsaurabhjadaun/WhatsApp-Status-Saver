package com.savestatus.wsstatussaver.mvvm

import android.net.Uri
import com.savestatus.wsstatussaver.model.ShareData
import com.savestatus.wsstatussaver.model.Status

data class DeletionResult(
    val isDeleting: Boolean = false,
    val statuses: List<Status> = arrayListOf(),
    val deleted: Int = 0
) {
    val isSuccess: Boolean
        get() = statuses.size == deleted

    companion object {
        fun single(status: Status, success: Boolean) =
            DeletionResult(false, listOf(status), if (success) 1 else 0)
    }
}

data class SaveResult(
    val isSaving: Boolean = false,
    val statuses: List<Status> = arrayListOf(),
    val uris: List<Uri> = arrayListOf(),
    val saved: Int = 0
) {
    val isSuccess: Boolean
        get() = statuses.isNotEmpty() && uris.isNotEmpty() && statuses.size == uris.size

    companion object {
        fun single(status: Status, uri: Uri?): SaveResult {
            val statuses = if (uri != null) listOf(status) else arrayListOf()
            val uris = if (uri != null) listOf(uri) else arrayListOf()
            return SaveResult(false, statuses, uris, uris.size)
        }
    }
}

data class ShareResult(
    val isLoading: Boolean = false,
    val data: ShareData = ShareData()
) {
    val isSuccess: Boolean
        get() = data.hasData
}