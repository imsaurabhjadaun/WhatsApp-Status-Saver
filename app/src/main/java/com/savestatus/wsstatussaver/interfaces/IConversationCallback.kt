package com.savestatus.wsstatussaver.interfaces

import com.savestatus.wsstatussaver.database.Conversation

interface IConversationCallback {
    fun conversationClick(conversation: Conversation)
    fun conversationLongClick(conversation: Conversation)
}