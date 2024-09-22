package com.savestatus.wsstatussaver.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.savestatus.wsstatussaver.R
import com.savestatus.wsstatussaver.database.Conversation
import com.savestatus.wsstatussaver.extensions.time
import com.savestatus.wsstatussaver.interfaces.IConversationCallback
import java.util.concurrent.TimeUnit

class ConversationAdapter(
    private val context: Context,
    private var conversations: List<Conversation>,
    private val callback: IConversationCallback
) : RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conversation = conversations[position]
        holder.name?.text = conversation.name
        holder.message?.text = conversation.latestMessage
        holder.time?.text = conversation.latestMessageTime.time(5, TimeUnit.DAYS)
    }

    override fun getItemCount(): Int = conversations.size

    override fun getItemId(position: Int): Long {
        return conversations[position].id
    }

    @SuppressLint("NotifyDataSetChanged")
    fun data(dataSet: List<Conversation>) {
        this.conversations = dataSet
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener,
        View.OnLongClickListener {

        val name: TextView? = itemView.findViewById(R.id.name)
        val message: TextView? = itemView.findViewById(R.id.message)
        val time: TextView? = itemView.findViewById(R.id.time)

        private val currentItem: Conversation
            get() = conversations[layoutPosition]

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(view: View) {
            callback.conversationClick(currentItem)
        }

        override fun onLongClick(v: View?): Boolean {
            callback.conversationLongClick(currentItem)
            return true
        }
    }
}