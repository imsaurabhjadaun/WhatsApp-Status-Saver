package com.savestatus.wsstatussaver.interfaces

import com.savestatus.wsstatussaver.database.MessageEntity

interface IMessageCallback {
    fun messageClick(message: MessageEntity)
    fun messageLongClick(message: MessageEntity)
}