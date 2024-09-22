package com.savestatus.wsstatussaver.storage

import android.os.Environment
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.savestatus.wsstatussaver.R

class StorageDevice internal constructor(
    val path: String?,
    private val uuid: String?,
    private val isRemovable: Boolean,
    private val isPrimary: Boolean,
    private val isEmulated: Boolean,
    private val state: String?) {

    private val isSDCard: Boolean
        get() = isRemovable && !isPrimary

    @get:DrawableRes
    val iconRes: Int
        get() = if (isSDCard) R.drawable.ic_sd_card_24dp else R.drawable.ic_phone_android_24dp

    @get:StringRes
    val nameRes: Int
        get() = if (isSDCard) R.string.statuses_location_sd_card else R.string.statuses_location_device

    val isValid: Boolean
        get() = state == Environment.MEDIA_MOUNTED && !path.isNullOrEmpty()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as StorageDevice
        if (isRemovable != that.isRemovable) return false
        if (isPrimary != that.isPrimary) return false
        if (isEmulated != that.isEmulated) return false
        if (path != that.path) return false
        if (uuid != that.uuid) return false
        return state == that.state
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + uuid.hashCode()
        result = 31 * result + if (isRemovable) 1 else 0
        result = 31 * result + if (isPrimary) 1 else 0
        result = 31 * result + if (isEmulated) 1 else 0
        result = 31 * result + state.hashCode()
        return result
    }

    override fun toString(): String {
        return "StorageDevice{" +
            "path='" + path + '\'' +
            ", uuid='" + uuid + '\'' +
            ", isRemovable=" + isRemovable +
            ", isPrimary=" + isPrimary +
            ", isEmulated=" + isEmulated +
            ", state='" + state + '\'' +
            '}'
    }
}