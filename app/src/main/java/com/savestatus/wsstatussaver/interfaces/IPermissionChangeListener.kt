package com.savestatus.wsstatussaver.interfaces

interface IPermissionChangeListener {
    fun permissionsStateChanged(hasPermissions: Boolean)
}