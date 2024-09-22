package com.savestatus.wsstatussaver.interfaces

import android.view.MenuItem
import com.savestatus.wsstatussaver.model.Status

interface IMultiStatusCallback : IStatusCallback {
    fun multiSelectionItemClick(item: MenuItem, selection: List<Status>)
}