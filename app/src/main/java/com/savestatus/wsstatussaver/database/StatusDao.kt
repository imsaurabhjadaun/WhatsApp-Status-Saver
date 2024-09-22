package com.savestatus.wsstatussaver.database

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StatusDao {
    @Insert
    fun saveStatus(status: StatusEntity): Long

    @Query("DELETE FROM saved_statuses WHERE save_name = :name")
    fun removeSave(name: String)

    @Query("DELETE FROM saved_statuses WHERE status_type = :type")
    suspend fun removeSaves(type: Int)

    @Query("SELECT EXISTS(SELECT * FROM saved_statuses WHERE original_uri = :origin OR save_name = :name)")
    fun statusSaved(origin: Uri, name: String): Boolean

    @Query("SELECT * FROM saved_statuses WHERE status_type = :type")
    fun savedStatuses(type: Int): LiveData<List<StatusEntity>>
}