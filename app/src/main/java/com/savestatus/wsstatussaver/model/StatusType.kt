package com.savestatus.wsstatussaver.model

import android.annotation.TargetApi
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.annotation.StringRes
import com.savestatus.wsstatussaver.R
import com.savestatus.wsstatussaver.extensions.getNewSaveName
import java.io.File

enum class StatusType(@StringRes val nameRes: Int, val format: String, private val saveType: StatusSaveType) {
    IMAGE(R.string.type_image, ".jpg", StatusSaveType.IMAGE_SAVE),
    VIDEO(R.string.type_video, ".mp4", StatusSaveType.VIDEO_SAVE);

    fun getDefaultSaveName(timeMillis: Long, delta: Int): String = getNewSaveName(this, timeMillis, delta = delta)

    val contentUri: Uri get() = saveType.contentUri

    val mimeType: String get() = saveType.fileMimeType

    @get:TargetApi(Build.VERSION_CODES.Q)
    val relativePath: String
        get() = String.format("%s/%s", saveType.dirType, saveType.dirName)

    val savesDirectory: File
        get() = File(Environment.getExternalStoragePublicDirectory(saveType.dirType), saveType.dirName)
}