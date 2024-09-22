package com.savestatus.wsstatussaver.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Person
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.os.BundleCompat.getParcelable
import androidx.core.os.BundleCompat.getParcelableArrayList
import com.savestatus.wsstatussaver.database.MessageEntity
import com.savestatus.wsstatussaver.extensions.*
import com.savestatus.wsstatussaver.repository.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.regex.Pattern

class MessageCatcherService : NotificationListenerService(), KoinComponent {

    private val repository: Repository by inject()
    private val serviceScope = CoroutineScope(Job() + Main)

    override fun onListenerConnected() {
        super.onListenerConnected()
        if (!preferences().isMessageViewEnabled) {
            requestUnbind()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null || !preferences().isMessageViewEnabled) return
        serviceScope.launch(IO) {
            val client = getClientIfInstalled(sbn.packageName)
            if (client != null) {
                val extras = sbn.notification.extras
                if (!isSelf(extras)) {
                    val received = sbn.notification.`when`
                    val senderName = getGroupName(extras) ?: getSenderName(extras)
                    val message = extras.getString(Notification.EXTRA_TEXT)
                    if (!senderName.isNullOrBlank() && !message.isNullOrBlank()) {
                        if (!preferences().isBlacklistedMessageSender(senderName)) {
                            val messageEntity = MessageEntity(
                                clientPackage = client.packageName,
                                time = received,
                                senderName = senderName,
                                content = message
                            )
                            repository.insertMessage(messageEntity)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("InlinedApi")
    private fun getGroupName(extras: Bundle): String? {
        val conversationTitle = extras.getString(Notification.EXTRA_CONVERSATION_TITLE)
        val isGroupConversation = extras.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION)
        if (conversationTitle.isNullOrBlank() || (hasP() && !isGroupConversation)) {
            return null
        }
        try {
            val matcher = GROUP_NAME_PATTERN.matcher(conversationTitle)
            if (matcher.find()) {
                val group = matcher.group(1)
                return group?.trim()
            }
        } catch (_: Exception) {}
        return conversationTitle.trim()
    }

    @Suppress("DEPRECATION")
    private fun isSelf(extras: Bundle): Boolean {
        if (hasP()) {
            val messagingUser = getParcelable(extras, Notification.EXTRA_MESSAGING_PERSON, Person::class.java)
            return messagingUser?.name == extras.getString(Notification.EXTRA_TITLE)
        } else {
            val selfDisplayName = extras.getString(Notification.EXTRA_SELF_DISPLAY_NAME)
            return selfDisplayName == extras.getString(Notification.EXTRA_TITLE)
        }
    }

    private fun getSenderName(extras: Bundle): String? {
        val title = extras.getString(Notification.EXTRA_TITLE)
        if (hasP()) {
            val people = getParcelableArrayList(extras, Notification.EXTRA_PEOPLE_LIST, Person::class.java)
            return people?.firstOrNull()?.name?.toString() ?: title
        }
        return title
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private val GROUP_NAME_PATTERN = Pattern.compile("^(.*?)\\s*\\((\\d+\\s*\\w*)\\)")
    }
}