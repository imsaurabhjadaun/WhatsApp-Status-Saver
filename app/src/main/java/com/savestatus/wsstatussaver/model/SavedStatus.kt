package com.savestatus.wsstatussaver.model

import android.net.Uri
import com.savestatus.wsstatussaver.extensions.canonicalOrAbsolutePath
import java.io.File

class SavedStatus(
    type: StatusType,
    name: String,
    fileUri: Uri,
    dateModified: Long,
    size: Long,
    private val path: String?
) : Status(type, name, fileUri, dateModified, size, null, true) {

    fun hasFile(): Boolean = !path.isNullOrBlank()

    fun getFile(): File {
        checkNotNull(path)
        return File(path)
    }

    fun getFilePath(): String {
        return getFile().canonicalOrAbsolutePath()
    }
}