package com.savestatus.wsstatussaver.repository

import androidx.lifecycle.LiveData
import com.savestatus.wsstatussaver.database.Conversation
import com.savestatus.wsstatussaver.database.MessageDao
import com.savestatus.wsstatussaver.database.MessageEntity

interface MessageRepository {
    fun listConversations(): LiveData<List<Conversation>>
    fun listMessages(sender: String): LiveData<List<MessageEntity>>
    suspend fun insertMessage(message: MessageEntity): Long
    suspend fun removeMessage(message: MessageEntity)
    suspend fun deleteConversation(sender: String)
    suspend fun clearMessages()
}

class MessageRepositoryImpl(private val messageDao: MessageDao) : MessageRepository {

    override fun listConversations(): LiveData<List<Conversation>> =
        messageDao.queryConversations()

    override fun listMessages(sender: String): LiveData<List<MessageEntity>> =
        messageDao.queryMessages(sender)

    override suspend fun insertMessage(message: MessageEntity) =
        messageDao.insetMessage(message)

    override suspend fun removeMessage(message: MessageEntity) =
        messageDao.removeMessage(message)

    override suspend fun deleteConversation(sender: String) =
        messageDao.deleteConversation(sender)

    override suspend fun clearMessages() {
        messageDao.clearMessages()
    }
}