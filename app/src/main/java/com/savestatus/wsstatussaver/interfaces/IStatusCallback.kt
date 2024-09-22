package com.savestatus.wsstatussaver.interfaces

import com.savestatus.wsstatussaver.model.Status

interface IStatusCallback {
    fun previewStatusClick(status: Status)
    fun saveStatusClick(status: Status)
    fun shareStatusClick(status: Status)
    fun deleteStatusClick(status: Status)
}