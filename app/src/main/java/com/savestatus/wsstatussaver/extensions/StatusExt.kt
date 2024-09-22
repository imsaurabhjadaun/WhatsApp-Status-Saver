package com.savestatus.wsstatussaver.extensions

import android.content.Context
import android.content.Intent
import com.savestatus.wsstatussaver.R
import com.savestatus.wsstatussaver.model.Status
import com.savestatus.wsstatussaver.model.StatusType
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.text.SimpleDateFormat.getDateTimeInstance
import java.util.Date
import java.util.Locale

val fileDateFormat: DateFormat by lazy {
    SimpleDateFormat("MMM_d_yyyy_HH.mm.ss", Locale.ENGLISH)
}

fun Status.getFormattedDate(context: Context): String {
    val date = Date(dateModified)
    val resLocale = context.resources.configuration.locales[0]
    return getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, resLocale).format(date)
}

/**
 * Generates and returns a new save name depending on the
 * given [StatusType] format and the current time.
 */
fun getNewSaveName(type: StatusType? = null, timeMillis: Long, delta: Int): String {
    var saveName = String.format("Status_%s", fileDateFormat.format(Date(timeMillis)))
    if (delta > 0) {
        saveName += "-$delta"
    }
    if (type != null) {
        saveName += type.format
    }
    return saveName
}

fun Status.getState(context: Context): String =
    if (isSaved) context.getString(R.string.status_saved) else context.getString(R.string.status_unsaved)

val Status.isVideo: Boolean get() = type == StatusType.VIDEO

/**
 * Creates an [Intent] that can be used to open this status
 * in other apps.
 */
fun Status.toPreviewIntent() =
    Intent(Intent.ACTION_VIEW)
        .setDataAndType(fileUri, type.mimeType)
        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

fun StatusType.acceptFileName(fileName: String): Boolean = !fileName.startsWith(".") && fileName.endsWith(this.format)