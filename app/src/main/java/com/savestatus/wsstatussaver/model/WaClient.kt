package com.savestatus.wsstatussaver.model

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.UriPermission
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import com.savestatus.wsstatussaver.R
import com.savestatus.wsstatussaver.extensions.formattedAsHtml
import com.savestatus.wsstatussaver.extensions.getDrawableCompat
import com.savestatus.wsstatussaver.extensions.isFromClient
import com.savestatus.wsstatussaver.extensions.packageInfo

enum class WaClient(
    val displayName: String,
    private val internalName: String,
    val packageName: String,
    private val iconRes: Int
) {
    WhatsApp("WA Messenger", "WhatsApp", "com.whatsapp", R.drawable.icon_wa),
    Business("WA Business", "WhatsApp Business", "com.whatsapp.w4b", R.drawable.icon_business),
    OGWhatsApp("OGWhatsApp", "OGWhatsApp", "com.gbwhatsapp3", R.drawable.icon_gb);

    fun getIcon(context: Context): Drawable? = context.getDrawableCompat(iconRes)

    fun getDescription(context: Context): CharSequence {
        val versionName = resolvePackageValue(context) { it?.versionName }
        if (versionName == null) {
            return context.getString(R.string.client_info_unknown)
        }
        return context.getString(R.string.client_info, versionName).formattedAsHtml()
    }

    fun isInstalled(context: Context): Boolean {
        return try {
            context.packageManager.packageInfo(packageName)
            true
        } catch (ignored: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun hasPermissions(context: Context): Boolean {
        return hasPermissions(context.contentResolver.persistedUriPermissions)
    }

    fun hasPermissions(uriPermissions: List<UriPermission>): Boolean {
        return uriPermissions.any { it.isReadPermission && it.uri.isFromClient(this) }
    }

    fun releasePermissions(context: Context): Boolean {
        val uriPermissions = context.contentResolver.persistedUriPermissions
        for (perm in uriPermissions) {
            if (perm.uri.isFromClient(this)) {
                val flags =
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.releasePersistableUriPermission(perm.uri, flags)
                return true
            }
        }
        return false
    }

    fun getLaunchIntent(packageManager: PackageManager): Intent? {
        return packageManager.getLaunchIntentForPackage(packageName)
    }

    fun getDirectoryPath(): Array<String> {
        return arrayOf(
            "$internalName/Media/.Statuses",
            "Android/media/$packageName/$internalName/Media/.Statuses"
        )
    }

    @TargetApi(Build.VERSION_CODES.Q)
    fun getSAFDirectoryPath(): String {
        return "Android/media/$packageName/$internalName/Media/.Statuses"
    }

    private fun <T> resolvePackageValue(context: Context, resolver: (PackageInfo?) -> T): T? {
        if (packageName.isEmpty()) {
            return null
        }
        val packageInfo = runCatching { context.packageManager.packageInfo(packageName) }
        if (packageInfo.isSuccess) {
            return resolver(packageInfo.getOrThrow())
        }
        return resolver(null)
    }
}