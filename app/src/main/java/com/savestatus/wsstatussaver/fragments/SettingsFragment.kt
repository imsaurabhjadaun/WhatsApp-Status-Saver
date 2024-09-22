package com.savestatus.wsstatussaver.fragments

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.transition.MaterialFadeThrough
import com.savestatus.wsstatussaver.*
import com.savestatus.wsstatussaver.databinding.FragmentSettingsBinding
import com.savestatus.wsstatussaver.extensions.*
import com.savestatus.wsstatussaver.fragments.base.BaseFragment
import com.savestatus.wsstatussaver.preferences.DefaultClientPreference
import com.savestatus.wsstatussaver.preferences.DefaultClientPreferenceDialog
import com.savestatus.wsstatussaver.preferences.StoragePreference
import com.savestatus.wsstatussaver.preferences.StoragePreferenceDialog

class SettingsFragment : BaseFragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSettingsBinding.bind(view)
        postponeEnterTransition()
        enterTransition = MaterialFadeThrough().addTarget(view)
        reenterTransition = MaterialFadeThrough().addTarget(view)
        view.doOnPreDraw { startPostponedEnterTransition() }
        binding.appBar.statusBarForeground = MaterialShapeDrawable.createWithElevationOverlay(requireContext())
        statusesActivity.setSupportActionBar(binding.toolbar)

        var settingsFragment: SettingsFragment? = whichFragment(R.id.settings_container)
        if (settingsFragment == null) {
            settingsFragment = SettingsFragment()
            childFragmentManager.beginTransaction()
                .replace(R.id.settings_container, settingsFragment)
                .commit()
        } else {
            settingsFragment.invalidatePreferences()
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateMenu(menu, menuInflater)
        menu.clear()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.preferences)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            invalidatePreferences()
        }

        override fun onDisplayPreferenceDialog(preference: Preference) {
            when (preference) {
                is DefaultClientPreference -> {
                    DefaultClientPreferenceDialog().show(childFragmentManager, "INSTALLED_CLIENTS")
                    return
                }

                is StoragePreference -> {
                    StoragePreferenceDialog().show(childFragmentManager, "STORAGE_DIALOG")
                    return
                }
            }
            super.onDisplayPreferenceDialog(preference)
        }

        fun invalidatePreferences() {
            findPreference<Preference>(PREFERENCE_NIGHT_MODE)
                ?.setOnPreferenceChangeListener { _: Preference?, newValue: Any? ->
                    val themeName = newValue as String
                    AppCompatDelegate.setDefaultNightMode(getDefaultDayNightMode(themeName))
                    logThemeSelected(themeName)
                    true
                }
            findPreference<SwitchPreferenceCompat>(PREFERENCE_JUST_BLACK_THEME)
                ?.apply {
                    isEnabled = requireContext().isNightModeEnabled
                    setOnPreferenceChangeListener { _, _ ->
                        requireActivity().recreate()
                        true
                    }
                }
            findPreference<Preference>(PREFERENCE_LONG_PRESS_ACTION)
                ?.setOnPreferenceChangeListener { _: Preference?, o: Any ->
                    val actionName = o as String
                    if (LongPressAction.VALUE_DELETE == actionName) {
                        showToast(R.string.statuses_deletion_is_not_permitted)
                    }
                    logLongPressActionSelected(actionName)
                    true
                }
            findPreference<Preference>(PREFERENCE_LANGUAGE)?.setOnPreferenceChangeListener { _, newValue ->
                val languageName = newValue as String
                if (languageName == "auto") {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                } else {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageName))
                }
                logLanguageSelected(languageName)
                true
            }
            findPreference<Preference>(PREFERENCE_ANALYTICS_ENABLED)?.setOnPreferenceChangeListener { _, newValue ->
                setAnalyticsEnabled((newValue as Boolean))
                true
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                findPreference<Preference>(PREFERENCE_STATUSES_LOCATION)?.isVisible = false
                findPreference<Preference>(PREFERENCE_DEFAULT_CLIENT)?.isVisible = false
                findPreference<Preference>(PREFERENCE_GRANT_PERMISSIONS)?.apply {
                    isVisible = true
                    setOnPreferenceClickListener {
                        findActivityNavController(R.id.main_container)
                            .navigate(R.id.onboardFragment, bundleOf("isFromSettings" to true))
                        true
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    findPreference<Preference>(PREFERENCE_QUICK_DELETION)?.isVisible = false
                }
            }
        }
    }
}