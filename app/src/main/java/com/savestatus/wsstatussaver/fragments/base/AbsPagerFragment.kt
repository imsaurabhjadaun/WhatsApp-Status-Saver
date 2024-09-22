package com.savestatus.wsstatussaver.fragments.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.savestatus.wsstatussaver.R
import com.savestatus.wsstatussaver.WhatSaveViewModel
import com.savestatus.wsstatussaver.adapter.StatusAdapter
import com.savestatus.wsstatussaver.databinding.FragmentStatusesPageBinding
import com.savestatus.wsstatussaver.extensions.createProgressDialog
import com.savestatus.wsstatussaver.extensions.doOnPageSelected
import com.savestatus.wsstatussaver.extensions.getPreferredClient
import com.savestatus.wsstatussaver.extensions.hasR
import com.savestatus.wsstatussaver.extensions.isNullOrEmpty
import com.savestatus.wsstatussaver.extensions.isQuickDeletion
import com.savestatus.wsstatussaver.extensions.isVideo
import com.savestatus.wsstatussaver.extensions.preferences
import com.savestatus.wsstatussaver.extensions.primaryColor
import com.savestatus.wsstatussaver.extensions.requestContext
import com.savestatus.wsstatussaver.extensions.requestPermissions
import com.savestatus.wsstatussaver.extensions.requestView
import com.savestatus.wsstatussaver.extensions.serializable
import com.savestatus.wsstatussaver.extensions.showToast
import com.savestatus.wsstatussaver.extensions.startActivitySafe
import com.savestatus.wsstatussaver.extensions.toPreviewIntent
import com.savestatus.wsstatussaver.fragments.binding.StatusesPageBinding
import com.savestatus.wsstatussaver.interfaces.IMultiStatusCallback
import com.savestatus.wsstatussaver.interfaces.IPermissionChangeListener
import com.savestatus.wsstatussaver.interfaces.IScrollable
import com.savestatus.wsstatussaver.model.Status
import com.savestatus.wsstatussaver.model.StatusQueryResult
import com.savestatus.wsstatussaver.model.StatusType
import com.savestatus.wsstatussaver.mvvm.DeletionResult
import com.savestatus.wsstatussaver.mvvm.SaveResult
import org.koin.androidx.viewmodel.ext.android.activityViewModel

