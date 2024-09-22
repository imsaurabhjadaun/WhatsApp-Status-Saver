package com.savestatus.wsstatussaver.update

import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Environment
import android.os.Parcelable
import androidx.core.content.edit
import androidx.core.net.toUri
import com.google.gson.annotations.SerializedName
import com.savestatus.wsstatussaver.R
import com.savestatus.wsstatussaver.extensions.packageInfo
import com.savestatus.wsstatussaver.extensions.preferences
import com.savestatus.wsstatussaver.extensions.toFileSize
//import io.github.g00fy2.versioncompare.Version
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
class GitHubRelease(
    @SerializedName("name")
    val name: String,
    @SerializedName("tag_name")
    val tag: String,
    @SerializedName("html_url")
    val url: String,
    @SerializedName("published_at")
    val date: String,
    @SerializedName("body")
    val body: String,
    @SerializedName("prerelease")
    val isPrerelease: Boolean,
    @SerializedName("assets")
    val downloads: List<ReleaseAsset>
) : Parcelable {

    companion object {
        private const val IGNORED_RELEASE = "ignored_release"
    }

    /*fun isDownloadable(context: Context): Boolean {
        val hasApk = downloads.any { it.isApk }
        if (hasApk) {
            return isNewer(context) && !isIgnored(context)
        }
        return false
    }*/

    private fun isIgnored(context: Context): Boolean {
        return context.preferences().getString(IGNORED_RELEASE, null) == tag
    }

    fun setIgnored(context: Context) {
        context.preferences().edit {
            putString(IGNORED_RELEASE, tag)
        }
    }

    /*fun isNewer(context: Context): Boolean {
        try {
            val packageInfo = context.packageManager.packageInfo()
            val installedVersionName = packageInfo.versionName ?: return true
            var updateVersionName = this.tag
            if (updateVersionName.startsWith("v", ignoreCase = true)) {
                updateVersionName = updateVersionName.substring(1)
            }
           // return Version(updateVersionName) > Version(installedVersionName)
        } catch (ignored: NameNotFoundException) {
        }
        return true // assume true
    }*/

    fun getDownloadSize(): String? {
        val assetSize = downloads.firstOrNull { it.isApk }
        return assetSize?.size?.toFileSize()
    }

    fun getDownloadRequest(context: Context): DownloadManager.Request? {
        val apkAsset = downloads.firstOrNull { it.isApk }
        if (apkAsset != null) {
            return DownloadManager.Request(apkAsset.downloadUrl.toUri())
                .setTitle(apkAsset.name)
                .setDescription(context.getString(R.string.downloading_update))
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkAsset.name)
                .setMimeType(ReleaseAsset.APK_MIME_TYPE)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        }
        return null
    }

    fun getFormattedDate(): String? {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val date = format.parse(date)
        if (date != null) {
            return SimpleDateFormat.getDateInstance().format(date)
        }
        return null
    }
}

@Parcelize
class ReleaseAsset(
    @SerializedName("name")
    val name: String,
    @SerializedName("content_type")
    val contentType: String,
    @SerializedName("state")
    val state: String,
    @SerializedName("size")
    val size: Long,
    @SerializedName("browser_download_url")
    val downloadUrl: String
) : Parcelable {

    val isApk: Boolean
        get() = contentType == APK_MIME_TYPE

    companion object {
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    }
}