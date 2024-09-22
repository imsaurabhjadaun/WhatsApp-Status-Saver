package com.savestatus.wsstatussaver.fragments.pager

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.savestatus.wsstatussaver.adapter.StatusAdapter
import com.savestatus.wsstatussaver.extensions.*
import com.savestatus.wsstatussaver.fragments.base.AbsPagerFragment
import com.savestatus.wsstatussaver.model.Status
import com.savestatus.wsstatussaver.model.StatusQueryResult
import com.savestatus.wsstatussaver.model.StatusType

class HomeStatusesFragment : AbsPagerFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getStatuses(statusType).apply {
            observe(viewLifecycleOwner) { result ->
                data(result)
            }
        }.also { liveData ->
            if (liveData.value == StatusQueryResult.Idle) {
                onLoadStatuses(statusType)
            }
        }
        preferences().registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateAdapter(): StatusAdapter {
        return StatusAdapter(
            requireActivity(),
            Glide.with(this),
            this,
            isSaveEnabled = true,
            isDeleteEnabled = false,
            isWhatsAppIconEnabled = preferences().isWhatsappIcon()
        )
    }

    override fun onDestroyView() {
        preferences().unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroyView()
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String?) {
        when (key) {
            PREFERENCE_DEFAULT_CLIENT,
            PREFERENCE_STATUSES_LOCATION,
            PREFERENCE_EXCLUDE_OLD_STATUSES,
            PREFERENCE_EXCLUDE_SAVED_STATUSES -> onLoadStatuses(statusType)

            PREFERENCE_WHATSAPP_ICON -> statusAdapter?.isWhatsAppIconEnabled = preferences.isWhatsappIcon()
        }
    }

    override fun deleteStatusClick(status: Status) {}

    override fun onLoadStatuses(type: StatusType) {
        viewModel.loadStatuses(type)
    }

}