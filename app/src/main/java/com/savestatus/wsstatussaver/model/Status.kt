package com.savestatus.wsstatussaver.model

import android.net.Uri

open class Status(
    val type: StatusType,
    val name: String?,
    val fileUri: Uri,
    val dateModified: Long,
    val size: Long,
    val clientPackage: String?,
    val isSaved: Boolean
)