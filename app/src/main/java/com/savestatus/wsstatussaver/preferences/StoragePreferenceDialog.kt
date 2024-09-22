package com.savestatus.wsstatussaver.preferences

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.savestatus.wsstatussaver.R
import com.savestatus.wsstatussaver.databinding.DialogRecyclerviewBinding
import com.savestatus.wsstatussaver.databinding.ItemStorageVolumeBinding
import com.savestatus.wsstatussaver.storage.Storage
import com.savestatus.wsstatussaver.storage.StorageDevice
import com.savestatus.wsstatussaver.WhatSaveViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class StoragePreferenceDialog : DialogFragment() {

    private val viewModel: WhatSaveViewModel by activityViewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogRecyclerviewBinding.inflate(layoutInflater)

        viewModel.getStorageDevices().observe(this) {
            if (it.isEmpty()) {
                binding.empty.setText(R.string.no_storage_device_found)
                binding.empty.isVisible = true
                binding.recyclerView.isVisible = false
            } else {
                binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.recyclerView.adapter = Adapter(requireContext(), it)
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.statuses_location_title)
            .setView(binding.root)
            .setNegativeButton(R.string.close_action, null)
            .create().also {
                it.setOnShowListener {
                    viewModel.loadStorageDevices()
                }
            }
    }

    private class Adapter constructor(private val context: Context, private val storageVolumes: List<StorageDevice>) :
        RecyclerView.Adapter<Adapter.ViewHolder>(), KoinComponent {

        private val storage: Storage by inject()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(ItemStorageVolumeBinding.inflate(LayoutInflater.from(context), parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val storageVolume = storageVolumes[position]

            holder.icon?.setImageResource(storageVolume.iconRes)
            holder.name?.setText(storageVolume.nameRes)

            if (storage.isDefaultStatusesLocation(storageVolume)) {
                holder.info?.setText(R.string.statuses_location_default)
            } else holder.info?.visibility = View.GONE

            holder.radioButton?.isChecked = storage.isStatusesLocation(storageVolume)
        }

        override fun getItemCount(): Int = storageVolumes.size

        inner class ViewHolder(binding: ItemStorageVolumeBinding) : RecyclerView.ViewHolder(binding.root),
            View.OnClickListener {
            var icon: ImageView? = binding.icon
            var name: TextView? = binding.name
            var info: TextView? = binding.info
            var radioButton: RadioButton? = binding.radioButton

            override fun onClick(view: View) {
                storage.setStatusesLocation(storageVolumes[layoutPosition])
                notifyDataSetChanged()
            }

            init {
                itemView.setOnClickListener(this)
            }
        }

    }
}