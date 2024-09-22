package com.savestatus.wsstatussaver.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.annotation.RequiresApi
import androidx.core.content.IntentCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.savestatus.wsstatussaver.R
import com.savestatus.wsstatussaver.logDefaultClient
import com.savestatus.wsstatussaver.model.WaClient

fun Context.getDefaultClient(): WaClient? {
    val clientPackageName = preferences().defaultClientPackageName
    if (!clientPackageName.isNullOrEmpty()) {
        return getClientIfInstalled(clientPackageName)
    }
    return null
}

fun Context.setDefaultClient(client: WaClient?) {
    logDefaultClient(client?.packageName ?: "cleared")
    preferences().defaultClientPackageName = client?.packageName
}

fun Context.getAllInstalledClients() = WaClient.entries.filter { it.isInstalled(this) }

fun Context.getClientIfInstalled(packageName: String?) =
    getAllInstalledClients().firstOrNull { it.packageName == packageName }

fun Context.getPreferredClient() = getDefaultClient() ?: getAllInstalledClients().firstOrNull()

fun List<WaClient>.getPreferred(context: Context): List<WaClient> {
    val preferred = context.getDefaultClient()
    return if (preferred == null) this else filter { it.packageName == preferred.packageName }
}

@RequiresApi(Build.VERSION_CODES.Q)
fun Context.getClientSAFIntent(client: WaClient): Intent {
    val storageManager = getSystemService<StorageManager>()!!

    val intent = storageManager.primaryStorageVolume.createOpenDocumentTreeIntent()
    val uri = IntentCompat.getParcelableExtra(intent, DocumentsContract.EXTRA_INITIAL_URI, Uri::class.java)
    val encodedPart = ":${client.getSAFDirectoryPath()}".encodedUrl()
    val scheme = uri.toString().replace("/root/", "/document/") + encodedPart

    intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, scheme.toUri())
    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    return intent
}

fun Uri.isFromClient(client: WaClient): Boolean {
    val path = this.encodedPath?.decodedUrl() ?: return false
    if (path.contains(":")) {
        val lastPart = path.split(":")
        if (lastPart.size == 2) {
            return lastPart[1] == client.getSAFDirectoryPath()
        }
    }
    return false
}

fun WaClient.takePermissions(context: Context, result: ActivityResult, isShowToast: Boolean = true): Boolean {
    if (result.resultCode == Activity.RESULT_OK) {
        val uri = result.data?.data ?: return false
        if (uri.isFromClient(this)) {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            if (isShowToast) context.showToast(R.string.permissions_granted_successfully)
            return true
        } else {
            if (isShowToast) context.showToast(R.string.select_the_correct_location, Toast.LENGTH_LONG)
        }
    }
    return false
}