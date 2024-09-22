package com.savestatus.wsstatussaver.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import com.savestatus.wsstatussaver.database.Conversation
import com.savestatus.wsstatussaver.database.MessageEntity
import com.savestatus.wsstatussaver.model.Country
import com.savestatus.wsstatussaver.model.ShareData
import com.savestatus.wsstatussaver.model.Status
import com.savestatus.wsstatussaver.model.StatusQueryResult
import com.savestatus.wsstatussaver.model.StatusType

interface Repository {
    suspend fun statuses(type: StatusType): StatusQueryResult
    suspend fun savedStatuses(type: StatusType): StatusQueryResult
    suspend fun shareStatus(status: Status): ShareData
    suspend fun shareStatuses(statuses: List<Status>): ShareData
    suspend fun saveStatus(status: Status, saveName: String?): Uri?
    suspend fun saveStatuses(statuses: List<Status>): Map<Status, Uri>
    suspend fun deleteStatus(status: Status): Boolean
    suspend fun deleteStatuses(statuses: List<Status>): Int
    suspend fun allCountries(): List<Country>
    suspend fun defaultCountry(): Country
    fun defaultCountry(country: Country)
    fun listConversations(): LiveData<List<Conversation>>
    fun receivedMessages(sender: Conversation): LiveData<List<MessageEntity>>
    suspend fun insertMessage(message: MessageEntity): Long
    suspend fun removeMessage(message: MessageEntity)
    suspend fun deleteConversation(sender: Conversation)
    suspend fun clearMessages()
}

class RepositoryImpl(
    private val statusesRepository: StatusesRepository,
    private val countryRepository: CountryRepository,
    private val messageRepository: MessageRepository
) : Repository {

    override suspend fun statuses(type: StatusType): StatusQueryResult = statusesRepository.statuses(type)

    override suspend fun savedStatuses(type: StatusType): StatusQueryResult = statusesRepository.savedStatuses(type)

    override suspend fun shareStatus(status: Status): ShareData = statusesRepository.share(status)

    override suspend fun shareStatuses(statuses: List<Status>): ShareData = statusesRepository.share(statuses)

    override suspend fun saveStatus(status: Status, saveName: String?): Uri? = statusesRepository.save(status, saveName)

    override suspend fun saveStatuses(statuses: List<Status>): Map<Status, Uri> =
        statusesRepository.save(statuses)

    override suspend fun deleteStatus(status: Status): Boolean = statusesRepository.delete(status)

    override suspend fun deleteStatuses(statuses: List<Status>): Int = statusesRepository.delete(statuses)

    override suspend fun allCountries(): List<Country> = countryRepository.allCountries()

    override suspend fun defaultCountry(): Country = countryRepository.defaultCountry()

    override fun defaultCountry(country: Country) = countryRepository.defaultCountry(country)

    override fun listConversations(): LiveData<List<Conversation>> = messageRepository.listConversations()

    override fun receivedMessages(sender: Conversation): LiveData<List<MessageEntity>> {
        return messageRepository.listMessages(sender.name)
    }

    override suspend fun insertMessage(message: MessageEntity) = messageRepository.insertMessage(message)

    override suspend fun removeMessage(message: MessageEntity) = messageRepository.removeMessage(message)

    override suspend fun deleteConversation(sender: Conversation) = messageRepository.deleteConversation(sender.name)

    override suspend fun clearMessages() = messageRepository.clearMessages()
}