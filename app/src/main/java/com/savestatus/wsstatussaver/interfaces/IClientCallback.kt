package com.savestatus.wsstatussaver.interfaces

import com.savestatus.wsstatussaver.model.WaClient

interface IClientCallback {
    fun clientClick(client: WaClient)
    fun checkModeForClient(client: WaClient): Int = MODE_UNCHECKABLE

    companion object {
        const val MODE_UNCHECKABLE = 0
        const val MODE_CHECKED = 1
        const val MODE_UNCHECKED = 2
    }
}