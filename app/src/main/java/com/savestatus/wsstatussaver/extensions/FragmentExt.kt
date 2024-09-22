package com.savestatus.wsstatussaver.extensions

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.IntegerRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment

fun Fragment.getIntRes(@IntegerRes int: Int): Int {
    return resources.getInteger(int)
}

fun Fragment.findActivityNavController(id: Int) = requireActivity().findNavController(id)

@Suppress("UNCHECKED_CAST")
fun <T : Fragment?> FragmentActivity.whichFragment(containerId: Int): T? {
    if (containerId != View.NO_ID) {
        val fragment = supportFragmentManager.findFragmentById(containerId) ?: return null
        return fragment as T
    }
    return null
}

@Suppress("UNCHECKED_CAST")
fun <T> Fragment.whichFragment(@IdRes id: Int): T {
    return childFragmentManager.findFragmentById(id) as T
}

fun Fragment.requestContext(consumer: ContextConsumer) {
    val context = context ?: return
    consumer(context)
}

fun Fragment.requestView(consumer: ViewConsumer) {
    val view = view ?: return
    consumer(view)
}

/**
 * Tries to start an activity using the given [Intent],
 * handling the case where the activity cannot be found.
 */
fun Fragment.startActivitySafe(intent: Intent?, onError: ExceptionConsumer? = null) {
    intent.doWithIntent(onError) { startActivity(it) }
}

fun Fragment.showToast(messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    context?.showToast(messageRes, duration)
}

fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    context?.showToast(message, duration)
}

fun Fragment.getOnBackPressedDispatcher() = requireActivity().onBackPressedDispatcher

fun AppCompatActivity.currentFragment(navHostId: Int): Fragment? {
    val navHostFragment: NavHostFragment =
        supportFragmentManager.findFragmentById(navHostId) as NavHostFragment
    return navHostFragment.childFragmentManager.fragments.firstOrNull()
}