package com.savestatus.wsstatussaver.database

import com.savestatus.wsstatussaver.model.Status

private fun Status.getSaveName(i: String?, timeMillis: Long, delta: Int = 0): String {
    var saveName = i
    if (saveName.isNullOrBlank()) {
        return type.getDefaultSaveName(timeMillis, delta)
    }
    if (!saveName.endsWith(type.format)) {
        saveName += type.format
    }
    while (saveName!!.startsWith(".")) {
        saveName = saveName.drop(1)
    }
    return saveName
}

fun Status.toStatusEntity(saveName: String?, timeMillis: Long = System.currentTimeMillis(), delta: Int = 0) = StatusEntity(
    type = type,
    name = name,
    origin = fileUri,
    dateModified = dateModified,
    size = size,
    client = clientPackage,
    saveName = getSaveName(saveName, timeMillis, delta)
)