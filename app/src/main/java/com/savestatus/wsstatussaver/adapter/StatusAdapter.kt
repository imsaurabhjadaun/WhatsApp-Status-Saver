
@file:Suppress("LeakingThis")

package com.savestatus.wsstatussaver.adapter

import android.annotation.SuppressLint
import android.view.*
import android.view.View.OnLongClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.savestatus.wsstatussaver.R
import com.savestatus.wsstatussaver.databinding.ItemStatusBinding
import com.savestatus.wsstatussaver.extensions.*
import com.savestatus.wsstatussaver.interfaces.IMultiStatusCallback
import com.savestatus.wsstatussaver.model.Status
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

@SuppressLint("NotifyDataSetChanged")
open class StatusAdapter(
    protected val activity: FragmentActivity,
    private val requestManager: RequestManager,
    private val callback: IMultiStatusCallback,
    private var isSaveEnabled: Boolean,
    private var isDeleteEnabled: Boolean,
    isWhatsAppIconEnabled: Boolean
) : RecyclerView.Adapter<StatusAdapter.ViewHolder>(), ActionMode.Callback {

    var actionMode: ActionMode? = null
    private val checked = ArrayList<Status>()

    var statuses: List<Status> by Delegates.observable(ArrayList()) { _: KProperty<*>, _: List<Status>, _: List<Status> ->
        notifyDataSetChanged()
    }
    var isSavingContent by Delegates.observable(false) { _: KProperty<*>, _: Boolean, _: Boolean ->
        notifyDataSetChanged()
    }
    var isWhatsAppIconEnabled by Delegates.observable(isWhatsAppIconEnabled) { _: KProperty<*>, _: Boolean, _: Boolean ->
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_status, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val status = statuses[position]

        holder.itemView.isActivated = isItemSelected(status)

        if (holder.image != null) {
            requestManager.load(status.fileUri)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .format(DecodeFormat.PREFER_RGB_565)
                .centerCrop()
                .into(holder.image)
        }

        if (holder.date != null && holder.date.isVisible) {
            holder.date.text = status.getFormattedDate(activity)
        }

        if (holder.state != null && holder.state.isVisible) {
            holder.state.text = status.getState(activity)
        }

        if (holder.clientIcon != null) {
            holder.clientIcon.isVisible = false
            holder.clientIcon.setImageDrawable(null)
            if (isWhatsAppIconEnabled) {
                val client = activity.getClientIfInstalled(status.clientPackage)
                if (client != null) {
                    holder.clientIcon.isVisible = true
                    holder.clientIcon.setImageDrawable(client.getIcon(activity))
                }
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return statuses[position].hashCode().toLong()
    }

    override fun getItemCount(): Int {
        return statuses.size
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        val menuInflater = mode.menuInflater
        menuInflater.inflate(R.menu.menu_statuses_selection, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        if (!isSaveEnabled) {
            menu.removeItem(R.id.action_save)
        }
        if (!isDeleteEnabled) {
            menu.removeItem(R.id.action_delete)
        }
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        if (item.itemId == R.id.action_select_all) {
            checkAll()
        } else {
            callback.multiSelectionItemClick(item, ArrayList(checked))
            finishActionMode()
        }
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        clearChecked()
        actionMode = null
        onBackPressedCallback.remove()
    }

    private fun isItemSelected(status: Status) = actionMode != null && checked.contains(status)

    private fun isMultiSelectionMode() = actionMode != null

    private fun toggleItemChecked(position: Int): Boolean {
        val identifier = statuses[position]
        if (!checked.remove(identifier)) {
            checked.add(identifier)
        }
        notifyItemChanged(position)
        updateCab()
        return true
    }

    private fun checkAll() {
        if (actionMode != null) {
            checked.clear()
            for (i in statuses) {
                checked.add(i)
            }
            notifyDataSetChanged()
            updateCab()
        }
    }

    private fun clearChecked() {
        checked.clear()
        notifyDataSetChanged()
    }

    private fun updateCab() {
        if (actionMode == null) {
            actionMode = (activity as AppCompatActivity).startSupportActionMode(this)
            activity.onBackPressedDispatcher.addCallback(onBackPressedCallback)
        }
        val size = checked.size
        if (size <= 0) actionMode?.finish()
        else actionMode?.title = activity.getString(R.string.x_selected, size)
    }

    fun finishActionMode() {
        actionMode?.finish()
        clearChecked()
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (actionMode != null) {
                actionMode?.finish()
                remove()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener,
        OnLongClickListener {
        val image: ImageView?
        val date: TextView?
        val state: TextView?
        val clientIcon: ImageView?

        private val status: Status
            get() = statuses[layoutPosition]

        init {
            val binding = ItemStatusBinding.bind(itemView)
            image = binding.image
            date = binding.date
            state = binding.state
            state.isVisible = isSaveEnabled
            clientIcon = binding.clientIcon

            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(view: View) {
            if (!isSavingContent) {
                if (isMultiSelectionMode()) {
                    toggleItemChecked(layoutPosition)
                } else {
                    activity.showStatusOptions(status, isSaveEnabled, isDeleteEnabled, callback)
                }
            }
        }

        override fun onLongClick(view: View): Boolean {
            if (!isSavingContent) {
                when (activity.preferences().getLongPressAction()) {
                    LongPressAction.VALUE_MULTI_SELECTION -> {
                        return toggleItemChecked(layoutPosition)
                    }

                    LongPressAction.VALUE_PREVIEW -> {
                        callback.previewStatusClick(status)
                        return true
                    }

                    LongPressAction.VALUE_SAVE -> {
                        if (isSaveEnabled) {
                            callback.saveStatusClick(status)
                        }
                        return true
                    }

                    LongPressAction.VALUE_SHARE -> {
                        callback.shareStatusClick(status)
                        return true
                    }

                    LongPressAction.VALUE_DELETE -> {
                        if (isDeleteEnabled) {
                            callback.deleteStatusClick(status)
                        }
                        return true
                    }
                }
            }
            return false
        }
    }

    init {
        setHasStableIds(true)
    }
}