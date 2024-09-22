package com.savestatus.wsstatussaver.model

import android.net.Uri
import android.os.Environment
import android.provider.MediaStore


internal enum class StatusSaveType(internal val dirType: String, internal val dirName: String,
                          internal val fileMimeType: String, internal val contentUri: Uri) {
    IMAGE_SAVE(Environment.DIRECTORY_DCIM, "Saved Image Statuses", "image/jpeg",
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
    VIDEO_SAVE(Environment.DIRECTORY_DCIM, "Saved Video Statuses", "video/mp4",
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
}