abstract class AbsPagerFragment : BaseFragment(R.layout.fragment_statuses_page),
    View.OnClickListener,
    OnRefreshListener,
    IScrollable,
    IPermissionChangeListener,
    IMultiStatusCallback {

    private var _binding: StatusesPageBinding? = null
    protected val binding get() = _binding!!

    protected val viewModel by activityViewModel<WhatSaveViewModel>()
    protected lateinit var deletionRequestLauncher: ActivityResultLauncher<IntentSenderRequest>
    protected lateinit var statusType: StatusType
    protected var statusAdapter: StatusAdapter? = null

    private val progressDialog by lazy { requireContext().createProgressDialog() }
    private val statusesFragment: AbsStatusesFragment
        get() = parentFragment as AbsStatusesFragment

    private val lastResult: StatusQueryResult?
        get() = viewModel.getStatuses(statusType).value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arguments = arguments
        if (arguments != null) {
            statusType = arguments.serializable(EXTRA_TYPE, StatusType::class)!!
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = StatusesPageBinding(FragmentStatusesPageBinding.bind(view)).apply {
            swipeRefreshLayout.setOnRefreshListener(this@AbsPagerFragment)
            swipeRefreshLayout.setColorSchemeColors(view.context.primaryColor())

            recyclerView.setPadding(resources.displayMetrics.density.toInt() * 4)
            recyclerView.layoutManager =
                GridLayoutManager(requireActivity(), resources.getInteger(R.integer.statuses_grid_span_count))
            recyclerView.adapter = onCreateAdapter().apply {
                registerAdapterDataObserver(adapterDataObserver)
            }.also { newStatusAdapter ->
                statusAdapter = newStatusAdapter
            }
        }

        deletionRequestLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                viewModel.reloadAll()
                showToast(R.string.deletion_success)
            }
        }

        binding.emptyButton.setOnClickListener(this)
        statusesFragment.getViewPager().doOnPageSelected(viewLifecycleOwner) {
            statusAdapter?.finishActionMode()
        }
    }

    protected fun data(result: StatusQueryResult) {
        statusAdapter?.statuses = result.statuses
        binding.swipeRefreshLayout.isRefreshing = result.isLoading
        if (result.code.titleRes != 0) {
            binding.emptyTitle.text = getString(result.code.titleRes)
            binding.emptyTitle.isVisible = true
        } else {
            binding.emptyTitle.isVisible = false
        }
        if (result.code.descriptionRes != 0) {
            binding.emptyText.text = getString(result.code.descriptionRes)
            binding.emptyText.isVisible = true
        } else {
            binding.emptyText.isVisible = false
        }
        if (result.code.buttonTextRes != 0) {
            binding.emptyButton.text = getString(result.code.buttonTextRes)
            binding.emptyButton.isVisible = true
        } else {
            binding.emptyButton.isVisible = false
        }
    }

    protected abstract fun onCreateAdapter(): StatusAdapter

    override fun scrollToTop() {
        binding.recyclerView.scrollToPosition(0)
    }

    override fun onRefresh() {
        onLoadStatuses(statusType)
    }

    override fun onStart() {
        super.onStart()
        statusesActivity.addPermissionsChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        statusesActivity.removePermissionsChangeListener(this)
    }

    override fun onClick(view: View) {
        if (view == binding.emptyButton) {
            val resultCode = lastResult?.code
            if (resultCode != StatusQueryResult.ResultCode.Loading) {
                when (resultCode) {
                    StatusQueryResult.ResultCode.PermissionError -> requestPermissions()
                    StatusQueryResult.ResultCode.NotInstalled -> requireActivity().finish()
                    StatusQueryResult.ResultCode.NoStatuses -> requireContext().getPreferredClient()?.let {
                        startActivitySafe(it.getLaunchIntent(requireContext().packageManager))
                    }

                    else -> onLoadStatuses(statusType)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        statusAdapter?.unregisterAdapterDataObserver(adapterDataObserver)
        statusAdapter = null
    }

    private val adapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            binding.emptyView.isVisible = statusAdapter.isNullOrEmpty()
        }
    }

    override fun permissionsStateChanged(hasPermissions: Boolean) {
        viewModel.reloadAll()
    }

    override fun multiSelectionItemClick(item: MenuItem, selection: List<Status>) {
        when (item.itemId) {
            R.id.action_share -> {
                viewModel.shareStatuses(selection).observe(viewLifecycleOwner) {
                    if (it.isLoading) {
                        progressDialog.show()
                    } else {
                        progressDialog.dismiss()
                        if (it.isSuccess) {
                            startActivitySafe(it.data.createIntent(requireContext()))
                        }
                    }
                }
            }

            R.id.action_save -> {
                viewModel.saveStatuses(selection).observe(viewLifecycleOwner) {
                    processSaveResult(it)
                }
            }

            R.id.action_delete -> {
                if (hasR()) {
                    viewModel.createDeleteRequest(requireContext(), selection).observe(viewLifecycleOwner) {
                        deletionRequestLauncher.launch(IntentSenderRequest.Builder(it).build())
                    }
                } else {
                    if (!preferences().isQuickDeletion()) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.delete_saved_statuses_title)
                            .setMessage(
                                getString(R.string.x_saved_statuses_will_be_permanently_deleted, selection.size)
                            )
                            .setPositiveButton(R.string.delete_action) { _: DialogInterface, _: Int ->
                                viewModel.deleteStatuses(selection).observe(viewLifecycleOwner) {
                                    processDeletionResult(it)
                                }
                            }
                            .setNegativeButton(android.R.string.cancel, null)
                            .show()
                    } else {
                        viewModel.deleteStatuses(selection).observe(viewLifecycleOwner) {
                            processDeletionResult(it)
                        }
                    }
                }
            }
        }
    }

    override fun previewStatusClick(status: Status) = requestContext {
        startActivitySafe(status.toPreviewIntent()) { _: Throwable, activityNotFound: Boolean ->
            if (activityNotFound) {
                requestView { view ->
                    val messageRes = when {
                        status.isVideo -> R.string.there_is_not_an_app_available_to_open_this_video
                        else -> R.string.there_is_not_an_app_available_to_open_this_image
                    }
                    Snackbar.make(view, messageRes, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun saveStatusClick(status: Status) = requestContext { context ->
        if (status.isSaved) {
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.save_again_title)
                .setMessage(R.string.you_saved_this_status_previously)
                .setPositiveButton(R.string.save_action) { _, _ ->
                    saveStatus(status)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        } else {
            saveStatus(status)
        }
    }

    override fun shareStatusClick(status: Status) = requestContext {
        viewModel.shareStatus(status).observe(viewLifecycleOwner) { result ->
            if (result.isLoading) {
                progressDialog.show()
            } else {
                progressDialog.dismiss()
                if (result.isSuccess) {
                    startActivitySafe(result.data.createIntent(requireContext()))
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun saveStatus(status: Status) {
        viewModel.saveStatus(status).observe(viewLifecycleOwner) {
            processSaveResult(it)
        }
    }

    protected abstract fun onLoadStatuses(type: StatusType)

    private fun processSaveResult(result: SaveResult) = requestView { view ->
        if (result.isSaving) {
            Snackbar.make(view, R.string.saving_status, Snackbar.LENGTH_SHORT).show()
        } else {
            if (result.isSuccess) {
                if (result.saved == 1) {
                    Snackbar.make(view, R.string.saved_successfully, Snackbar.LENGTH_SHORT)
                        .setAction(R.string.open_action) {
                            previewStatusClick(result.statuses.single())
                        }
                        .show()
                } else {
                    Snackbar.make(view, getString(R.string.saved_x_statuses, result.saved), Snackbar.LENGTH_SHORT)
                        .show()
                }
                viewModel.reloadAll()
            } else {
                Snackbar.make(view, R.string.failed_to_save, Snackbar.LENGTH_SHORT).show()
            }
        }
        statusAdapter?.isSavingContent = result.isSaving
    }

    protected fun processDeletionResult(result: DeletionResult) = requestView { view ->
        if (result.isDeleting) {
            Snackbar.make(view, R.string.deleting_please_wait, Snackbar.LENGTH_SHORT).show()
        } else if (result.isSuccess) {
            Snackbar.make(view, R.string.deletion_success, Snackbar.LENGTH_SHORT).show()
            viewModel.reloadAll()
        } else {
            Snackbar.make(view, R.string.deletion_failed, Snackbar.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_TYPE = "extra_type"
    }
}