package com.savestatus.wsstatussaver.extensions

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Context
import android.content.UriPermission
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import com.savestatus.wsstatussaver.R
import com.savestatus.wsstatussaver.model.RequestedPermissions
import com.savestatus.wsstatussaver.model.WaClient

const val STORAGE_PERMISSION_REQUEST = 100

@SuppressLint("InlinedApi")
fun getRequestedPermissions(): Array<RequestedPermissions> {
    return arrayOf(
        RequestedPermissions(1..Build.VERSION_CODES.P, WRITE_EXTERNAL_STORAGE),
        RequestedPermissions(1..Build.VERSION_CODES.S_V2, READ_EXTERNAL_STORAGE),
        RequestedPermissions(Build.VERSION_CODES.TIRAMISU, READ_MEDIA_IMAGES, READ_MEDIA_VIDEO)
    )
}

fun getApplicablePermissions() = getRequestedPermissions()
    .filter { it.isApplicable() }
    .flatMap { it.permissions.asIterable() }
    .toTypedArray()

fun List<UriPermission>.areValidPermissions(): Boolean {
    return isNotEmpty() && WaClient.entries.any { client -> client.hasPermissions(this) }
}

fun Context.hasStoragePermissions(): Boolean = doIHavePermissions(*getApplicablePermissions())

fun Context.hasSAFPermissions(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || getAllInstalledClients().any { it.hasPermissions(this) }
}

fun Context.hasPermissions() = hasStoragePermissions() && hasSAFPermissions()

fun FragmentActivity.requestWithOnboard() {
    preferences().isShownOnboard = false
    findNavController(R.id.main_container).navigate(R.id.onboardFragment)
}

fun FragmentActivity.requestWithoutOnboard() {
    requestPermissions(getApplicablePermissions(), STORAGE_PERMISSION_REQUEST)
}

fun FragmentActivity.requestPermissions(isShowOnboard: Boolean = false) {
    if (isShowOnboard) {
        val navController = findNavController(R.id.main_container)
        if (navController.currentDestination?.id == R.id.onboardFragment) {
            requestWithoutOnboard()
        } else {
            requestWithOnboard()
        }
    } else {
        requestWithoutOnboard()
    }
}

fun Fragment.hasStoragePermissions() = requireContext().hasStoragePermissions()

fun Fragment.hasPermissions() = requireContext().hasPermissions()

fun Fragment.requestWithoutOnboard() = requireActivity().requestWithoutOnboard()

fun Fragment.requestPermissions() = requireActivity().requestPermissions(true)