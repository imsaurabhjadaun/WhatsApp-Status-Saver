package com.savestatus.wsstatussaver.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.savestatus.wsstatussaver.databinding.ItemClientBinding
import com.savestatus.wsstatussaver.extensions.animateAlpha
import com.savestatus.wsstatussaver.interfaces.IClientCallback
import com.savestatus.wsstatussaver.model.WaClient

class ClientAdapter(private val context: Context, private val callback: IClientCallback) :
    RecyclerView.Adapter<ClientAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)
    private var clients: List<WaClient> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemClientBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val client = clients[position]
        holder.icon?.setImageDrawable(client.getIcon(context))
        holder.name?.text = client.displayName
        holder.description?.text = client.getDescription(context)
        configureCheckIcon(holder, client)
    }

    private fun configureCheckIcon(holder: ViewHolder, client: WaClient) {
        val checkMode = callback.checkModeForClient(client)
        if (checkMode == IClientCallback.MODE_UNCHECKABLE) {
            holder.check?.isVisible = false
        } else {
            holder.check?.isVisible = true
            holder.check?.animateAlpha(if (checkMode == IClientCallback.MODE_CHECKED) 1f else 0.35f)
        }
    }

    override fun getItemCount(): Int = clients.size

    @SuppressLint("NotifyDataSetChanged")
    fun setClients(clients: List<WaClient>) {
        this.clients = clients
        notifyDataSetChanged()
    }

    inner class ViewHolder(binding: ItemClientBinding) : RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        var icon: ImageView? = binding.icon
        var name: TextView? = binding.name
        var description: TextView? = binding.description
        var check: ImageView? = binding.check

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            val currentClient = clients[layoutPosition]
            callback.clientClick(currentClient)
        }
    }
}