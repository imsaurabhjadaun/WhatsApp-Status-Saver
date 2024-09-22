package com.savestatus.wsstatussaver.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.savestatus.wsstatussaver.R
import com.savestatus.wsstatussaver.database.MessageEntity
import com.savestatus.wsstatussaver.extensions.time
import com.savestatus.wsstatussaver.interfaces.IMessageCallback

class MessageAdapter(
    private val context: Context,
    private var messages: List<MessageEntity>,
    private val callback: IMessageCallback
) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.message.text = message.content
        holder.time.text = message.time.time(useTimeFormat = true)
    }

    override fun getItemCount(): Int = messages.size

    @SuppressLint("NotifyDataSetChanged")
    fun data(messages: List<MessageEntity>) {
        this.messages = messages
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener,
        View.OnLongClickListener {
        val message: TextView = itemView.findViewById(R.id.message)
        val time: TextView = itemView.findViewById(R.id.time)

        private val currentMessage: MessageEntity?
            get() = layoutPosition.let { position -> if (position > -1) messages[position] else null }

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            currentMessage?.let { callback.messageClick(it) }
        }

        override fun onLongClick(v: View?): Boolean {
            currentMessage?.let { callback.messageLongClick(it) }
            return true
        }
    }
}